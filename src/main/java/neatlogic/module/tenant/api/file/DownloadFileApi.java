/*
 *
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 */

package neatlogic.module.tenant.api.file;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.NoAuth;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.constvalue.CacheControlType;
import neatlogic.framework.common.util.FileUtil;
import neatlogic.framework.exception.file.FileAccessDeniedException;
import neatlogic.framework.exception.file.FileNotFoundException;
import neatlogic.framework.exception.file.FileTypeHandlerNotFoundException;
import neatlogic.framework.exception.user.NoTenantException;
import neatlogic.framework.file.core.FileOperationType;
import neatlogic.framework.file.core.FileTypeHandlerFactory;
import neatlogic.framework.file.core.IFileTypeHandler;
import neatlogic.framework.file.dao.mapper.FileMapper;
import neatlogic.framework.file.dto.FileVo;
import neatlogic.framework.file.dto.FileDownloadVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.binarystream.PrivateBinaryStreamApiComponentBase;
import neatlogic.module.tenant.util.ImageFileSnapshot;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Service

@AuthAction(action = NoAuth.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class DownloadFileApi extends PrivateBinaryStreamApiComponentBase {

    @Resource
    private FileMapper fileMapper;

    @Override
    public String getToken() {
        return "file/download";
    }

    @Override
    public String getName() {
        return "附件下载接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    /** 按既有附件权限下载，并对图片内容执行与专用入口一致的安全校验。 */
    @CacheControl(cacheControlType = CacheControlType.MAXAGE, maxAge = 30000)
    @Input({@Param(name = "id", type = ApiParamType.LONG, desc = "附件id", isRequired = true),
            @Param(name = "lastModified", type = ApiParamType.DOUBLE, desc = "最后修改时间（秒，支持小数位）")})
    @Description(desc = "附件下载接口")
    @Override
    public Object myDoService(JSONObject paramObj, HttpServletRequest request, HttpServletResponse response) throws Exception {
        Long id = paramObj.getLong("id");
        long lastModifiedLong = 0L;
        boolean isNeedDownLoad = false;
        FileVo fileVo = fileMapper.getFileById(id);
        String tenantUuid = TenantContext.get().getTenantUuid();
        if (StringUtils.isBlank(tenantUuid)) {
            throw new NoTenantException();
        }
        if (fileVo != null) {
            if (paramObj.getDouble("lastModified") != null) {
                BigDecimal lastModifiedDec = new BigDecimal(Double.toString(paramObj.getDouble("lastModified")));
                lastModifiedLong = lastModifiedDec.multiply(new BigDecimal("1000")).longValue();
            }
            if (lastModifiedLong == 0L || lastModifiedLong < fileVo.getUploadTime().getTime()) {
                String userUuid = UserContext.get().getUserUuid();
                IFileTypeHandler fileTypeHandler = FileTypeHandlerFactory.getHandler(fileVo.getType());
                if (fileTypeHandler != null) {
                    if (Objects.equals(userUuid, fileVo.getUserUuid()) || fileTypeHandler.valid(userUuid, fileVo, paramObj)) {
                        // 下载内容和元信息来自同一次存储请求，关闭结果时同时释放文件流。
                        try (FileDownloadVo download = FileUtil.getDownloadData(fileVo.getPath())) {
                            InputStream input = download == null ? null : download.getInputStream();
                            if (input != null) {
                                // 使用有效远端文件名，完整保留后缀；编码分号、引号等以兼容现有前端解析。
                                String fileName = StringUtils.defaultIfBlank(download.getFileName(), fileVo.getName());
                                response.setHeader("Content-Disposition", " attachment; filename=\""
                                        + URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20") + "\"");
                                if ("image".equalsIgnoreCase(fileVo.getType())) {
                                    // 通用下载同样校验图片，防止历史污染记录绕过图片专用入口。
                                    try (ImageFileSnapshot image = ImageFileSnapshot.createForDownload(input)) {
                                        image.writeTo(response);
                                    }
                                } else {
                                    // 远端可能返回 ZIP 等转换格式，缺少元信息时保留原附件的媒体类型。
                                    String contentType = StringUtils.defaultIfBlank(download.getContentType(), fileVo.getContentType());
                                    response.setContentType(StringUtils.defaultIfBlank(contentType, "application/octet-stream"));
                                    try (ServletOutputStream output = response.getOutputStream()) {
                                        IOUtils.copyLarge(input, output);
                                        output.flush();
                                    }
                                }
                            }
                        }
                        isNeedDownLoad = true;
                    } else {
                        throw new FileAccessDeniedException(fileVo.getName(), FileOperationType.DOWNLOAD.getText());
                    }
                } else {
                    throw new FileTypeHandlerNotFoundException(fileVo.getType());
                }
            }
            if (!isNeedDownLoad) {
                if (response != null) {
                    response.setHeader("Content-Disposition", " attachment; filename=\"" + neatlogic.framework.util.FileUtil.getEncodedFileName(fileVo.getName()) + "\"");
                    response.setStatus(204);
                    response.getWriter().print(StringUtils.EMPTY);
                }
            }
        } else {
            throw new FileNotFoundException(id);
        }
        return null;
    }
}

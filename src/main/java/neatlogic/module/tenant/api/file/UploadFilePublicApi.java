/*
 * Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package neatlogic.module.tenant.api.file;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.util.FileUtil;
import neatlogic.framework.exception.file.EmptyFileException;
import neatlogic.framework.exception.file.FileExtNotAllowedException;
import neatlogic.framework.exception.file.FileTooLargeException;
import neatlogic.framework.exception.file.FileTypeHandlerNotFoundException;
import neatlogic.framework.exception.user.NoTenantException;
import neatlogic.framework.file.core.FileTypeHandlerFactory;
import neatlogic.framework.file.core.IFileTypeHandler;
import neatlogic.framework.file.dao.mapper.FileMapper;
import neatlogic.framework.file.dto.FileTypeVo;
import neatlogic.framework.file.dto.FileVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.publicapi.PublicBinaryStreamApiComponentBase;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Service
@Deprecated
@OperationType(type = OperationTypeEnum.CREATE)
public class UploadFilePublicApi extends PublicBinaryStreamApiComponentBase {
    // static Logger logger = LoggerFactory.getLogger(UploadFilePublicApi.class);

    @Resource
    private FileMapper fileMapper;

    @Override
    public String getName() {
        return "附件上传接口(供第三方使用)";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "param", type = ApiParamType.STRING, desc = "附件参数名称", isRequired = true),
            @Param(name = "type", type = ApiParamType.STRING, desc = "附件类型", isRequired = true)})
    @Output({@Param(explode = FileVo.class)})
    @Description(desc = "附件上传接口(供第三方使用)")
    @Override
    public Object myDoService(JSONObject paramObj, HttpServletRequest request, HttpServletResponse response) throws Exception {
        String tenantUuid = TenantContext.get().getTenantUuid();
        if (StringUtils.isBlank(tenantUuid)) {
            throw new NoTenantException();
        }
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        String paramName = paramObj.getString("param");
        String type = paramObj.getString("type");
        List<FileTypeVo> fileTypeList = FileTypeHandlerFactory.getActiveFileTypeHandler();
        FileTypeVo fileTypeVo = null;
        for (FileTypeVo f : fileTypeList) {
            if (f.getName().equalsIgnoreCase(type)) {
                fileTypeVo = f;
                break;
            }
        }
        if (fileTypeVo == null) {
            throw new FileTypeHandlerNotFoundException(type);
        }
        FileTypeVo fileTypeConfigVo = fileMapper.getFileTypeConfigByType(fileTypeVo.getName());

        MultipartFile multipartFile = multipartRequest.getFile(paramName);

        if (multipartFile != null && multipartFile.getName() != null) {
            String userUuid = UserContext.get().getUserUuid(true);
            String oldFileName = multipartFile.getOriginalFilename();
            long size = multipartFile.getSize();
            // 如果配置为空代表不受任何限制
            if (fileTypeConfigVo != null) {
                boolean isAllowed = false;
                long maxSize = 0L;
                String fileExt = null;
                if (oldFileName != null) {
                    fileExt = oldFileName.substring(oldFileName.lastIndexOf(".") + 1).toLowerCase();
                }
                JSONObject configObj = fileTypeConfigVo.getConfigObj();
                JSONArray whiteList = new JSONArray();
                JSONArray blackList = new JSONArray();
                if (size == 0) {
                    throw new EmptyFileException();
                }
                if (configObj != null) {
                    whiteList = configObj.getJSONArray("whiteList");
                    blackList = configObj.getJSONArray("blackList");
                    maxSize = configObj.getLongValue("maxSize");
                }
                if (whiteList != null && !whiteList.isEmpty()) {
                    for (int i = 0; i < whiteList.size(); i++) {
                        if (fileExt != null && fileExt.equalsIgnoreCase(whiteList.getString(i))) {
                            isAllowed = true;
                            break;
                        }
                    }
                } else if (blackList != null && !blackList.isEmpty()) {
                    isAllowed = true;
                    for (int i = 0; i < blackList.size(); i++) {
                        if (fileExt != null && fileExt.equalsIgnoreCase(blackList.getString(i))) {
                            isAllowed = false;
                            break;
                        }
                    }
                } else {
                    isAllowed = true;
                }
                if (!isAllowed) {
                    throw new FileExtNotAllowedException(fileExt);
                }
                if (maxSize > 0 && size > maxSize) {
                    throw new FileTooLargeException(size, maxSize);
                }
            }

            IFileTypeHandler fileTypeHandler = FileTypeHandlerFactory.getHandler(type);
            if (fileTypeHandler == null) {
                throw new FileTypeHandlerNotFoundException(type);
            }

            FileVo fileVo = new FileVo();
            fileVo.setName(oldFileName);
            fileVo.setSize(size);
            fileVo.setUserUuid(userUuid);
            fileVo.setType(type);
            fileVo.setContentType(multipartFile.getContentType());
            String filePath = FileUtil.saveData(tenantUuid, multipartFile.getInputStream(), fileVo.getId().toString(), fileVo.getContentType(), fileVo.getType());
            fileVo.setPath(filePath);
            fileMapper.insertFile(fileVo);
            fileTypeHandler.afterUpload(fileVo, paramObj);
            return fileMapper.getFileById(fileVo.getId());
        }
        return null;
    }
}

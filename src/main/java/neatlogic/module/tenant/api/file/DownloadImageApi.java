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
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.NoAuth;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.constvalue.CacheControlType;
import neatlogic.framework.common.util.FileUtil;
import neatlogic.framework.exception.file.FileNotFoundException;
import neatlogic.framework.exception.user.NoTenantException;
import neatlogic.framework.file.dao.mapper.FileMapper;
import neatlogic.framework.file.dto.FileVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.ApiAnonymousAccessSupportEnum;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.binarystream.PrivateBinaryStreamApiComponentBase;
import neatlogic.module.tenant.util.ImageFileSnapshot;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;

@Service

@AuthAction(action = NoAuth.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class DownloadImageApi extends PrivateBinaryStreamApiComponentBase {

    @Resource
    private FileMapper fileMapper;

    @Override
    public String getToken() {
        return "image/download";
    }

    @Override
    public String getName() {
        return "nmtaf.downloadimageapi.getname";
    }

    /**
     * 图片下载支持匿名访问，但token需要加密
     * @return
     */
    @Override
    public ApiAnonymousAccessSupportEnum supportAnonymousAccess() {
        return ApiAnonymousAccessSupportEnum.ANONYMOUS_ACCESS_WITH_ENCRYPTION;
    }

    @Override
    public String getConfig() {
        return null;
    }

    /** 验证图片内容并使用服务端 MIME 输出，保持匿名访问能力。 */
    @CacheControl(cacheControlType = CacheControlType.MAXAGE, maxAge = 30000)
    @Input({@Param(name = "id", type = ApiParamType.LONG, desc = "common.id", isRequired = true)})
    @Description(desc = "nmtaf.downloadimageapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj, HttpServletRequest request, HttpServletResponse response) throws Exception {
        Long id = paramObj.getLong("id");
        FileVo fileVo = fileMapper.getFileById(id);
        String tenantUuid = TenantContext.get().getTenantUuid();
        if (StringUtils.isBlank(tenantUuid)) {
            throw new NoTenantException();
        }
        if (fileVo != null && "image".equals(fileVo.getType())) {
            // 历史记录中的类型和 MIME 不能替代对实际内容的校验。
            try (InputStream input = FileUtil.getData(fileVo.getPath())) {
                if (input != null) {
                    try (ImageFileSnapshot image = ImageFileSnapshot.createForDownload(input)) {
                        image.writeTo(response);
                    }
                }
            }
        } else {
            throw new FileNotFoundException(id);
        }
        return null;
    }
}

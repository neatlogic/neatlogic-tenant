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
import neatlogic.framework.common.constvalue.systemuser.SystemUser;
import neatlogic.framework.documentonline.exception.DocumentOnlineNotFoundException;
import neatlogic.framework.documentonline.util.DocumentOnlineManager;
import neatlogic.framework.exception.user.NoTenantException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.ApiAnonymousAccessSupportEnum;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateBinaryStreamApiComponentBase;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;

@Service
@AuthUser(SystemUser.ANONYMOUS)
@AuthAction(action = NoAuth.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class DownloadClasspathImageApi extends PrivateBinaryStreamApiComponentBase {

    @Override
    public String getToken() {
        return "classpath/image/download";
    }

    @Override
    public String getName() {
        return "图片下载接口";
    }

    @Override
    public ApiAnonymousAccessSupportEnum supportAnonymousAccess() {
        return ApiAnonymousAccessSupportEnum.ANONYMOUS_ACCESS_WITHOUT_ENCRYPTION;
    }

    @Override
    public String getConfig() {
        return null;
    }

    @CacheControl(cacheControlType = CacheControlType.MAXAGE, maxAge = 30000)
    @Input({@Param(name = "filePath", type = ApiParamType.STRING, desc = "文件路径", isRequired = true)})
    @Description(desc = "图片下载接口")
    @Override
    public Object myDoService(JSONObject paramObj, HttpServletRequest request, HttpServletResponse response) throws Exception {
        String filePath = paramObj.getString("filePath");
        String locationPattern = DocumentOnlineManager.getResourceLocationPatternByFilePath(filePath);
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource resource = resolver.getResource(locationPattern);
        if (!resource.exists()) {
            throw new DocumentOnlineNotFoundException(filePath);
        }

        if (StringUtils.isBlank(TenantContext.get().getTenantUuid())) {
            throw new NoTenantException();
        }
        String contentType = "image/" + resource.getFilename().substring(resource.getFilename().lastIndexOf("."));
        response.setContentType(contentType);

        try (ServletOutputStream os = response.getOutputStream();InputStream in = resource.getInputStream()) {
            IOUtils.copyLarge(in, os);
        }
        return null;
    }
}

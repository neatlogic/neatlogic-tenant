/*Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

package neatlogic.module.tenant.api.file;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.constvalue.CacheControlType;
import neatlogic.framework.documentonline.exception.DocumentOnlineNotFoundException;
import neatlogic.framework.exception.user.NoTenantException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.ApiAnonymousAccessSupportEnum;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateBinaryStreamApiComponentBase;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;

@Service

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

        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        org.springframework.core.io.Resource resource = resolver.getResource("classpath:" + filePath);
        if (!resource.exists()) {
            throw new DocumentOnlineNotFoundException(filePath);
        }

        String tenantUuid = TenantContext.get().getTenantUuid();
        if (StringUtils.isBlank(tenantUuid)) {
            throw new NoTenantException();
        }
        ServletOutputStream os = null;
        InputStream in = resource.getInputStream();
        try {
            if (in != null) {
                String contentType = "image/" + resource.getFilename().substring(resource.getFilename().lastIndexOf("."));
                response.setContentType(contentType);
                os = response.getOutputStream();
                IOUtils.copyLarge(in, os);
            }
        } finally {
            if (os != null) {
                os.flush();
                os.close();
            }
            if (in != null) {
                in.close();
            }
        }
        return null;
    }
}

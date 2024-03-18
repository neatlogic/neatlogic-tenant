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
import neatlogic.framework.common.util.FileUtil;
import neatlogic.framework.exception.file.FileNotFoundException;
import neatlogic.framework.exception.user.NoTenantException;
import neatlogic.framework.file.dao.mapper.FileMapper;
import neatlogic.framework.file.dto.FileVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.publicapi.PublicBinaryStreamApiComponentBase;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;

@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class DownloadImagePublicApi extends PublicBinaryStreamApiComponentBase {

    @Resource
    private FileMapper fileMapper;

    @Override
    public String getName() {
        return "图片下载接口(供第三方使用)";
    }

    @Override
	public String getConfig() {
		return null;
	}

	@CacheControl(cacheControlType = CacheControlType.MAXAGE, maxAge = 30000)
	@Input({ @Param(name = "id", type = ApiParamType.LONG, desc = "图片id", isRequired = true) })
	@Description(desc = "图片下载接口(供第三方使用)")
	@Override
	public Object myDoService(JSONObject paramObj, HttpServletRequest request, HttpServletResponse response) throws Exception {
		Long id = paramObj.getLong("id");
		FileVo fileVo = fileMapper.getFileById(id);
		String tenantUuid = TenantContext.get().getTenantUuid();
		if (StringUtils.isBlank(tenantUuid)) {
			throw new NoTenantException();
		}
		if (fileVo != null && fileVo.getType().equals("image")) {
			ServletOutputStream os = null;
			InputStream in = null;
			in = FileUtil.getData(fileVo.getPath());
			if (in != null) {
				response.setContentType(fileVo.getContentType());
				os = response.getOutputStream();
				IOUtils.copyLarge(in, os);
				if (os != null) {
					os.flush();
					os.close();
				}
				if (in != null) {
					in.close();
				}
			}
		} else {
			throw new FileNotFoundException(id);
		}
		return null;
	}
}

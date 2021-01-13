package codedriver.module.tenant.api.file;

import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.util.FileUtil;
import codedriver.framework.exception.file.FileNotFoundException;
import codedriver.framework.exception.user.NoTenantException;
import codedriver.framework.file.dao.mapper.FileMapper;
import codedriver.framework.file.dto.FileVo;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.publicapi.PublicBinaryStreamApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class ImageDownloadPublicApi extends PublicBinaryStreamApiComponentBase {

	@Autowired
	private FileMapper fileMapper;

	@Override
	public String getName() {
		return "图片下载接口(供第三方使用)";
	}

	@Override
	public String getConfig() {
		return null;
	}

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

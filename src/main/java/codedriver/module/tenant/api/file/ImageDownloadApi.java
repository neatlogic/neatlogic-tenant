/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.file;

import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.constvalue.CacheControlType;
import codedriver.framework.common.util.FileUtil;
import codedriver.framework.exception.file.FileNotFoundException;
import codedriver.framework.exception.user.NoTenantException;
import codedriver.framework.file.dao.mapper.FileMapper;
import codedriver.framework.file.dto.FileVo;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateBinaryStreamApiComponentBase;
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
public class ImageDownloadApi extends PrivateBinaryStreamApiComponentBase {

	@Autowired
	private FileMapper fileMapper;

	@Override
	public String getToken() {
		return "image/download";
	}

	@Override
	public String getName() {
		return "图片下载接口";
	}

	@Override
	public boolean supportAnonymousAccess(){
		return true;
	}
	@Override
	public String getConfig() {
		return null;
	}

	@CacheControl(cacheControlType = CacheControlType.MAXAGE, maxAge = 30000)
	@Input({ @Param(name = "id", type = ApiParamType.LONG, desc = "图片id", isRequired = true) })
	@Description(desc = "图片下载接口")
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
//			if (fileVo.getPath().startsWith("file:")) {
//				File file = new File(Config.DATA_HOME() + fileVo.getPath().substring(5));
//				if (file.exists() && file.isFile()) {
//					in = new FileInputStream(file);
//				}
//			} else if(fileVo.getPath().startsWith("minio:")) {
//				in = minioManager.getObject(Config.MINIO_BUCKET(),fileVo.getPath().replaceAll("minio:", ""));
//			}
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

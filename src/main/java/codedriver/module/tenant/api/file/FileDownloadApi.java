package codedriver.module.tenant.api.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.config.Config;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.exception.user.NoTenantException;
import codedriver.framework.file.core.FileTypeHandlerFactory;
import codedriver.framework.file.core.IFileTypeHandler;
import codedriver.framework.file.dao.mapper.FileMapper;
import codedriver.framework.file.dto.FileVo;
import codedriver.framework.minio.core.MinioManager;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.BinaryStreamApiComponentBase;
import codedriver.module.tenant.exception.file.FileAccessDeniedException;
import codedriver.module.tenant.exception.file.FileNotFoundException;
import codedriver.module.tenant.exception.file.FileTypeHandlerNotFoundException;

@Service
public class FileDownloadApi extends BinaryStreamApiComponentBase {

	@Autowired
	private FileMapper fileMapper;
	
	@Autowired
	private MinioManager minioManager;

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

	@Input({ @Param(name = "id", type = ApiParamType.LONG, desc = "附件id", isRequired = true) })
	@Description(desc = "附件下载接口")
	@Override
	public Object myDoService(JSONObject paramObj, HttpServletRequest request, HttpServletResponse response) throws Exception {
		Long id = paramObj.getLong("id");
		FileVo fileVo = fileMapper.getFileById(id);
		String tenantUuid = TenantContext.get().getTenantUuid();
		if (StringUtils.isBlank(tenantUuid)) {
			throw new NoTenantException();
		}
		if (fileVo != null) {
			IFileTypeHandler fileTypeHandler = FileTypeHandlerFactory.getHandler(fileVo.getType());
			if (fileTypeHandler != null) {
				if (fileTypeHandler.valid(UserContext.get().getUserUuid(), paramObj)) {
					ServletOutputStream os = null;
					InputStream in = null;
					if (fileVo.getPath().startsWith("file:")) {
						File file = new File(Config.DATA_HOME() + fileVo.getPath().substring(5));
						if (file.exists() && file.isFile()) {
							in = new FileInputStream(file);
						}
					}else if(fileVo.getPath().startsWith("minio:")) {
						in = minioManager.getObject(Config.MINIO_BUCKET(),fileVo.getPath().replaceAll("minio:", ""));
					}
					if (in != null) {
						String fileNameEncode = "";
						Boolean flag = request.getHeader("User-Agent").indexOf("Gecko") > 0;
						if (request.getHeader("User-Agent").toLowerCase().indexOf("msie") > 0 || flag) {
							fileNameEncode = URLEncoder.encode(fileVo.getName(), "UTF-8");// IE浏览器
						} else {
							fileNameEncode = new String(fileVo.getName().replace(" ", "").getBytes(StandardCharsets.UTF_8), "ISO8859-1");
						}

						if (StringUtils.isBlank(fileVo.getContentType())) {
							response.setContentType("aplication/x-msdownload");
						} else {
							response.setContentType(fileVo.getContentType());
						}
						response.setHeader("Content-Disposition", "attachment;fileName=\"" + fileNameEncode + "\"");
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
					throw new FileAccessDeniedException(fileVo.getName());
				}
			} else {
				throw new FileTypeHandlerNotFoundException(fileVo.getType());
			}
		} else {
			throw new FileNotFoundException(id);
		}
		return null;
	}
}

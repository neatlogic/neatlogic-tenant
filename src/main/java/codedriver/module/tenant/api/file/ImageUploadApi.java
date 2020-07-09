package codedriver.module.tenant.api.file;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.config.Config;
import codedriver.framework.exception.user.NoTenantException;
import codedriver.framework.file.dao.mapper.FileMapper;
import codedriver.framework.file.dto.FileVo;
import codedriver.framework.minio.core.MinioManager;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.BinaryStreamApiComponentBase;

@Service
public class ImageUploadApi extends BinaryStreamApiComponentBase {
	static Logger logger = LoggerFactory.getLogger(ImageUploadApi.class);
	@Autowired
	private MinioManager minioManager;

	@Autowired
	private FileMapper fileMapper;

	@Override
	public String getToken() {
		return "image/upload";
	}

	@Override
	public String getName() {
		return "图片上传接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Override
	public boolean isRaw() {
		return true;
	}

	@Output({ @Param(explode = FileVo.class) })
	@Description(desc = "图片上传接口")
	@Override
	public Object myDoService(JSONObject paramObj, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String tenantUuid = TenantContext.get().getTenantUuid();
		if (StringUtils.isBlank(tenantUuid)) {
			throw new NoTenantException();
		}
		MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
		String paramName = "upload";
		JSONObject returnObj = new JSONObject();
		try {
			MultipartFile multipartFile = multipartRequest.getFile(paramName);
			if (multipartFile != null && multipartFile.getName() != null && multipartFile.getContentType().startsWith("image")) {
				String userUuid = UserContext.get().getUserUuid(true);
				String oldFileName = multipartFile.getOriginalFilename();
				Long size = multipartFile.getSize();

				FileVo fileVo = new FileVo();
				fileVo.setName(oldFileName);
				fileVo.setSize(size);
				fileVo.setUserUuid(userUuid);
				fileVo.setType("image");
				fileVo.setContentType(multipartFile.getContentType());

				try {
					SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmm");
					String finalPath ="/" + tenantUuid + "/images/"+format.format(new Date()) + "/" + fileVo.getId(); 
					fileVo.setPath("minio:" + finalPath);
					minioManager.saveObject(Config.MINIO_BUCKET, finalPath, multipartFile.getInputStream(), size,multipartFile.getContentType());
				} catch (Exception ex) {
					//如果minio异常，则上传到本地
					logger.error(ex.getMessage(),ex);
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy" + File.separator + "MM" + File.separator + "dd");
					String filePath = tenantUuid + File.separator + sdf.format(new Date()) + File.separator + fileVo.getId();
					String finalPath = Config.DATA_HOME() + filePath;
					File file = new File(finalPath);
					if (!file.getParentFile().exists()) {
						file.getParentFile().mkdirs();
					}
					FileOutputStream fos = new FileOutputStream(file);
					IOUtils.copyLarge(multipartFile.getInputStream(), fos);
					fos.flush();
					fos.close();
					fileVo.setPath("file:" + filePath);
				}

				fileMapper.insertFile(fileVo);

				returnObj.put("uploaded", true);
				returnObj.put("url", "api/binary/image/download?id=" + fileVo.getId());
			} else {
				returnObj.put("uploaded", false);
				returnObj.put("error", "请选择图片文件");
			}
		} catch (Exception ex) {
			returnObj.put("uploaded", false);
			returnObj.put("error", ex.getMessage());
		}
		return returnObj;

	}
}

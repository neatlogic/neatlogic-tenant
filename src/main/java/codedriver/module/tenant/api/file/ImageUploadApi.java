package codedriver.module.tenant.api.file;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.exception.user.NoTenantException;
import codedriver.framework.file.dao.mapper.FileMapper;
import codedriver.framework.file.dto.FileVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.IsActived;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.BinaryStreamApiComponentBase;

@Service
@IsActived
public class ImageUploadApi extends BinaryStreamApiComponentBase {

	@Autowired
	private FileSystem fileSystem;

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

				String finalPath = "/" + tenantUuid + "/images/" + fileVo.getUuid();
				FSDataOutputStream fos = fileSystem.create(new Path(finalPath));
				IOUtils.copyLarge(multipartFile.getInputStream(), fos);
				fos.flush();
				fos.close();

				fileMapper.insertFile(fileVo);

				returnObj.put("uploaded", true);
				returnObj.put("url", "api/binary/image/download?uuid=" + fileVo.getUuid());
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

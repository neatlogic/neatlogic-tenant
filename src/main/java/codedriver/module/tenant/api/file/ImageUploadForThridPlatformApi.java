package codedriver.module.tenant.api.file;

import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.util.FileUtil;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.UserVo;
import codedriver.framework.exception.user.NoTenantException;
import codedriver.framework.exception.user.UserNotFoundException;
import codedriver.framework.file.core.LocalFileSystemHandler;
import codedriver.framework.file.core.MinioFileSystemHandler;
import codedriver.framework.file.dao.mapper.FileMapper;
import codedriver.framework.file.dto.FileVo;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.publicapi.PublicBinaryStreamApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Service
@OperationType(type = OperationTypeEnum.CREATE)
public class ImageUploadForThridPlatformApi extends PublicBinaryStreamApiComponentBase {
	static Logger logger = LoggerFactory.getLogger(ImageUploadForThridPlatformApi.class);

	@Autowired
	private FileMapper fileMapper;
	@Autowired
	private UserMapper userMapper;

	@Override
	public String getName() {
		return "图片上传接口(供第三方使用)";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Override
	public boolean isRaw() {
		return true;
	}

	@Input({ @Param(name = "userUuid", type = ApiParamType.STRING, desc = "上传者UUID", isRequired = true) })
	@Output({ @Param(explode = FileVo.class) })
	@Description(desc = "图片上传接口(供第三方使用)")
	@Override
	public Object myDoService(JSONObject paramObj, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String tenantUuid = TenantContext.get().getTenantUuid();
		if (StringUtils.isBlank(tenantUuid)) {
			throw new NoTenantException();
		}
		String userUuid = paramObj.getString("userUuid");
		UserVo user = userMapper.getUserByUuid(userUuid);
		if(user == null){
			throw new UserNotFoundException(userUuid);
		}
		MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
		String paramName = "upload";
		JSONObject returnObj = new JSONObject();
		try {
			MultipartFile multipartFile = multipartRequest.getFile(paramName);
			if (multipartFile != null && multipartFile.getName() != null && multipartFile.getContentType().startsWith("image")) {
//				String userUuid = UserContext.get().getUserUuid(true);
				String oldFileName = multipartFile.getOriginalFilename();
				Long size = multipartFile.getSize();

				FileVo fileVo = new FileVo();
				fileVo.setName(oldFileName);
				fileVo.setSize(size);
				fileVo.setUserUuid(userUuid);
				fileVo.setType("image");
				fileVo.setContentType(multipartFile.getContentType());
				String filePath = null;
				try {
//					SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmm");
//					String finalPath ="/" + tenantUuid + "/images/"+format.format(new Date()) + "/" + fileVo.getId();
//					fileVo.setPath("minio:" + finalPath);
					filePath = FileUtil.saveData(MinioFileSystemHandler.NAME,tenantUuid,multipartFile.getInputStream(),fileVo.getId(),fileVo.getContentType(),fileVo.getType());
				} catch (Exception ex) {
					//如果minio出现异常，则上传到本地
					logger.error(ex.getMessage(),ex);
					filePath = FileUtil.saveData(LocalFileSystemHandler.NAME,tenantUuid,multipartFile.getInputStream(),fileVo.getId(),fileVo.getContentType(),fileVo.getType());
				}
				fileVo.setPath(filePath);
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

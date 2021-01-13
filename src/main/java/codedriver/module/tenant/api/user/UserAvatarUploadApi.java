package codedriver.module.tenant.api.user;

import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.auth.label.NO_AUTH;
import codedriver.framework.common.util.FileUtil;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.UserVo;
import codedriver.framework.exception.user.NoTenantException;
import codedriver.framework.file.core.LocalFileSystemHandler;
import codedriver.framework.file.core.MinioFileSystemHandler;
import codedriver.framework.file.dao.mapper.FileMapper;
import codedriver.framework.file.dto.FileVo;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateBinaryStreamApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Service
@Transactional
@AuthAction(action = NO_AUTH.class)
@OperationType(type = OperationTypeEnum.OPERATE)
public class UserAvatarUploadApi extends PrivateBinaryStreamApiComponentBase {
	static Logger logger = LoggerFactory.getLogger(UserAvatarUploadApi.class);

	@Autowired
	private UserMapper userMapper;
	@Autowired
	private FileMapper fileMapper;

	@Override
	public String getToken() {
		return "user/avatar/upload";
	}

	@Override
	public String getName() {
		return "用户头像上传";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Override
	public boolean isRaw() {
		return true;
	}

	@Output({@Param(explode = FileVo.class)})
	@Description(desc = "用户头像上传")
	@Override
	public Object myDoService(JSONObject paramObj, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String tenantUuid = TenantContext.get().getTenantUuid();
		if (StringUtils.isBlank(tenantUuid)) {
			throw new NoTenantException();
		}
		String userUuid = UserContext.get().getUserUuid(true);
		UserVo user = userMapper.getUserByUuid(userUuid);
		MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
		String paramName = "upload";
		JSONObject returnObj = new JSONObject();
		try {
			MultipartFile multipartFile = multipartRequest.getFile(paramName);
			if (multipartFile != null && multipartFile.getName() != null && multipartFile.getContentType().startsWith("image")) {
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
					filePath = FileUtil.saveData(MinioFileSystemHandler.NAME,tenantUuid,multipartFile.getInputStream(),fileVo.getId().toString(),fileVo.getContentType(),fileVo.getType());
				} catch (Exception ex) {
					//如果minio出现异常，则上传到本地
					logger.error(ex.getMessage(),ex);
					filePath = FileUtil.saveData(LocalFileSystemHandler.NAME,tenantUuid,multipartFile.getInputStream(),fileVo.getId().toString(),fileVo.getContentType(),fileVo.getType());
				}
				fileVo.setPath(filePath);
				fileMapper.insertFile(fileVo);
				Long fileId = fileVo.getId();
				/** 保存头像数据 */
				JSONObject userInfo = new JSONObject();
				userInfo.put("avatar","api/binary/image/download?id=" + fileId);
				user.setUserInfo(userInfo.toJSONString());
				userMapper.updateUserInfo(user);
				returnObj.put("uploaded", true);
				returnObj.put("url", "api/binary/image/download?id=" + fileId);

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

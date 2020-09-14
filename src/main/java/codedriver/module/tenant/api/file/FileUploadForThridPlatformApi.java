package codedriver.module.tenant.api.file;

import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.config.Config;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.util.FileUtil;
import codedriver.framework.common.util.RC4Util;
import codedriver.framework.exception.file.EmptyFileException;
import codedriver.framework.exception.file.FileExtNotAllowedException;
import codedriver.framework.exception.file.FileTooLargeException;
import codedriver.framework.exception.file.FileTypeHandlerNotFoundException;
import codedriver.framework.exception.user.NoTenantException;
import codedriver.framework.file.core.FileTypeHandlerFactory;
import codedriver.framework.file.core.IFileTypeHandler;
import codedriver.framework.file.core.LocalFileSystemHandler;
import codedriver.framework.file.core.MinioFileSystemHandler;
import codedriver.framework.file.dao.mapper.FileMapper;
import codedriver.framework.file.dto.FileTypeVo;
import codedriver.framework.file.dto.FileVo;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.publicapi.PublicBinaryStreamApiComponentBase;
import com.alibaba.fastjson.JSONArray;
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
import java.util.List;

@Service
@OperationType(type = OperationTypeEnum.CREATE)
public class FileUploadForThridPlatformApi extends PublicBinaryStreamApiComponentBase {
	static Logger logger = LoggerFactory.getLogger(FileUploadForThridPlatformApi.class);

	@Autowired
	private FileMapper fileMapper;

	@Override
	public String getName() {
		return "附件上传接口(供第三方使用)";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({@Param(name = "param", type = ApiParamType.STRING, desc = "附件参数名称", isRequired = true),
			@Param(name = "type", type = ApiParamType.STRING, desc = "附件类型", isRequired = true) })
	@Output({ @Param(explode = FileVo.class) })
	@Description(desc = "附件上传接口(供第三方使用)")
	@Override
	public Object myDoService(JSONObject paramObj, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String tenantUuid = TenantContext.get().getTenantUuid();
		if (StringUtils.isBlank(tenantUuid)) {
			throw new NoTenantException();
		}
		MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
		String paramName = paramObj.getString("param");
		String type = paramObj.getString("type");
		List<FileTypeVo> fileTypeList = FileTypeHandlerFactory.getActiveFileTypeHandler();
		FileTypeVo fileTypeVo = null;
		for (FileTypeVo f : fileTypeList) {
			if (f.getName().equalsIgnoreCase(type)) {
				fileTypeVo = f;
				break;
			}
		}
		if (fileTypeVo == null) {
			throw new FileTypeHandlerNotFoundException(type);
		}
		FileTypeVo fileTypeConfigVo = fileMapper.getFileTypeConfigByType(fileTypeVo.getName());

		MultipartFile multipartFile = multipartRequest.getFile(paramName);

		if (multipartFile != null && multipartFile.getName() != null) {
			String userUuid = UserContext.get().getUserUuid(true);
			String oldFileName = multipartFile.getOriginalFilename();
			Long size = multipartFile.getSize();
			// 如果配置为空代表不受任何限制
			if (fileTypeConfigVo != null) {
				boolean isAllowed = false;
				Long maxSize = 0L;
				String fileExt = oldFileName.substring(oldFileName.lastIndexOf(".") + 1).toLowerCase();
				JSONObject configObj = fileTypeConfigVo.getConfigObj();
				JSONArray whiteList = new JSONArray();
				JSONArray blackList = new JSONArray();
				if (size == 0) {
					throw new EmptyFileException();
				}
				if (configObj != null) {
					whiteList = configObj.getJSONArray("whiteList");
					blackList = configObj.getJSONArray("blackList");
					maxSize = configObj.getLongValue("maxSize");
				}
				if (whiteList != null && whiteList.size() > 0) {
					for (int i = 0; i < whiteList.size(); i++) {
						if (fileExt.equalsIgnoreCase(whiteList.getString(i))) {
							isAllowed = true;
							break;
						}
					}
				} else if (blackList != null && blackList.size() > 0) {
					isAllowed = true;
					for (int i = 0; i < blackList.size(); i++) {
						if (fileExt.equalsIgnoreCase(blackList.getString(i))) {
							isAllowed = false;
							break;
						}
					}
				} else {
					isAllowed = true;
				}
				if (!isAllowed) {
					throw new FileExtNotAllowedException(fileExt);
				}
				if (maxSize != null && maxSize > 0 && size > maxSize) {
					throw new FileTooLargeException(size, maxSize);
				}
			}

			IFileTypeHandler fileTypeHandler = FileTypeHandlerFactory.getHandler(type);
			if (fileTypeHandler == null) {
				throw new FileTypeHandlerNotFoundException(type);
			}

			FileVo fileVo = new FileVo();
			fileVo.setName(oldFileName);
			fileVo.setSize(size);
			fileVo.setUserUuid(userUuid);
			fileVo.setType(type);
			fileVo.setContentType(multipartFile.getContentType());
			String filePath = null;
			try {
				filePath = FileUtil.saveData(MinioFileSystemHandler.NAME, tenantUuid, multipartFile.getInputStream(), fileVo.getId(), fileVo.getContentType(), fileVo.getType());
			} catch (Exception ex) {
				// 如果minio出现异常，则上传到本地
				logger.error(ex.getMessage(), ex);
				filePath = FileUtil.saveData(LocalFileSystemHandler.NAME, tenantUuid, multipartFile.getInputStream(), fileVo.getId(), fileVo.getContentType(), fileVo.getType());
			}
			fileVo.setPath(filePath);
			fileMapper.insertFile(fileVo);
			fileTypeHandler.afterUpload(fileVo, paramObj);
			return fileMapper.getFileById(fileVo.getId());
		}
		return null;
	}

	public static void main(String[] arg) {
		System.out.println(RC4Util.decrypt(Config.RC4KEY, "1375737719d3"));
	}
}

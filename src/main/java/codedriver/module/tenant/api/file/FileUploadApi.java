package codedriver.module.tenant.api.file;

import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.config.Config;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.util.FileUtil;
import codedriver.framework.common.util.RC4Util;
import codedriver.framework.exception.user.NoTenantException;
import codedriver.framework.file.core.FileTypeHandlerFactory;
import codedriver.framework.file.core.IFileTypeHandler;
import codedriver.framework.file.core.LocalFileSystemHandler;
import codedriver.framework.file.dao.mapper.FileMapper;
import codedriver.framework.file.dto.FileTypeVo;
import codedriver.framework.file.dto.FileVo;
import codedriver.framework.minio.core.MinioManager;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.BinaryStreamApiComponentBase;
import codedriver.module.tenant.exception.file.EmptyFileException;
import codedriver.module.tenant.exception.file.FileExtNotAllowedException;
import codedriver.module.tenant.exception.file.FileTooLargeException;
import codedriver.module.tenant.exception.file.FileTypeHandlerNotFoundException;
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
public class FileUploadApi extends BinaryStreamApiComponentBase {
	static Logger logger = LoggerFactory.getLogger(FileUploadApi.class);
//	@Autowired
//	private FileSystem fileSystem;
	
	@Autowired
	private MinioManager minioManager;

	@Autowired
	private FileMapper fileMapper;

	@Override
	public String getToken() {
		return "file/upload";
	}

	@Override
	public String getName() {
		return "附件上传接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({ @Param(name = "param", type = ApiParamType.STRING, desc = "附件参数名称", isRequired = true),
			 @Param(name = "type", type = ApiParamType.STRING, desc = "附件类型", isRequired = true),
			 @Param(name = "storageMedium", type = ApiParamType.STRING, desc = "存储介质", isRequired = true)
	})
	@Output({ @Param(explode = FileVo.class) })
	@Description(desc = "附件上传接口")
	@Override
	public Object myDoService(JSONObject paramObj, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String tenantUuid = TenantContext.get().getTenantUuid();
		if (StringUtils.isBlank(tenantUuid)) {
			throw new NoTenantException();
		}
		MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
		String paramName = paramObj.getString("param");
		String type = paramObj.getString("type");
		//存储介质storageMediumHandler，以此决定存储到minio还是其他文件系统
		String storageMediumHandler = paramObj.getString("storageMediumHandler");
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
		// if (fileTypeConfigVo == null) {
		// throw new FileTypeConfigNotFoundException(type);
		// }

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
			try {
				/*//TODO 废弃Hadoop
				 * FsStatus fsStatus = fileSystem.getStatus(); String finalPath = "/" +
				 * tenantUuid + "/upload/" + type + "/" + fileVo.getId(); FSDataOutputStream fos
				 * = fileSystem.create(new Path(finalPath));
				 * IOUtils.copyLarge(multipartFile.getInputStream(), fos); fos.flush();
				 * fos.close(); fileVo.setPath("hdfs:" + finalPath);
				 */
//				SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmm");
//				String finalPath = "/"+tenantUuid + "/upload/" + type + "/" +format.format(new Date()) + "/" + fileVo.getId();
//				fileVo.setPath("minio:" + finalPath);
//				minioManager.saveData(tenantUuid,multipartFile,fileVo,format);
				FileUtil.saveData(storageMediumHandler,tenantUuid,multipartFile.getInputStream(),fileVo,multipartFile.getContentType());
			} catch (Exception ex) {
				//如果指定的存储介质出现异常，则上传到本地
				logger.error(ex.getMessage(),ex);
				FileUtil.saveData(LocalFileSystemHandler.NAME,tenantUuid,multipartFile.getInputStream(),fileVo,null);
			}

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

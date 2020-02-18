package codedriver.module.tenant.api.file;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.config.Config;
import codedriver.framework.file.core.FileTypeHandlerFactory;
import codedriver.framework.file.core.IFileTypeHandler;
import codedriver.framework.file.dto.FileTypeVo;
import codedriver.framework.file.dto.FileVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.BinaryStreamApiComponentBase;
import codedriver.module.tenant.dao.mapper.FileMapper;
import codedriver.module.tenant.exception.file.BelongNotFoundException;
import codedriver.module.tenant.exception.file.DirectoryCreateException;
import codedriver.module.tenant.exception.file.EmptyFileException;
import codedriver.module.tenant.exception.file.FileExtNotAllowedException;
import codedriver.module.tenant.exception.file.FileTooLargeException;
import codedriver.module.tenant.exception.file.FileTypeConfigNotFoundException;
import codedriver.module.tenant.exception.file.FileTypeHandlerNotFoundException;
import codedriver.module.tenant.exception.file.SavePathNotExistsException;

public class FileUploadApi_bak extends BinaryStreamApiComponentBase {

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

	@Input({
			@Param(name = "param",
					type = ApiParamType.STRING,
					desc = "附件参数名称",
					isRequired = true),
			@Param(name = "type",
					type = ApiParamType.STRING,
					desc = "附件类型",
					isRequired = true) })
	@Output({ @Param(explode = FileVo.class) })
	@Description(desc = "附件上传接口")
	@Override
	public Object myDoService(JSONObject paramObj, HttpServletRequest request, HttpServletResponse response) throws Exception {
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
			throw new BelongNotFoundException("附件归属：" + type + "不存在");
		}
		FileTypeVo fileTypeConfigVo = fileMapper.getFileTypeConfigByType(fileTypeVo.getName());
		if (fileTypeConfigVo == null) {
			throw new FileTypeConfigNotFoundException(type);
		}

		MultipartFile multipartFile = multipartRequest.getFile(paramName);

		JSONObject jsonObj = new JSONObject();
		if (multipartFile != null && multipartFile.getName() != null) {
			String userId = UserContext.get().getUserId();
			boolean isAllowed = false;
			Long maxSize = 0L;
			String savePath = "";
			String typeName = "";
			String forwardUrl = null;
			String oldFileName = multipartFile.getOriginalFilename();
			Long size = multipartFile.getSize();
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
				savePath = configObj.getString("savePath");
			}
			if (StringUtils.isBlank(savePath)) {
				throw new SavePathNotExistsException(type);
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

			IFileTypeHandler fileTypeHandler = FileTypeHandlerFactory.getHandler(type);
			if (fileTypeHandler == null) {
				throw new FileTypeHandlerNotFoundException(type);
			}

			if (!savePath.startsWith(File.separator)) {
				if (Config.DATA_HOME.endsWith(File.separator)) {
					savePath = Config.DATA_HOME + savePath;
				} else {
					savePath = Config.DATA_HOME + File.separator + savePath;
				}
			}

			SimpleDateFormat dateformat = new SimpleDateFormat("yyyy" + File.separator + "MM" + File.separator + "dd");
			String filePath = dateformat.format(new Date());
			String fileName = UUID.randomUUID() + "." + fileExt;
			if (!savePath.endsWith(File.separator)) {
				savePath = savePath + File.separator;
			}
			String finalPath = savePath + filePath;
			File dir = new File(finalPath);
			boolean flag = false;

			// if (dir.getAbsolutePath().startsWith(Config.DATA_HOME)) {
			if ((!dir.isDirectory()) || (!dir.exists())) {
				flag = dir.mkdirs();
			} else {
				flag = true;
			}
			// }
			if (flag) {
				File finalFile = new File(finalPath + File.separator + fileName);
				FileOutputStream fos = new FileOutputStream(finalFile);
				IOUtils.copyLarge(multipartFile.getInputStream(), fos);
				fos.flush();
				fos.close();
				FileVo fileVo = new FileVo();
				fileVo.setName(oldFileName);
				fileVo.setPath(finalFile.getAbsolutePath());
				fileVo.setSize(size);
				fileVo.setUserId(userId);
				fileVo.setType(type);
				fileMapper.insertFile(fileVo);
				fileTypeHandler.afterUpload(fileVo, paramObj);
				return fileMapper.getFileByUuid(fileVo.getUuid());
			} else {
				throw new DirectoryCreateException(finalPath);
			}
		}
		return null;
	}
}

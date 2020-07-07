package codedriver.module.tenant.api.file;

import java.io.File;
import java.io.FileOutputStream;
import java.net.ConnectException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FsStatus;
import org.apache.hadoop.fs.Path;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.config.Config;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.util.RC4Util;
import codedriver.framework.exception.user.NoTenantException;
import codedriver.framework.file.core.FileTypeHandlerFactory;
import codedriver.framework.file.core.IFileTypeHandler;
import codedriver.framework.file.dao.mapper.FileMapper;
import codedriver.framework.file.dto.FileTypeVo;
import codedriver.framework.file.dto.FileVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.BinaryStreamApiComponentBase;
import codedriver.module.tenant.exception.file.EmptyFileException;
import codedriver.module.tenant.exception.file.FileExtNotAllowedException;
import codedriver.module.tenant.exception.file.FileTooLargeException;
import codedriver.module.tenant.exception.file.FileTypeHandlerNotFoundException;

@Service
public class FileUploadApi extends BinaryStreamApiComponentBase {

	@Autowired
	private FileSystem fileSystem;

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

	@Input({ @Param(name = "param", type = ApiParamType.STRING, desc = "附件参数名称", isRequired = true), @Param(name = "type", type = ApiParamType.STRING, desc = "附件类型", isRequired = true) })
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
				FsStatus fsStatus = fileSystem.getStatus();
				String finalPath = "/" + tenantUuid + "/upload/" + type + "/" + fileVo.getId();
				FSDataOutputStream fos = fileSystem.create(new Path(finalPath));
				IOUtils.copyLarge(multipartFile.getInputStream(), fos);
				fos.flush();
				fos.close();
				fileVo.setPath("hdfs:" + finalPath);
			} catch (ConnectException ex) {
				// 如果hadoop不存在，改为本地模式
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
			fileTypeHandler.afterUpload(fileVo, paramObj);
			return fileMapper.getFileById(fileVo.getId());
		}
		return null;
	}

	public static void main(String[] arg) {
		System.out.println(RC4Util.decrypt(Config.RC4KEY, "1375737719d3"));
	}
}

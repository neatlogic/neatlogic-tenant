/*
 * Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package neatlogic.module.tenant.api.user;

import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.common.util.FileUtil;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dto.UserVo;
import neatlogic.framework.exception.user.NoTenantException;
import neatlogic.module.framework.file.handler.LocalFileSystemHandler;
import neatlogic.module.framework.file.handler.MinioFileSystemHandler;
import neatlogic.framework.file.dao.mapper.FileMapper;
import neatlogic.framework.file.dto.FileVo;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateBinaryStreamApiComponentBase;
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
		return "??????????????????";
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
	@Description(desc = "??????????????????")
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
					//??????minio?????????????????????????????????
					logger.error(ex.getMessage(),ex);
					filePath = FileUtil.saveData(LocalFileSystemHandler.NAME,tenantUuid,multipartFile.getInputStream(),fileVo.getId().toString(),fileVo.getContentType(),fileVo.getType());
				}
				fileVo.setPath(filePath);
				fileMapper.insertFile(fileVo);
				Long fileId = fileVo.getId();
				/** ?????????????????? */
				JSONObject userInfo = new JSONObject();
				userInfo.put("avatar","api/binary/image/download?id=" + fileId);
				user.setUserInfo(userInfo.toJSONString());
				userMapper.updateUserInfo(user);
				returnObj.put("uploaded", true);
				returnObj.put("url", "api/binary/image/download?id=" + fileId);

			} else {
				returnObj.put("uploaded", false);
				returnObj.put("error", "?????????????????????");
			}
		} catch (Exception ex) {
			returnObj.put("uploaded", false);
			returnObj.put("error", ex.getMessage());
		}
		return returnObj;

	}
}

/*
 *
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 */

package neatlogic.module.tenant.api.user;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.NoAuth;
import neatlogic.framework.common.util.FileUtil;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dto.UserVo;
import neatlogic.framework.exception.user.NoTenantException;
import neatlogic.framework.file.dao.mapper.FileMapper;
import neatlogic.framework.file.dto.FileVo;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.binarystream.PrivateBinaryStreamApiComponentBase;
import neatlogic.module.tenant.util.ImageFileSnapshot;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;

@Service
@Transactional
@AuthAction(action = NoAuth.class)
@OperationType(type = OperationTypeEnum.OPERATE)
public class UserAvatarUploadApi extends PrivateBinaryStreamApiComponentBase {
    //static Logger logger = LoggerFactory.getLogger(UserAvatarUploadApi.class);

    @Resource
    private UserMapper userMapper;
    @Resource
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

    /** 校验图片真实内容后保存，保留编辑器和头像上传的原有返回格式。 */
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
            if (multipartFile != null) {
                String oldFileName = multipartFile.getOriginalFilename();
                Long size = multipartFile.getSize();

                FileVo fileVo = new FileVo();
                fileVo.setName(oldFileName);
                fileVo.setSize(size);
                fileVo.setUserUuid(userUuid);
                fileVo.setType("image");
                String filePath;
                // 旧版裁剪器提交 Blob 时文件名固定为 blob，按真实内容识别后补齐安全文件名。
                try (InputStream input = multipartFile.getInputStream()) {
                    ImageFileSnapshot snapshot;
                    if ("blob".equals(oldFileName)) {
                        snapshot = ImageFileSnapshot.createFromContent(input);
                    } else {
                        snapshot = ImageFileSnapshot.create(input, oldFileName);
                    }
                    try (ImageFileSnapshot image = snapshot;
                         InputStream verified = image.openStream()) {
                        fileVo.setContentType(image.getContentType());
                        if ("blob".equals(oldFileName)) {
                            fileVo.setName("avatar." + image.getContentType().substring("image/".length()));
                        }
                        filePath = FileUtil.saveData(tenantUuid, verified, fileVo);
                    }
                }
                fileVo.setPath(filePath);
                fileMapper.insertFile(fileVo);
                Long fileId = fileVo.getId();
                /* 保存头像数据 */
                JSONObject userInfo = new JSONObject();
                userInfo.put("avatar", "api/binary/image/download?id=" + fileId);
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

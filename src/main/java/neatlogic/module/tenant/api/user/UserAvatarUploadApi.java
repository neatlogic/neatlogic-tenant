/*Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

package neatlogic.module.tenant.api.user;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
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
import neatlogic.framework.restful.core.privateapi.PrivateBinaryStreamApiComponentBase;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Service
@Transactional

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
                String filePath = FileUtil.saveData(tenantUuid, multipartFile.getInputStream(), fileVo);
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

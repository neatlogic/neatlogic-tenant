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

package neatlogic.module.tenant.api.file;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.common.util.FileUtil;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

@Service

@OperationType(type = OperationTypeEnum.CREATE)
public class UploadImageApi extends PrivateBinaryStreamApiComponentBase {
    //static Logger logger = LoggerFactory.getLogger(UploadImageApi.class);

    @Resource
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

    @Output({@Param(explode = FileVo.class)})
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
            if (multipartFile != null && Objects.requireNonNull(multipartFile.getContentType()).startsWith("image")) {
                String userUuid = UserContext.get().getUserUuid(true);
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

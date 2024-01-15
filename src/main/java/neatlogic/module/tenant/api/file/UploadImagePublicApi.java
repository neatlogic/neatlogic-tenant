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

package neatlogic.module.tenant.api.file;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.common.util.FileUtil;
import neatlogic.framework.exception.user.NoTenantException;
import neatlogic.framework.file.dao.mapper.FileMapper;
import neatlogic.framework.file.dto.FileVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.publicapi.PublicBinaryStreamApiComponentBase;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Service
@Deprecated
@OperationType(type = OperationTypeEnum.CREATE)
public class UploadImagePublicApi extends PublicBinaryStreamApiComponentBase {
    //static Logger logger = LoggerFactory.getLogger(UploadImagePublicApi.class);

    @Resource
    private FileMapper fileMapper;

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

    @Input({})
    @Output({@Param(explode = FileVo.class)})
    @Description(desc = "图片上传接口(供第三方使用)")
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
            if (multipartFile != null && multipartFile.getName() != null && multipartFile.getContentType().startsWith("image")) {
                String userUuid = UserContext.get().getUserUuid(true);
                String oldFileName = multipartFile.getOriginalFilename();
                Long size = multipartFile.getSize();

                FileVo fileVo = new FileVo();
                fileVo.setName(oldFileName);
                fileVo.setSize(size);
                fileVo.setUserUuid(userUuid);
                fileVo.setType("image");
                fileVo.setContentType(multipartFile.getContentType());
                String filePath = FileUtil.saveData(tenantUuid, multipartFile.getInputStream(), fileVo.getId().toString(), fileVo.getContentType(), fileVo.getType());
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

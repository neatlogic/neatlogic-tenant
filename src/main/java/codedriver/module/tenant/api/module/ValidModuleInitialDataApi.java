/*
 * Copyright(c) 2022 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.module;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.auth.label.MODULE_MODIFY;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.initialdata.core.InitialDataManager;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateBinaryStreamApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Service
@AuthAction(action = MODULE_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ValidModuleInitialDataApi extends PrivateBinaryStreamApiComponentBase {


    @Override
    public String getToken() {
        return "/module/data/valid";
    }

    @Override
    public String getName() {
        return "校验模块初始化数据";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "file", type = ApiParamType.FILE, isRequired = true, desc = "导入文件")})
    @Output({@Param(name = "Return", type = ApiParamType.STRING, desc = "数据表清单")})
    @Description(desc = "校验模块初始化数据接口")
    @Override
    public Object myDoService(JSONObject jsonObj, HttpServletRequest request, HttpServletResponse response) throws Exception {
        String moduleId = jsonObj.getString("moduleId");
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        MultipartFile multipartFile = multipartRequest.getFile("file");
        if (multipartFile != null) {
            return InitialDataManager.validData(multipartFile.getInputStream());
        }
        return null;
    }
}

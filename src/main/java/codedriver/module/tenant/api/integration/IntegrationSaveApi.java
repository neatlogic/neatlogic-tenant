/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.integration;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dto.FieldValidResultVo;
import codedriver.framework.exception.integration.HttpMethodNotFoundException;
import codedriver.framework.exception.integration.IntegrationHandlerNotFoundException;
import codedriver.framework.exception.integration.IntegrationNameRepeatsException;
import codedriver.framework.integration.authentication.enums.HttpMethod;
import codedriver.framework.integration.core.IntegrationHandlerFactory;
import codedriver.framework.integration.dao.mapper.IntegrationMapper;
import codedriver.framework.integration.dto.IntegrationVo;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.IValid;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.auth.label.INTERFACE_MODIFY;
import codedriver.module.tenant.exception.integration.IntegrationUrlIllegalException;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@AuthAction(action = INTERFACE_MODIFY.class)
@OperationType(type = OperationTypeEnum.CREATE)
public class IntegrationSaveApi extends PrivateApiComponentBase {

    @Resource
    private IntegrationMapper integrationMapper;

    @Override
    public String getToken() {
        return "integration/save";
    }

    @Override
    public String getName() {
        return "集成配置保存接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "uuid", type = ApiParamType.STRING, desc = "uuid，为空代表新增"),
            @Param(name = "name", type = ApiParamType.STRING, desc = "名称", isRequired = true, xss = true),
            @Param(name = "url", type = ApiParamType.REGEX, desc = "目标地址", isRequired = true, rule = "^((http|ftp|https)://)(([a-zA-Z0-9\\._-]+)|([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}))(:[0-9]{1,4})*(/[a-zA-Z0-9\\&%_\\./-~-]*)?"),
            @Param(name = "handler", type = ApiParamType.STRING, desc = "组件", isRequired = true, xss = true),
            @Param(name = "config", type = ApiParamType.JSONOBJECT, desc = "配置，json格式", isRequired = true)
    })
    @Description(desc = "集成配置保存接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        IntegrationVo integrationVo = JSONObject.toJavaObject(jsonObj, IntegrationVo.class);
        if (integrationMapper.checkNameIsRepeats(integrationVo) > 0) {
            throw new IntegrationNameRepeatsException(integrationVo.getName());
        }
        if (IntegrationHandlerFactory.getHandler(integrationVo.getHandler()) == null) {
            throw new IntegrationHandlerNotFoundException(integrationVo.getHandler());
        }
        if (HttpMethod.getHttpMethod(integrationVo.getMethod()) == null) {
            throw new HttpMethodNotFoundException(integrationVo.getMethod());
        }
        if (integrationVo.getUrl().contains("integration/run/")) {
            throw new IntegrationUrlIllegalException(integrationVo.getUrl());
        }
        if (StringUtils.isNotBlank(jsonObj.getString("uuid"))) {
            integrationMapper.updateIntegration(integrationVo);
        } else {
            integrationMapper.insertIntegration(integrationVo);
        }
        return null;
    }

    public IValid name() {
        return value -> {
            IntegrationVo integrationVo = JSONObject.toJavaObject(value, IntegrationVo.class);
            if (integrationMapper.checkNameIsRepeats(integrationVo) > 0) {
                return new FieldValidResultVo(new IntegrationNameRepeatsException(integrationVo.getName()));
            }
            return new FieldValidResultVo();
        };
    }
}

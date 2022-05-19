/*
 * Copyright(c) 2022 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.auditconfig;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.auth.label.ADMIN;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.auditconfig.dao.mapper.AuditConfigMapper;
import codedriver.framework.auditconfig.dto.AuditConfigVo;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
@AuthAction(action = ADMIN.class)
public class SaveAuditConfigApi extends PrivateApiComponentBase {

    @Resource
    private AuditConfigMapper auditConfigMapper;

    @Override
    public String getToken() {
        return "auditconfig/save";
    }

    @Override
    public String getName() {
        return "保存审计配置";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "name", type = ApiParamType.STRING, desc = "名称", isRequired = true),
            @Param(name = "config", type = ApiParamType.JSONOBJECT, desc = "配置", isRequired = true)})
    @Description(desc = "保存审计配置接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        AuditConfigVo auditConfigVo = JSONObject.toJavaObject(jsonObj, AuditConfigVo.class);
        auditConfigMapper.saveAuditConfig(auditConfigVo);
        return null;
    }

}

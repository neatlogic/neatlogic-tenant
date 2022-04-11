/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.apimanage;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.auth.label.INTERFACE_MODIFY;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.exception.type.ApiNotFoundException;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentFactory;
import codedriver.framework.restful.core.publicapi.PublicApiComponentFactory;
import codedriver.framework.restful.dao.mapper.ApiAuditMapper;
import codedriver.framework.restful.dao.mapper.ApiMapper;
import codedriver.framework.restful.dto.ApiVo;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@AuthAction(action = INTERFACE_MODIFY.class)
@OperationType(type = OperationTypeEnum.UPDATE)
public class ApiManageNeedAuditUpdateApi extends PrivateApiComponentBase {

    @Autowired
    private ApiMapper ApiMapper;

    @Autowired
    private ApiAuditMapper apiAuditMapper;

    @Override
    public String getToken() {
        return "apimanage/needaudit/udpate";
    }

    @Override
    public String getName() {
        return "接口启用审计接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "token", type = ApiParamType.REGEX, rule = "^[A-Za-z_\\{\\}\\d/]+$", isRequired = true, desc = "token")
    })
    @Description(desc = "接口启用审计接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String token = jsonObj.getString("token");
        ApiVo apiVo = ApiMapper.getApiByToken(token);
        if (apiVo != null) {
            apiAuditMapper.updateApiNeedAuditByToken(token);
            return 1 - apiVo.getNeedAudit();
        } else {
            ApiVo ramApiVo = PrivateApiComponentFactory.getApiByToken(apiVo.getToken());
            if (ramApiVo == null) {
                ramApiVo = PublicApiComponentFactory.getApiByToken(apiVo.getToken());
                if (ramApiVo == null) {
                    throw new ApiNotFoundException(token);
                }
            }
            ramApiVo.setNeedAudit(1);
            ApiMapper.replaceApi(ramApiVo);
            return 1;
        }
    }

}

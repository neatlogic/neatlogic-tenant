/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package neatlogic.module.tenant.api.apimanage;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.INTERFACE_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.exception.type.ApiNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentFactory;
import neatlogic.framework.restful.core.publicapi.PublicApiComponentFactory;
import neatlogic.framework.restful.dao.mapper.ApiAuditMapper;
import neatlogic.framework.restful.dao.mapper.ApiMapper;
import neatlogic.framework.restful.dto.ApiVo;
import neatlogic.framework.util.RegexUtils;
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
            @Param(name = "token", type = ApiParamType.REGEX, rule = RegexUtils.API_TOKEN, isRequired = true, desc = "token")
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

/*
Copyright(c) $today.year NeatLogic Co., Ltd. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License. 
 */

package neatlogic.module.tenant.api.apiaudit;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentFactory;
import neatlogic.framework.restful.core.publicapi.PublicApiComponentFactory;
import neatlogic.framework.restful.dao.mapper.ApiMapper;
import neatlogic.framework.restful.dto.ApiVo;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@OperationType(type = OperationTypeEnum.UPDATE)
public class ApiAuditEnableBatchUpdateApi extends PrivateApiComponentBase {

    @Resource
    private ApiMapper apiMapper;

    @Override
    public String getToken() {
        return "apiaudit/enable/batch/update";
    }

    @Override
    public String getName() {
        return "接口调用记录启用审计批量更新";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "needAudit", type = ApiParamType.INTEGER, isRequired = true, desc = "是否启用审计")
    })
    @Description(desc = "接口调用记录启用审计批量更新")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        Integer needAudit = paramObj.getInteger("needAudit");
        List<ApiVo> apiList = PrivateApiComponentFactory.getApiList();
        for (ApiVo apiVo : apiList) {
            apiVo.setNeedAudit(needAudit);
            apiMapper.insertOrUpdateNeedAuditApi(apiVo);
        }
        return null;
    }
}

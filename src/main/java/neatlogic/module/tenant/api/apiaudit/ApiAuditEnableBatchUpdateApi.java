/*Copyright (C) 2023  深圳极向量科技有限公司 All Rights Reserved.

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

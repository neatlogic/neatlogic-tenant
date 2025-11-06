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

package neatlogic.module.tenant.api.notify;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.NoAuth;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.notify.core.INotifyPolicyHandler;
import neatlogic.framework.notify.core.NotifyPolicyHandlerFactory;
import neatlogic.framework.notify.dao.mapper.NotifyMapper;
import neatlogic.framework.notify.dto.NotifyPolicyVo;
import neatlogic.framework.notify.exception.NotifyPolicyHandlerNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@AuthAction(action = NoAuth.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class GetDefaultNotifyPolicyApi extends PrivateApiComponentBase {

    @Resource
    private NotifyMapper notifyMapper;

    @Override
    public String getName() {
        return "nmtan.getdefaultnotifypolicyapi.getname";
    }

    @Input({
            @Param(name = "handler", type = ApiParamType.STRING, isRequired = true, desc = "common.handler"),
    })
    @Output({
            @Param(explode = NotifyPolicyVo.class, desc = "common.tbodylist")
    })
    @Description(desc = "nmtan.getdefaultnotifypolicyapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        String handler = paramObj.getString("handler");
        INotifyPolicyHandler notifyPolicyHandler = NotifyPolicyHandlerFactory.getHandler(handler);
        if (notifyPolicyHandler == null) {
            throw new NotifyPolicyHandlerNotFoundException(handler);
        }
        NotifyPolicyVo notifyPolicyVo = notifyMapper.getDefaultNotifyPolicyByHandler(notifyPolicyHandler.getClassName());
        return notifyPolicyVo;
    }

    @Override
    public String getToken() {
        return "notify/policy/default/get";
    }

    @Override
    public String getConfig() {
        return null;
    }
}

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
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.notify.core.INotifyPolicyHandler;
import neatlogic.framework.notify.core.NotifyPolicyHandlerFactory;
import neatlogic.framework.notify.dao.mapper.NotifyMapper;
import neatlogic.framework.notify.dto.NotifyPolicyVo;
import neatlogic.framework.notify.exception.NotifyPolicyHandlerNotFoundException;
import neatlogic.framework.notify.exception.NotifyPolicyNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Objects;

@Service
@Transactional
@OperationType(type = OperationTypeEnum.UPDATE)
public class UpdateIsDefaultNotifyPolicyApi extends PrivateApiComponentBase {

    @Resource
    private NotifyMapper notifyMapper;

    @Override
    public String getName() {
        return "设置默认通知策略";
    }

    @Input({
            @Param(name = "id", type = ApiParamType.LONG, desc = "策略id"),
            @Param(name = "handler", type = ApiParamType.STRING, isRequired = true, desc = "策略类型"),
    })
    @Output({})
    @Description(desc = "设置默认通知策略")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        String handler = paramObj.getString("handler");
        Long id = paramObj.getLong("id");
        if (id != null) {
            NotifyPolicyVo notifyPolicyVo = notifyMapper.getNotifyPolicyById(id);
            if (notifyPolicyVo == null) {
                throw new NotifyPolicyNotFoundException(id.toString());
            }
            if (Objects.equals(notifyPolicyVo.getIsDefault(), 1)) {
                return null;
            }
            handler = notifyPolicyVo.getHandler();
        }
        INotifyPolicyHandler notifyPolicyHandler = NotifyPolicyHandlerFactory.getHandler(handler);
        if (notifyPolicyHandler == null) {
            throw new NotifyPolicyHandlerNotFoundException(handler);
        }
        notifyMapper.resetNotifyPolicyIsDefaultByHandler(handler);
        if (id != null) {
            notifyMapper.updateNotifyPolicyIsDefaultById(id);
        }
        return null;
    }

    @Override
    public String getToken() {
        return "notify/policy/isdefault/update";
    }

    @Override
    public String getConfig() {
        return null;
    }
}

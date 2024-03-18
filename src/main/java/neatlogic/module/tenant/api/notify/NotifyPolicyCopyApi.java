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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.NOTIFY_POLICY_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dto.ConditionParamVo;
import neatlogic.framework.dto.FieldValidResultVo;
import neatlogic.framework.notify.core.INotifyPolicyHandler;
import neatlogic.framework.notify.core.NotifyPolicyHandlerFactory;
import neatlogic.framework.notify.dao.mapper.NotifyMapper;
import neatlogic.framework.notify.dto.NotifyPolicyConfigVo;
import neatlogic.framework.notify.dto.NotifyPolicyVo;
import neatlogic.framework.notify.exception.NotifyPolicyHandlerNotFoundException;
import neatlogic.framework.notify.exception.NotifyPolicyMoreThanOneException;
import neatlogic.framework.notify.exception.NotifyPolicyNameRepeatException;
import neatlogic.framework.notify.exception.NotifyPolicyNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.IValid;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.$;
import neatlogic.framework.util.RegexUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
@Transactional
@AuthAction(action = NOTIFY_POLICY_MODIFY.class)
@OperationType(type = OperationTypeEnum.CREATE)
public class NotifyPolicyCopyApi extends PrivateApiComponentBase {

    @Resource
    private NotifyMapper notifyMapper;

    @Override
    public String getToken() {
        return "notify/policy/copy";
    }

    @Override
    public String getName() {
        return "nmtan.notifypolicycopyapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "id", type = ApiParamType.LONG, isRequired = true, desc = "common.id"),
            @Param(name = "name", type = ApiParamType.REGEX, rule = RegexUtils.NAME, maxLength = 50, isRequired = true, desc = "common.name"),
    })
    @Output({
            @Param(explode = NotifyPolicyVo.class, desc = "common.tbodylist")
    })
    @Description(desc = "nmtan.notifypolicycopyapi.getname")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long id = jsonObj.getLong("id");
        NotifyPolicyVo notifyPolicyVo = notifyMapper.getNotifyPolicyById(id);
        if (notifyPolicyVo == null) {
            throw new NotifyPolicyNotFoundException(id.toString());
        }
        INotifyPolicyHandler notifyPolicyHandler = NotifyPolicyHandlerFactory.getHandler(notifyPolicyVo.getHandler());
        if (notifyPolicyHandler == null) {
            throw new NotifyPolicyHandlerNotFoundException(notifyPolicyVo.getHandler());
        }
        if (notifyPolicyHandler.isAllowMultiPolicy() == 0) {
            throw new NotifyPolicyMoreThanOneException($.t(notifyPolicyHandler.getName()));
        }
        String name = jsonObj.getString("name");
        notifyPolicyVo.setName(name);
        notifyPolicyVo.setId(null);
        notifyPolicyVo.setFcu(UserContext.get().getUserUuid(true));
        if (notifyMapper.checkNotifyPolicyNameIsRepeat(notifyPolicyVo) > 0) {
            throw new NotifyPolicyNameRepeatException(name);
        }
        notifyMapper.insertNotifyPolicy(notifyPolicyVo);
        NotifyPolicyConfigVo config = notifyPolicyVo.getConfig();
        List<ConditionParamVo> paramList = config.getParamList();
        paramList.addAll(notifyPolicyHandler.getSystemParamList());
        paramList.sort((e1, e2) -> e1.getName().compareToIgnoreCase(e2.getName()));
        return notifyPolicyVo;
    }

    public IValid name() {
        return value -> {
            NotifyPolicyVo notifyPolicyVo = JSON.toJavaObject(value, NotifyPolicyVo.class);
            notifyPolicyVo.setId(null);
            if (notifyMapper.checkNotifyPolicyNameIsRepeat(notifyPolicyVo) > 0) {
                return new FieldValidResultVo(new NotifyPolicyNameRepeatException(notifyPolicyVo.getName()));
            }
            return new FieldValidResultVo();
        };
    }

}

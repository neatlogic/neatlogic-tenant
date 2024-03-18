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

package neatlogic.module.tenant.api.wechat;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.NOTIFY_CONFIG_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dao.mapper.NotifyConfigMapper;
import neatlogic.framework.dto.WechatVo;
import neatlogic.framework.notify.core.NotifyHandlerType;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Component
@Transactional
@AuthAction(action = NOTIFY_CONFIG_MODIFY.class)
@OperationType(type = OperationTypeEnum.UPDATE)
public class SaveWechatApi extends PrivateApiComponentBase {

    @Resource
    private NotifyConfigMapper notifyConfigMapper;

    @Override
    public String getName() {
        return "nmtaw.savewechatapi.getname";
    }

    @Input({
            @Param(name = "corpId", type = ApiParamType.STRING, isRequired = true, desc = "term.framework.corpid"),
            @Param(name = "corpSecret", type = ApiParamType.STRING, isRequired = true, desc = "term.framework.corpsecret"),
            @Param(name = "agentId", type = ApiParamType.STRING, isRequired = true, desc = "term.framework.agentid")
    })
    @Output({})
    @Description(desc = "nmtaw.savewechatapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        WechatVo wechatVo = paramObj.toJavaObject(WechatVo.class);
        notifyConfigMapper.insertNotifyConfig(NotifyHandlerType.WECHAT.getValue(), JSONObject.toJSONString(wechatVo));
        return null;
    }

    @Override
    public String getToken() {
        return "wechat/save";
    }
}

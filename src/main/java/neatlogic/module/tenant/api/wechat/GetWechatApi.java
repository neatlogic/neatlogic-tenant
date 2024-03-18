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
import neatlogic.framework.dao.mapper.NotifyConfigMapper;
import neatlogic.framework.dto.WechatVo;
import neatlogic.framework.notify.core.NotifyHandlerType;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@AuthAction(action = NOTIFY_CONFIG_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class GetWechatApi extends PrivateApiComponentBase {

    @Resource
    private NotifyConfigMapper notifyConfigMapper;

    @Override
    public String getName() {
        return "nmtaw.getwechatapi.getname";
    }

    @Input({})
    @Output({
            @Param(explode = WechatVo.class)
    })
    @Description(desc = "nmtaw.getwechatapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        String config = notifyConfigMapper.getConfigByType(NotifyHandlerType.WECHAT.getValue());
        if (StringUtils.isBlank(config)) {
            return null;
        }
        WechatVo wechatVo = JSONObject.parseObject(config, WechatVo.class);
        return wechatVo;
    }

    @Override
    public String getToken() {
        return "wechat/get";
    }
}

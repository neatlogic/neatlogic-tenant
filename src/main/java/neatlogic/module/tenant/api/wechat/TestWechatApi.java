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
import neatlogic.framework.exception.wechat.WechatAuthenticationInformationNotFoundException;
import neatlogic.framework.notify.core.NotifyHandlerType;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.WechatUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@AuthAction(action = NOTIFY_CONFIG_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class TestWechatApi extends PrivateApiComponentBase {

    @Resource
    private NotifyConfigMapper notifyConfigMapper;

    @Override
    public String getName() {
        return "nmtaw.testwechatapi.getname";
    }

    @Input({
            @Param(name = "toUser", type = ApiParamType.STRING, isRequired = true, desc = "nmtaw.testwechatapi.input.param.desc.touser")
    })
    @Output({})
    @Description(desc = "nmtaw.testwechatapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        String config = notifyConfigMapper.getConfigByType(NotifyHandlerType.WECHAT.getValue());
        if (StringUtils.isBlank(config)) {
            throw new WechatAuthenticationInformationNotFoundException();
        }
        WechatVo wechatVo = JSONObject.parseObject(config, WechatVo.class);
        WechatUtil.AccessToken accessToken = WechatUtil.getAccessToken(wechatVo.getCorpId(), wechatVo.getCorpSecret());
        String toUser = paramObj.getString("toUser");
        JSONObject data = WechatUtil.getTextCardMsg(
                toUser ,
                "Test wechat",
                "Your enterprise wechat configuration is available!@link:https://open.weixin.qq.com/connect/oauth2/authorize?appid=wx8a4c400b4c54eead&redirect_uri=http://demo.neatlogic.cn:8011/demo/workDetail?processTaskId=${DATA.id}&isHandle=true&response_type=code&scope=snsapi_base&state=STATE#wechat_redirect",
                wechatVo.getCorpId()
        );
        WechatUtil.sendMessage(accessToken.getToken(), data, wechatVo.getAgentId());
        return null;
    }

    @Override
    public String getToken() {
        return "wechat/test";
    }
}

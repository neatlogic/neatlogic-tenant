/*
 *
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 */

package neatlogic.module.tenant.api.wechat;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.NOTIFY_CONFIG_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dto.WechatVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.WechatUtil;
import org.springframework.stereotype.Component;

@Component
@AuthAction(action = NOTIFY_CONFIG_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class TestWechatApi extends PrivateApiComponentBase {

//    @Resource
//    private NotifyConfigMapper notifyConfigMapper;

    @Override
    public String getName() {
        return "nmtaw.testwechatapi.getname";
    }

    @Input({
            @Param(name = "corpId", type = ApiParamType.STRING, isRequired = true, desc = "term.framework.corpid"),
            @Param(name = "corpSecret", type = ApiParamType.STRING, isRequired = true, desc = "term.framework.corpsecret"),
            @Param(name = "agentId", type = ApiParamType.STRING, isRequired = true, desc = "term.framework.agentid"),
            @Param(name = "toUser", type = ApiParamType.STRING, isRequired = true, desc = "nmtaw.testwechatapi.input.param.desc.touser")
    })
    @Output({})
    @Description(desc = "nmtaw.testwechatapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
//        String config = notifyConfigMapper.getConfigByType(NotifyHandlerType.WECHAT.getValue());
//        if (StringUtils.isBlank(config)) {
//            throw new WechatAuthenticationInformationNotFoundException();
//        }
//        WechatVo wechatVo = JSONObject.parseObject(config, WechatVo.class);
        WechatVo wechatVo = paramObj.toJavaObject(WechatVo.class);
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

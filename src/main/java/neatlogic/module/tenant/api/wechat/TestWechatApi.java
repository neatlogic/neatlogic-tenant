/*
 * Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package neatlogic.module.tenant.api.wechat;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.NOTIFY_CONFIG_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dao.mapper.WechatMapper;
import neatlogic.framework.dto.WechatVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.WechatUtil;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@AuthAction(action = NOTIFY_CONFIG_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class TestWechatApi extends PrivateApiComponentBase {

    @Resource
    private WechatMapper wechatMapper;

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
        String toUser = paramObj.getString("toUser");
        WechatVo wechatVo = wechatMapper.getWechat();
        WechatUtil.AccessToken accessToken = WechatUtil.getAccessToken(wechatVo.getCorpId(), wechatVo.getCorpSecret());
        JSONObject data = WechatUtil.getTextCardMsg(
                toUser ,
                "Test wechat",
                "Your enterprise wechat configuration is available!@link:https://open.weixin.qq.com/connect/oauth2/authorize?appid=wx8a4c400b4c54eead&redirect_uri=http://demo.techsure.cn:8011/demo/workDetail?processTaskId=${DATA.id}&isHandle=true&response_type=code&scope=snsapi_base&state=STATE#wechat_redirect",
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

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
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Component
@Transactional
@AuthAction(action = NOTIFY_CONFIG_MODIFY.class)
@OperationType(type = OperationTypeEnum.UPDATE)
public class SaveWechatApi extends PrivateApiComponentBase {

    @Resource
    private WechatMapper wechatMapper;

    @Override
    public String getName() {
        return "保存企业微信数据";
    }

    @Input({
            @Param(name = "corpId", type = ApiParamType.STRING, isRequired = true, desc = "企业ID"),
            @Param(name = "corpSecret", type = ApiParamType.STRING, isRequired = true, desc = "企业密钥"),
            @Param(name = "agentId", type = ApiParamType.STRING, isRequired = true, desc = "企业应用ID")
    })
    @Output({})
    @Description(desc = "保存企业微信数据")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        WechatVo wechatVo = paramObj.toJavaObject(WechatVo.class);
        wechatMapper.deleteWechat();
        wechatMapper.insertWechat(wechatVo);
        return null;
    }

    @Override
    public String getToken() {
        return "wechat/save";
    }
}

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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.NOTIFY_CONFIG_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dao.mapper.NotifyConfigMapper;
import neatlogic.framework.dto.NotifyConfigVo;
import neatlogic.framework.dto.WechatVo;
import neatlogic.framework.notify.core.NotifyHandlerType;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.SnowflakeUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

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
        Long id = null;
        List<NotifyConfigVo> notifyConfigList = notifyConfigMapper.getNotifyConfigListByType(NotifyHandlerType.WECHAT.getValue());
        if (CollectionUtils.isNotEmpty(notifyConfigList)) {
            id = notifyConfigList.get(0).getId();
        } else {
            id = SnowflakeUtil.uniqueLong();
        }
        WechatVo wechatVo = paramObj.toJavaObject(WechatVo.class);
        NotifyConfigVo notifyConfigVo = new NotifyConfigVo();
        notifyConfigVo.setId(id);
        notifyConfigVo.setIsActive(1);
        notifyConfigVo.setIsDefault(1);
        notifyConfigVo.setType(NotifyHandlerType.WECHAT.getValue());
        notifyConfigVo.setConfigStr(JSON.toJSONString(wechatVo));
        notifyConfigMapper.insertNotifyConfigVo(notifyConfigVo);
        return null;
    }

    @Override
    public String getToken() {
        return "wechat/save";
    }
}

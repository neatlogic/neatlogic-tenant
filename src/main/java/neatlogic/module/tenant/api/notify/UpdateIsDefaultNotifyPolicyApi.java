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
@AuthAction(action = NoAuth.class)
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

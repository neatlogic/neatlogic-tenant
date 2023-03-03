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

package neatlogic.module.tenant.api.notify;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.dependency.constvalue.FrameworkFromType;
import neatlogic.framework.dependency.core.DependencyManager;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;

import neatlogic.framework.auth.label.NOTIFY_POLICY_MODIFY;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.notify.dao.mapper.NotifyMapper;
import neatlogic.framework.notify.exception.NotifyPolicyReferencedCannotBeDeletedException;

import javax.annotation.Resource;

@Service
@Transactional
@AuthAction(action = NOTIFY_POLICY_MODIFY.class)
@OperationType(type = OperationTypeEnum.DELETE)
public class NotifyPolicyDeleteApi extends PrivateApiComponentBase {

    @Resource
    private NotifyMapper notifyMapper;

    @Override
    public String getToken() {
        return "notify/policy/delete";
    }

    @Override
    public String getName() {
        return "通知策略删除接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "id", type = ApiParamType.LONG, isRequired = true, desc = "策略id")
    })
    @Output({})
    @Description(desc = "通知策略删除接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long id = jsonObj.getLong("id");
        int count = DependencyManager.getDependencyCount(FrameworkFromType.NOTIFY_POLICY, id);
        if (count > 0) {
            throw new NotifyPolicyReferencedCannotBeDeletedException(id.toString());
        }
        notifyMapper.deleteNotifyPolicyById(id);
        return null;
    }

}

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

import com.alibaba.fastjson.JSONObject;
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
@OperationType(type = OperationTypeEnum.SEARCH)
public class UpdateIsDefaultNotifyPolicyApi extends PrivateApiComponentBase {

    @Resource
    private NotifyMapper notifyMapper;

    @Override
    public String getName() {
        return "设置默认通知策略";
    }

    @Input({
            @Param(name = "id", type = ApiParamType.LONG, isRequired = true, desc = "策略id")
    })
    @Output({})
    @Description(desc = "设置默认通知策略")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        Long id = paramObj.getLong("id");
        NotifyPolicyVo notifyPolicyVo = notifyMapper.getNotifyPolicyById(id);
        if (notifyPolicyVo == null) {
            throw new NotifyPolicyNotFoundException(id.toString());
        }
        if (Objects.equals(notifyPolicyVo.getIsDefault(), 1)) {
            return null;
        }
        INotifyPolicyHandler notifyPolicyHandler = NotifyPolicyHandlerFactory.getHandler(notifyPolicyVo.getHandler());
        if (notifyPolicyHandler == null) {
            throw new NotifyPolicyHandlerNotFoundException(notifyPolicyVo.getHandler());
        }
        notifyMapper.resetNotifyPolicyIsDefaultByHandler(notifyPolicyVo.getHandler());
        notifyMapper.updateNotifyPolicyIsDefaultById(id);
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

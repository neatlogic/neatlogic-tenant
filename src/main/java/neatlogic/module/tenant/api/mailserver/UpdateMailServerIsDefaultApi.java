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

package neatlogic.module.tenant.api.mailserver;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.NOTIFY_CONFIG_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dao.mapper.NotifyConfigMapper;
import neatlogic.framework.dto.NotifyConfigVo;
import neatlogic.framework.notify.core.NotifyHandlerType;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
@AuthAction(action = NOTIFY_CONFIG_MODIFY.class)
@OperationType(type = OperationTypeEnum.UPDATE)
public class UpdateMailServerIsDefaultApi extends PrivateApiComponentBase {

    @Resource
    private NotifyConfigMapper notifyConfigMapper;

    @Override
    public String getToken() {
        return "mailserver/isdefault/update";
    }

    @Override
    public String getName() {
        return "nmtam.updatemailserverisdefaultapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "id", type = ApiParamType.STRING, isRequired = true, desc = "common.id"),
            @Param(name = "isDefault", type = ApiParamType.ENUM, rule = "0,1", isRequired = true, desc = "common.isdefault"),
    })
    @Output({})
    @Description(desc = "nmtam.updatemailserverisdefaultapi.getname")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long id = jsonObj.getLong("id");
        Integer isDefault = jsonObj.getInteger("isDefault");
        if (Objects.equals(isDefault, 1)) {
            List<NotifyConfigVo> notifyConfigList = notifyConfigMapper.getNotifyConfigListByType(NotifyHandlerType.EMAIL.getValue());
            if (CollectionUtils.isNotEmpty(notifyConfigList)) {
                for (NotifyConfigVo notifyConfigVo : notifyConfigList) {
                    if (Objects.equals(notifyConfigVo.getIsDefault(), 1) && !Objects.equals(notifyConfigVo.getId(), id)) {
                        notifyConfigMapper.updateNotifyConfigIsDefault(notifyConfigVo.getId(), 0);
                    }
                }
            }
        }
        notifyConfigMapper.updateNotifyConfigIsDefault(id, isDefault);
        return null;
    }

}

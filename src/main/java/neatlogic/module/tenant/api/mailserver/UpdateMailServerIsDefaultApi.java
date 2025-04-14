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

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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.NOTIFY_CONFIG_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dao.mapper.NotifyConfigMapper;
import neatlogic.framework.dto.MailServerVo;
import neatlogic.framework.dto.NotifyConfigVo;
import neatlogic.framework.notify.core.NotifyHandlerType;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.RegexUtils;
import neatlogic.framework.util.SnowflakeUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@Transactional
@AuthAction(action = NOTIFY_CONFIG_MODIFY.class)
@OperationType(type = OperationTypeEnum.CREATE)
public class MailServerSaveApi extends PrivateApiComponentBase {

    @Resource
    private NotifyConfigMapper notifyConfigMapper;

    @Override
    public String getToken() {
        return "mailserver/save";
    }

    @Override
    public String getName() {
        return "nmtam.mailserversaveapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "id", type = ApiParamType.STRING, desc = "common.id"),
            @Param(name = "name", type = ApiParamType.REGEX, rule = RegexUtils.NAME, isRequired = true, maxLength = 50, desc = "common.name"),
            @Param(name = "host", type = ApiParamType.STRING, isRequired = true, maxLength = 50, desc = "term.framework.smpthost"),
            @Param(name = "port", type = ApiParamType.INTEGER, isRequired = true, desc = "term.framework.smptport"),
            @Param(name = "userName", type = ApiParamType.STRING, maxLength = 50, desc = "common.username"),
            @Param(name = "password", type = ApiParamType.STRING, maxLength = 50, desc = "common.password"),
            @Param(name = "homeUrl", type = ApiParamType.STRING, desc = "common.homeurl"),
            @Param(name = "fromAddress", type = ApiParamType.STRING, isRequired = true, maxLength = 50, desc = "common.mailaddress"),
            @Param(name = "sslEnable", type = ApiParamType.ENUM, rule = "true,false", isRequired = true, maxLength = 50, desc = "term.framework.smptsslenable"),
    })
    @Output({})
    @Description(desc = "nmtam.mailserversaveapi.getname")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        MailServerVo mailServerVo = jsonObj.toJavaObject(MailServerVo.class);
        NotifyConfigVo notifyConfigVo = new NotifyConfigVo();
        if (mailServerVo.getId() != null) {
            notifyConfigVo.setId(mailServerVo.getId());
        } else {
            notifyConfigVo.setId(SnowflakeUtil.uniqueLong());
        }
        notifyConfigVo.setName(mailServerVo.getName());
        notifyConfigVo.setIsActive(0);
        notifyConfigVo.setIsDefault(0);
        notifyConfigVo.setType(NotifyHandlerType.EMAIL.getValue());
        notifyConfigVo.setConfigStr(JSON.toJSONString(mailServerVo));
        notifyConfigMapper.insertNotifyConfigVo(notifyConfigVo);
        return null;
    }

}

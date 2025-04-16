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
import neatlogic.framework.dao.mapper.NotifyConfigMapper;
import neatlogic.framework.dto.MailServerVo;
import neatlogic.framework.dto.NotifyConfigVo;
import neatlogic.framework.notify.core.NotifyHandlerType;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.TableResultUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
@AuthAction(action = NOTIFY_CONFIG_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ListMailServerApi extends PrivateApiComponentBase {

    @Resource
    private NotifyConfigMapper notifyConfigMapper;

    @Override
    public String getToken() {
        return "mailserver/list";
    }

    @Override
    public String getName() {
        return "nmtam.listmailserverapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Output({
            @Param(name = "tbodyList", explode = MailServerVo[].class, desc = "common.tbodylist")
    })
    @Description(desc = "nmtam.listmailserverapi.getname")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        List<MailServerVo> tbodyList = new ArrayList<>();
        List<NotifyConfigVo> notifyConfigList = notifyConfigMapper.getNotifyConfigListByType(NotifyHandlerType.EMAIL.getValue());
        if (CollectionUtils.isNotEmpty(notifyConfigList)) {
            for (NotifyConfigVo notifyConfigVo : notifyConfigList) {
                Long id = notifyConfigVo.getId();
                String name = notifyConfigVo.getName();
                Integer isActive = notifyConfigVo.getIsActive();
                Integer isDefault = notifyConfigVo.getIsDefault();
                JSONObject config = notifyConfigVo.getConfig();
                String fromAddress = config.getString("fromAddress");
                String homeUrl = config.getString("homeUrl");
                String host = config.getString("host");
                if (StringUtils.isBlank(name)) {
                    name = config.getString("name");
                }
                String password = config.getString("password");
                Integer port = config.getInteger("port");
                String sslEnable = config.getString("sslEnable");
                String userName = config.getString("userName");
                MailServerVo mailServerVo = new MailServerVo();
                mailServerVo.setFromAddress(fromAddress);
                mailServerVo.setHomeUrl(homeUrl);
                mailServerVo.setHost(host);
                mailServerVo.setName(name);
                mailServerVo.setPassword(password);
                mailServerVo.setPort(port);
                mailServerVo.setSslEnable(sslEnable);
                mailServerVo.setUserName(userName);
                mailServerVo.setId(id);
                mailServerVo.setIsActive(isActive);
                mailServerVo.setIsDefault(isDefault);
                tbodyList.add(mailServerVo);
            }
        }
        return TableResultUtil.getResult(tbodyList);
    }

}

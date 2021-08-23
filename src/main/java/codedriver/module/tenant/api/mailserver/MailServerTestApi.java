package codedriver.module.tenant.api.mailserver;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dao.mapper.MailServerMapper;
import codedriver.framework.dto.MailServerVo;
import codedriver.framework.notify.core.NotifyHandlerFactory;
import codedriver.framework.notify.exception.EmailServerNotFoundException;
import codedriver.framework.notify.exception.NotifyHandlerNotFoundException;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.tenant.auth.label.MAIL_SERVER_MODIFY;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.mail.HtmlEmail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 测试邮件服务器能否正常发送邮件
 *
 * @author linbq
 * @since 2021/5/11 11:21
 **/
@Service
@AuthAction(action = MAIL_SERVER_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class MailServerTestApi extends PrivateApiComponentBase {

    @Autowired
    private MailServerMapper mailServerMapper;

    @Override
    public String getToken() {
        return "mailserver/test";
    }

    @Override
    public String getName() {
        return "测试邮件服务器能否正常发送邮件";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "emailAddress", type = ApiParamType.EMAIL, isRequired = true, desc = "邮箱地址")
    })
    @Output({@Param(name = "Return", type = ApiParamType.STRING, desc = "测试发送结果或异常信息")})
    @Description(desc = "测试邮件服务器能否正常发送邮件")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        if (NotifyHandlerFactory.getHandler("EmailNotifyHandler") == null) {
            throw new NotifyHandlerNotFoundException("EmailNotifyHandler");
        }
        MailServerVo mailServerVo = mailServerMapper.getActiveMailServer();
        if (mailServerVo == null || StringUtils.isBlank(mailServerVo.getHost()) || mailServerVo.getPort() == null) {
            throw new EmailServerNotFoundException();
        }
        HtmlEmail se = new HtmlEmail();
        se.addTo(jsonObj.getString("emailAddress"));
        se.setHostName(mailServerVo.getHost());
        se.setSmtpPort(mailServerVo.getPort());
        if (StringUtils.isNotBlank(mailServerVo.getUserName()) && StringUtils.isNotBlank(mailServerVo.getPassword())) {
            se.setAuthentication(mailServerVo.getUserName(), mailServerVo.getPassword());
        }
        if (StringUtils.isNotBlank(mailServerVo.getFromAddress())) {
            se.setFrom(mailServerVo.getFromAddress(), mailServerVo.getName());
        }
        se.setSubject("测试邮件");
        se.addPart("您配置的邮件服务器信息可用！", "text/html;charset=utf-8");
        se.send();
        return null;
    }

}

package codedriver.module.tenant.api.mailserver;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dao.mapper.MailServerMapper;
import codedriver.framework.notify.core.INotifyHandler;
import codedriver.framework.notify.core.NotifyHandlerFactory;
import codedriver.framework.notify.dto.NotifyVo;
import codedriver.framework.notify.exception.NotifyHandlerNotFoundException;
import codedriver.framework.notify.handler.EmailNotifyHandler;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.tenant.auth.label.MAIL_SERVER_MODIFY;
import com.alibaba.fastjson.JSONObject;
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
	@Description(desc = "邮件服务器信息删除接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		INotifyHandler handler = NotifyHandlerFactory.getHandler(EmailNotifyHandler.class.getName());
		if (handler == null) {
			throw new NotifyHandlerNotFoundException(EmailNotifyHandler.class.getName());
		}
		NotifyVo.Builder notifyBuilder = new NotifyVo.Builder(null);
		notifyBuilder.withTitleTemplate("测试邮件");
		notifyBuilder.withContentTemplate("您配置的邮件服务器信息可用！");
		notifyBuilder.addEmailAddress(jsonObj.getString("emailAddress"));
		NotifyVo notifyVo = notifyBuilder.build();
		return handler.execute(notifyVo);
	}

}

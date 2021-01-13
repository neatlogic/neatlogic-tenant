package codedriver.module.tenant.api.mailserver;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.restful.core.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.module.tenant.auth.label.MAIL_SERVER_MODIFY;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dao.mapper.MailServerMapper;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
@Service
@Transactional
@AuthAction(action = MAIL_SERVER_MODIFY.class)
@OperationType(type = OperationTypeEnum.DELETE)
public class MailServerDeleteApi extends PrivateApiComponentBase {

	@Autowired
	private MailServerMapper mailServerMapper;

	@Override
	public String getToken() {
		return "mailserver/delete";
	}

	@Override
	public String getName() {
		return "邮件服务器信息删除接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "uuid", type = ApiParamType.STRING, isRequired = true, desc = "邮件服务器uuid")
	})
	@Description(desc = "邮件服务器信息删除接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		mailServerMapper.deleteMailServerByUuid(jsonObj.getString("uuid"));
		return null;
	}

}

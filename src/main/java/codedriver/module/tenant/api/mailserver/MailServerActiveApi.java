package codedriver.module.tenant.api.mailserver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.dao.mapper.MailServerMapper;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.tenant.exception.mailserver.MailServerNotFoundException;
@Service
@Transactional
public class MailServerActiveApi extends ApiComponentBase {

	@Autowired
	private MailServerMapper mailServerMapper;

	@Override
	public String getToken() {
		return "mailserver/active";
	}

	@Override
	public String getName() {
		return "邮件服务器激活接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "uuid", type = ApiParamType.STRING, isRequired = true, desc = "邮件服务器uuid")
	})
	@Description(desc = "邮件服务器激活接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String uuid = jsonObj.getString("uuid");
		if(mailServerMapper.checkMailServerIsExists(uuid) == 0) {
			throw new MailServerNotFoundException(uuid);
		}
		mailServerMapper.resetAllMailServerStatus();
		mailServerMapper.activeMailServerByUuid(uuid);
		return null;
	}

}

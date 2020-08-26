package codedriver.module.tenant.api.mailserver;

import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dao.mapper.MailServerMapper;
import codedriver.framework.dto.MailServerVo;
import codedriver.module.tenant.exception.mailserver.MailServerNotFoundException;
@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class MailServerGetApi extends PrivateApiComponentBase {

	@Autowired
	private MailServerMapper mailServerMapper;

	@Override
	public String getToken() {
		return "mailserver/get";
	}

	@Override
	public String getName() {
		return "邮件服务器信息获取接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "uuid", type = ApiParamType.STRING, isRequired = true, desc = "邮件服务器uuid")
	})
	@Output({
		@Param(explode = MailServerVo.class, desc = "邮件服务器信息")
	})
	@Description(desc = "邮件服务器信息获取接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String uuid = jsonObj.getString("uuid");
		MailServerVo mailServerVo = mailServerMapper.getMailServerByUuid(uuid);
		if(mailServerVo == null) {
			throw new MailServerNotFoundException(uuid);
		}
		return mailServerVo;
	}

}

package codedriver.module.tenant.api.mailserver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dao.mapper.MailServerMapper;
import codedriver.framework.dto.MailServerVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.tenant.exception.mailserver.MailServerNameRepeatException;
import codedriver.module.tenant.exception.mailserver.MailServerNotFoundException;
@Service
@Transactional
public class MailServerSaveApi extends ApiComponentBase {

	@Autowired
	private MailServerMapper mailServerMapper;
	
	@Override
	public String getToken() {
		return "mailserver/save";
	}

	@Override
	public String getName() {
		return "邮件服务器信息保存接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "uuid", type = ApiParamType.STRING, desc = "uuid"),
		@Param(name = "name", type = ApiParamType.REGEX, rule = "^[A-Za-z_\\d\\u4e00-\\u9fa5]+$", isRequired= true, maxLength = 50, desc = "名称"),
		@Param(name = "host", type = ApiParamType.STRING, isRequired = true,  maxLength = 50, desc = "smtp主机"),
		@Param(name = "port", type = ApiParamType.INTEGER, isRequired = true, desc = "smtp端口"),
		@Param(name = "userName", type = ApiParamType.EMAIL, isRequired = true,  maxLength = 50, desc = "用户"),
		@Param(name = "password", type = ApiParamType.STRING, isRequired = true,  maxLength = 50, desc = "密码"),
		@Param(name = "domain", type = ApiParamType.STRING,  maxLength = 50, desc = "域名"),
		@Param(name = "fromAddress", type = ApiParamType.STRING, isRequired = true,  maxLength = 50, desc = "")
	})
	@Output({
		@Param(name = "Return", type = ApiParamType.STRING, desc = "邮件服务器uuid")
	})
	@Description(desc = "邮件服务器信息保存接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		MailServerVo mailServerVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<MailServerVo>() {});
		if(mailServerMapper.checkMailServerNameIsRepeat(mailServerVo) > 0) {
			throw new MailServerNameRepeatException(mailServerVo.getName());
		}
		
		String uuid = jsonObj.getString("uuid");
		if(uuid != null) {
			if(mailServerMapper.checkMailServerIsExists(uuid) == 0) {
				throw new MailServerNotFoundException(uuid);
			}
		}
		mailServerMapper.replaceMailServer(mailServerVo);		
		return mailServerVo.getUuid();
	}

}

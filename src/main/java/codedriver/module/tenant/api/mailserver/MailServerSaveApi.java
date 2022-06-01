/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.mailserver;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.auth.label.MAIL_SERVER_MODIFY;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dao.mapper.MailServerMapper;
import codedriver.framework.dto.MailServerVo;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.util.RegexUtils;
import codedriver.module.tenant.exception.mailserver.MailServerNameRepeatException;
import codedriver.module.tenant.exception.mailserver.MailServerNotFoundException;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
@Transactional
@AuthAction(action = MAIL_SERVER_MODIFY.class)
@OperationType(type = OperationTypeEnum.CREATE)
public class MailServerSaveApi extends PrivateApiComponentBase {

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
		@Param(name = "name", type = ApiParamType.REGEX, rule = RegexUtils.NAME, isRequired= true, maxLength = 50, desc = "名称"),
		@Param(name = "host", type = ApiParamType.STRING, isRequired = true,  maxLength = 50, desc = "smtp主机"),
		@Param(name = "port", type = ApiParamType.INTEGER, isRequired = true, desc = "smtp端口"),
		@Param(name = "userName", type = ApiParamType.STRING, isRequired = true,  maxLength = 50, desc = "用户"),
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

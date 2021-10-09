/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.mailserver;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.auth.label.MAIL_SERVER_MODIFY;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dao.mapper.MailServerMapper;
import codedriver.framework.dto.MailServerVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.tenant.exception.mailserver.MailServerNotFoundException;
//@Service
@Transactional
@AuthAction(action = MAIL_SERVER_MODIFY.class)
@OperationType(type = OperationTypeEnum.UPDATE)
public class MailServerStatusUpdateApi extends PrivateApiComponentBase {

	@Autowired
	private MailServerMapper mailServerMapper;

	@Override
	public String getToken() {
		return "mailserver/status/update";
	}

	@Override
	public String getName() {
		return "邮件服务器状态更新接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "uuid", type = ApiParamType.STRING, isRequired = true, desc = "邮件服务器uuid"),
		@Param(name = "isActive", type = ApiParamType.ENUM, rule = "0,1", isRequired = true, desc = "状态（0-禁用，1-启用）")
	})
	@Description(desc = "邮件服务器状态更新接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String uuid = jsonObj.getString("uuid");
		if(mailServerMapper.checkMailServerIsExists(uuid) == 0) {
			throw new MailServerNotFoundException(uuid);
		}
		int isActive = jsonObj.getIntValue("isActive");
		if(isActive == 1) {
//			mailServerMapper.resetAllMailServerStatus();
		}
		MailServerVo mailServerVo = new MailServerVo();
		mailServerVo.setUuid(uuid);
//		mailServerVo.setIsActive(isActive);
//		mailServerMapper.updateMailServerByUuid(mailServerVo);
		return null;
	}

}

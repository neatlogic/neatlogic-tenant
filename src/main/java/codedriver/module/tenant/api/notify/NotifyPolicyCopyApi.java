package codedriver.module.tenant.api.notify;

import java.util.List;

import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dto.ConditionParamVo;
import codedriver.framework.notify.core.INotifyPolicyHandler;
import codedriver.framework.notify.core.NotifyPolicyHandlerFactory;
import codedriver.framework.notify.dao.mapper.NotifyMapper;
import codedriver.framework.notify.dto.NotifyPolicyConfigVo;
import codedriver.framework.notify.dto.NotifyPolicyVo;
import codedriver.framework.notify.exception.NotifyPolicyHandlerNotFoundException;
import codedriver.framework.notify.exception.NotifyPolicyNameRepeatException;
import codedriver.framework.notify.exception.NotifyPolicyNotFoundException;
@Service
@Transactional
@OperationType(type = OperationTypeEnum.CREATE)
public class NotifyPolicyCopyApi extends PrivateApiComponentBase {
	
	@Autowired
	private NotifyMapper notifyMapper;

	@Override
	public String getToken() {
		return "notify/policy/copy";
	}

	@Override
	public String getName() {
		return "通知策略复制接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "id", type = ApiParamType.LONG, isRequired = true, desc = "策略id"),
		@Param(name = "name", type = ApiParamType.REGEX, rule = "^[A-Za-z_\\d\\u4e00-\\u9fa5]{1,50}$", isRequired = true, desc = "策略名"),
	})
	@Output({
		@Param(explode = NotifyPolicyVo.class, desc = "策略信息")
	})
	@Description(desc = "通知策略复制接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long id = jsonObj.getLong("id");
		NotifyPolicyVo notifyPolicyVo = notifyMapper.getNotifyPolicyById(id);
		if(notifyPolicyVo == null) {
			throw new NotifyPolicyNotFoundException(id.toString());
		}
		String name = jsonObj.getString("name");
		notifyPolicyVo.setName(name);
		notifyPolicyVo.setId(null);
		if(notifyMapper.checkNotifyPolicyNameIsRepeat(notifyPolicyVo) > 0) {
			throw new NotifyPolicyNameRepeatException(name);
		}
		notifyMapper.insertNotifyPolicy(notifyPolicyVo);
		NotifyPolicyConfigVo config = notifyPolicyVo.getConfig();
		List<ConditionParamVo> paramList = config.getParamList();
		INotifyPolicyHandler notifyPolicyHandler = NotifyPolicyHandlerFactory.getHandler(notifyPolicyVo.getHandler());
		if(notifyPolicyHandler == null) {
			throw new NotifyPolicyHandlerNotFoundException(notifyPolicyVo.getHandler());
		}
		paramList.addAll(notifyPolicyHandler.getSystemParamList());
		paramList.sort((e1, e2) -> e1.getName().compareToIgnoreCase(e2.getName()));
		return notifyPolicyVo;
	}

}

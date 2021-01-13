package codedriver.module.tenant.api.notify;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

import codedriver.module.tenant.auth.label.NOTIFY_POLICY_MODIFY;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.notify.dao.mapper.NotifyMapper;
import codedriver.framework.notify.exception.NotifyPolicyReferencedCannotBeDeletedException;
@Service
@Transactional
@AuthAction(action = NOTIFY_POLICY_MODIFY.class)
@OperationType(type = OperationTypeEnum.DELETE)
public class NotifyPolicyDeleteApi  extends PrivateApiComponentBase {
	
	@Autowired
	private NotifyMapper notifyMapper;

	@Override
	public String getToken() {
		return "notify/policy/delete";
	}

	@Override
	public String getName() {
		return "通知策略删除接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "id", type = ApiParamType.LONG, isRequired = true, desc = "策略id")
	})
	@Output({})
	@Description(desc = "通知策略删除接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long id = jsonObj.getLong("id");
		if(notifyMapper.getNotifyPolicyInvokerCountByPolicyId(id) > 0) {
			throw new NotifyPolicyReferencedCannotBeDeletedException(id.toString());
		}
		notifyMapper.deleteNotifyPolicyById(id);
		return null;
	}

}

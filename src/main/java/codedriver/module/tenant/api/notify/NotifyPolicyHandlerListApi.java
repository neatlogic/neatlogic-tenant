package codedriver.module.tenant.api.notify;

import codedriver.framework.auth.core.AuthActionChecker;
import codedriver.framework.notify.core.INotifyPolicyHandler;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.module.tenant.auth.label.NOTIFY_JOB_MODIFY;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.notify.core.NotifyPolicyHandlerFactory;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class NotifyPolicyHandlerListApi extends PrivateApiComponentBase {

	@Override
	public String getToken() {
		return "notify/policy/handler/list";
	}

	@Override
	public String getName() {
		return "通知策略分类列表接口";
	}

	@Override
	public String getConfig() {
		return null;
	}
	
	@Output({
		@Param(explode = ValueTextVo[].class, desc = "通知策略分类列表")
	})
	@Description(desc = "通知策略分类列表接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		List<ValueTextVo> list = new ArrayList<>(NotifyPolicyHandlerFactory.getNotifyPolicyHandlerList());
		Iterator<ValueTextVo> iterator = list.iterator();
		while (iterator.hasNext()){
			ValueTextVo next = iterator.next();
			INotifyPolicyHandler handler = NotifyPolicyHandlerFactory.getHandler(next.getValue().toString());
			/** 通知策略与权限绑定，例如没有流程管理权限则无法编辑流程及流程步骤通知策略 */
			if(!AuthActionChecker.check(handler.getAuthName())){
				iterator.remove();
			}
		}
		if(AuthActionChecker.check(NOTIFY_JOB_MODIFY.class.getSimpleName())){
			list.add(new ValueTextVo("定时任务","定时任务"));
		}
		return list;
	}

}

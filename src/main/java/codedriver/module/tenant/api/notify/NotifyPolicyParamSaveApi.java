package codedriver.module.tenant.api.notify;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.constvalue.ParamType;
import codedriver.framework.common.constvalue.Expression;
import codedriver.framework.common.constvalue.FormHandlerType;
import codedriver.framework.notify.dao.mapper.NotifyMapper;
import codedriver.framework.notify.dto.ExpressionVo;
import codedriver.framework.notify.dto.NotifyPolicyParamVo;
import codedriver.framework.notify.dto.NotifyPolicyVo;
import codedriver.framework.notify.exception.NotifyPolicyNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.IsActived;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
@Service
@Transactional
@IsActived
public class NotifyPolicyParamSaveApi extends ApiComponentBase {
	
	@Autowired
	private NotifyMapper notifyMapper;

	@Override
	public String getToken() {
		return "notify/policy/param/save";
	}

	@Override
	public String getName() {
		return "通知策略参数保存接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "policyId", type = ApiParamType.LONG, isRequired = true, desc = "策略id"),
		@Param(name = "handler", type = ApiParamType.STRING, isRequired = true, desc = "参数名"),
		@Param(name = "basicType", type = ApiParamType.ENUM, rule = "string,array,date", isRequired = true, desc = "参数类型"),
		@Param(name = "handlerName", type = ApiParamType.STRING, isRequired = true, desc = "参数描述")
	})
	@Output({
		@Param(explode = NotifyPolicyParamVo.class, desc = "参数信息")
	})
	@Description(desc = "通知策略参数保存接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long policyId = jsonObj.getLong("policyId");
		NotifyPolicyVo notifyPolicyVo = notifyMapper.getNotifyPolicyById(policyId);
		if(notifyPolicyVo == null) {
			throw new NotifyPolicyNotFoundException(policyId.toString());
		}
		String basicType = jsonObj.getString("basicType");
		ParamType basicTypeEnum = ParamType.getParamType(basicType);
		String handler = jsonObj.getString("handler");
		String handlerName = jsonObj.getString("handlerName");
		NotifyPolicyParamVo resultParamVo = null;
		boolean isNew = true;
		JSONObject config = notifyPolicyVo.getConfig();
		List<NotifyPolicyParamVo> paramList = JSON.parseArray(config.getJSONArray("paramList").toJSONString(), NotifyPolicyParamVo.class);
		for(NotifyPolicyParamVo notifyPolicyParamVo : paramList) {
			if(handler.equals(notifyPolicyParamVo.getHandler())) {
				notifyPolicyParamVo.setBasicType(basicType);
				notifyPolicyParamVo.setHandlerName(handlerName);
				notifyPolicyParamVo.setBasicTypeName(basicTypeEnum.getText());
				notifyPolicyParamVo.setDefaultExpression(basicTypeEnum.getDefaultExpression().getExpression());
				notifyPolicyParamVo.getExpressionList().clear();
				for(Expression expression : basicTypeEnum.getExpressionList()) {
					notifyPolicyParamVo.getExpressionList().add(new ExpressionVo(expression.getExpression(), expression.getExpressionName()));
				}
				isNew = false;
				resultParamVo = notifyPolicyParamVo;
			}
		}
		if(isNew) {
			NotifyPolicyParamVo notifyPolicyParamVo = new NotifyPolicyParamVo();
			notifyPolicyParamVo.setHandler(handler);
			notifyPolicyParamVo.setBasicType(basicType);
			notifyPolicyParamVo.setHandlerName(handlerName);
			notifyPolicyParamVo.setHandlerType(FormHandlerType.INPUT.toString());
			notifyPolicyParamVo.setType("custom");
			notifyPolicyParamVo.setBasicTypeName(basicTypeEnum.getText());
			notifyPolicyParamVo.setDefaultExpression(basicTypeEnum.getDefaultExpression().getExpression());
			for(Expression expression : basicTypeEnum.getExpressionList()) {
				notifyPolicyParamVo.getExpressionList().add(new ExpressionVo(expression.getExpression(), expression.getExpressionName()));
			}
			paramList.add(notifyPolicyParamVo);
			resultParamVo = notifyPolicyParamVo;
		}

		config.put("paramList", paramList);
		notifyPolicyVo.setConfig(config.toJSONString());
		notifyMapper.updateNotifyPolicyById(notifyPolicyVo);
		return resultParamVo;
	}

}

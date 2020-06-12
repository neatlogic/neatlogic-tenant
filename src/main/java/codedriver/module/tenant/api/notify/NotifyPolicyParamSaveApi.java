package codedriver.module.tenant.api.notify;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.constvalue.ParamType;
import codedriver.framework.dto.ConditionParamVo;
import codedriver.framework.common.constvalue.Expression;
import codedriver.framework.common.constvalue.FormHandlerType;
import codedriver.framework.notify.dao.mapper.NotifyMapper;
import codedriver.framework.notify.dto.ExpressionVo;
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
		@Param(name = "name", type = ApiParamType.STRING, isRequired = true, desc = "参数名"),
		@Param(name = "paramType", type = ApiParamType.ENUM, rule = "string,array,date", isRequired = true, desc = "参数类型"),
		@Param(name = "label", type = ApiParamType.STRING, isRequired = true, desc = "参数描述")
	})
	@Output({
		@Param(explode = ConditionParamVo.class, desc = "参数信息")
	})
	@Description(desc = "通知策略参数保存接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long policyId = jsonObj.getLong("policyId");
		NotifyPolicyVo notifyPolicyVo = notifyMapper.getNotifyPolicyById(policyId);
		if(notifyPolicyVo == null) {
			throw new NotifyPolicyNotFoundException(policyId.toString());
		}
		String paramType = jsonObj.getString("paramType");
		ParamType basicTypeEnum = ParamType.getParamType(paramType);
		String name = jsonObj.getString("name");
		String label = jsonObj.getString("label");
		ConditionParamVo resultParamVo = null;
		boolean isNew = true;
		JSONObject config = notifyPolicyVo.getConfig();
		List<ConditionParamVo> paramList = JSON.parseArray(config.getJSONArray("paramList").toJSONString(), ConditionParamVo.class);
		for(ConditionParamVo notifyPolicyParamVo : paramList) {
			if(name.equals(notifyPolicyParamVo.getName())) {
				notifyPolicyParamVo.setParamType(paramType);
				notifyPolicyParamVo.setLabel(label);
				notifyPolicyParamVo.setParamTypeName(basicTypeEnum.getText());
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
			ConditionParamVo notifyPolicyParamVo = new ConditionParamVo();
			notifyPolicyParamVo.setName(name);
			notifyPolicyParamVo.setParamType(paramType);
			notifyPolicyParamVo.setLabel(label);
			notifyPolicyParamVo.setController(FormHandlerType.INPUT.toString());
			notifyPolicyParamVo.setType("custom");
			notifyPolicyParamVo.setParamTypeName(basicTypeEnum.getText());
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

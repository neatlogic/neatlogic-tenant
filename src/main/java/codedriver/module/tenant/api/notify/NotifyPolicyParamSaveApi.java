package codedriver.module.tenant.api.notify;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.common.constvalue.BasicType;
import codedriver.framework.common.constvalue.Expression;
import codedriver.framework.common.constvalue.FormHandlerType;
import codedriver.framework.notify.core.INotifyPolicyHandler;
import codedriver.framework.notify.core.NotifyPolicyHandlerFactory;
import codedriver.framework.notify.dao.mapper.NotifyMapper;
import codedriver.framework.notify.dto.ExpressionVo;
import codedriver.framework.notify.dto.NotifyPolicyParamVo;
import codedriver.framework.notify.dto.NotifyPolicyVo;
import codedriver.framework.notify.exception.NotifyPolicyHandlerNotFoundException;
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
		@Param(name = "paramList", explode = NotifyPolicyParamVo[].class, desc = "参数列表")
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
		BasicType basicTypeEnum = BasicType.getBasicType(basicType);
		String handler = jsonObj.getString("handler");
		String handlerName = jsonObj.getString("handlerName");
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
			}
		}
		if(isNew) {
			NotifyPolicyParamVo notifyPolicyParamVo = new NotifyPolicyParamVo();
			notifyPolicyParamVo.setHandler(handler);
			notifyPolicyParamVo.setBasicType(basicType);
			notifyPolicyParamVo.setHandlerName(handlerName);
			notifyPolicyParamVo.setHandlerType(FormHandlerType.INPUT.toString());
			//notifyPolicyParamVo.setType(type);
			notifyPolicyParamVo.setBasicTypeName(basicTypeEnum.getText());
			notifyPolicyParamVo.setDefaultExpression(basicTypeEnum.getDefaultExpression().getExpression());
			for(Expression expression : basicTypeEnum.getExpressionList()) {
				notifyPolicyParamVo.getExpressionList().add(new ExpressionVo(expression.getExpression(), expression.getExpressionName()));
			}
			paramList.add(notifyPolicyParamVo);
		}

		config.put("paramList", paramList);
		notifyPolicyVo.setConfig(config.toJSONString());
		notifyMapper.updateNotifyPolicyById(notifyPolicyVo);
		JSONObject resultObj = new JSONObject();
		INotifyPolicyHandler notifyPolicyHandler = NotifyPolicyHandlerFactory.getHandler(notifyPolicyVo.getHandler());
		if(notifyPolicyHandler == null) {
			throw new NotifyPolicyHandlerNotFoundException(notifyPolicyVo.getHandler());
		}
		List<NotifyPolicyParamVo> systemParamList = notifyPolicyHandler.getSystemParamList();
		paramList.addAll(systemParamList);
		paramList.sort((e1, e2) -> e1.getHandler().compareToIgnoreCase(e2.getHandler()));
		resultObj.put("paramList", paramList);
		return resultObj;
	}

}

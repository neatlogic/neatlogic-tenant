package codedriver.module.tenant.api.expression;

import java.util.ArrayList;
import java.util.List;

import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.OperationType;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.Expression;
import codedriver.framework.dto.ExpressionVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class ExpressionListApi extends PrivateApiComponentBase {

	@Override
	public String getToken() {
		return "expression/list";
	}

	@Override
	public String getName() {
		return "表达式列表接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Output({
		@Param(explode = ExpressionVo[].class, desc = "表达式列表")
	})
	@Description(desc = "表达式列表接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		List<ExpressionVo> expressionList = new ArrayList<>();
		for(Expression expression : Expression.values()) {
			expressionList.add(new ExpressionVo(expression));
		}
		return expressionList;
	}

}

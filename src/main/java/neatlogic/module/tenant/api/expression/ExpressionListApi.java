/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package neatlogic.module.tenant.api.expression;

import java.util.ArrayList;
import java.util.List;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.OperationType;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import neatlogic.framework.common.constvalue.Expression;
import neatlogic.framework.dto.ExpressionVo;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;

@Deprecated
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

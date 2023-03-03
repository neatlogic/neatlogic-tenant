/*
 * Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

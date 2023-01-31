/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package neatlogic.module.tenant.api.apiaudit;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.dto.ValueTextVo;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;

import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Deprecated
@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class ApiOperationTypeListApi extends PrivateApiComponentBase {

	@Override
	public String getToken() {
		return "apiaudit/operationtype/list";
	}

	@Override
	public String getName() {
		return "获取API操作类型";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({})
	@Output({})
	@Description(desc = "获取API操作类型")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {

		List<ValueTextVo> resultList = new ArrayList<>();
		OperationTypeEnum[] operationTypeEnums = OperationTypeEnum.values();
		for(OperationTypeEnum type : operationTypeEnums){
			resultList.add(new ValueTextVo(type.getValue(),type.getText()));
		}

		return resultList;
	}
}

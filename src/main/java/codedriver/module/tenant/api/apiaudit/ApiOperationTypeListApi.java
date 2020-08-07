package codedriver.module.tenant.api.apiaudit;

import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.core.ApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class ApiOperationTypeListApi extends ApiComponentBase {

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

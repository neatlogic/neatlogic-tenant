package codedriver.module.tenant.api.matrix;

import java.util.ArrayList;
import java.util.List;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.auth.label.FRAMEWORK_BASE;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.matrix.constvalue.MatrixAttributeType;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
@Deprecated
@Service
@AuthAction(action = FRAMEWORK_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class MatrixAttributeTypeListApi extends PrivateApiComponentBase {

	@Override
	public String getToken() {
		return "matrix/attribute/type";
	}

	@Override
	public String getName() {
		return "矩阵属性类型列表接口";
	}

	@Override
	public String getConfig() {
		return null;
	}
	
	@Output({ @Param( name = "Return", desc = "矩阵属性类型列表", explode = ValueTextVo[].class)})
	@Description(desc = "矩阵属性类型列表接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		 List<ValueTextVo> typeList = new ArrayList<>();
		for(MatrixAttributeType type : MatrixAttributeType.values()) {
			typeList.add(new ValueTextVo(type.getValue(), type.getText()));
		}
		return typeList;
	}

}

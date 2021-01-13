package codedriver.module.tenant.api.matrix;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.matrix.constvalue.MatrixType;
import codedriver.framework.matrix.dao.mapper.MatrixAttributeMapper;
import codedriver.framework.matrix.dao.mapper.MatrixMapper;
import codedriver.framework.matrix.dto.MatrixAttributeVo;
import codedriver.framework.matrix.dto.MatrixVo;
import codedriver.framework.matrix.exception.MatrixNotFoundException;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.tenant.service.matrix.MatrixService;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class MatrixAttributeValueToTextBatchApi extends PrivateApiComponentBase {
	
	@Autowired
	private MatrixService matrixService;
	
	@Autowired
    private MatrixMapper matrixMapper;

    @Autowired
    private MatrixAttributeMapper attributeMapper;

	@Override
	public String getToken() {
		return "matrix/attribute/valuetotext/batch";
	}

	@Override
	public String getName() {
		return "批量将矩阵属性值转换成显示文本接口";
	}

	@Override
	public String getConfig() {
		return null;
	}
	
//	{
//		"matrixUuid":"f84ec0ec15be473bbd7ba148a9d4bbcc",
//		"attributeData":{
//			"4fc1508850fe4da4bda0028613053399":["1","2","3"],
//			"b694710424cf4176926c51b7c38bc803":["a","b","c"],
//			"0886a741675e4c4b956157aec7144a89":["x","y","z"]
//		}
//	}
	@Input({ 
		@Param(name = "matrixUuid", type = ApiParamType.STRING, isRequired = true, desc = "矩阵uuid"),
		@Param(name = "attributeData", type = ApiParamType.JSONOBJECT, isRequired = true, desc = "矩阵属性数据")
	})
    @Output({
    	@Param( name = "Return", type = ApiParamType.JSONOBJECT, desc = "矩阵属性数据")
    })
    @Description( desc = "批量将矩阵属性值转换成显示文本接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String matrixUuid = jsonObj.getString("matrixUuid");
		MatrixVo matrixVo =  matrixMapper.getMatrixByUuid(matrixUuid);
		if(matrixVo == null) {
			throw new MatrixNotFoundException(matrixUuid);
		}
		JSONObject attributeData = jsonObj.getJSONObject("attributeData");
		Map<String, Object> resultMap = new HashMap<>(attributeData.size());
		if (matrixVo.getType().equals(MatrixType.CUSTOM.getValue())){
			List<MatrixAttributeVo> attributeList = attributeMapper.getMatrixAttributeByMatrixUuid(matrixUuid);
	        if (MapUtils.isNotEmpty(attributeData) && CollectionUtils.isNotEmpty(attributeList)){
	        	Map<String, MatrixAttributeVo> processMatrixAttributeMap = new HashMap<>();
	        	for(MatrixAttributeVo processMatrixAttributeVo : attributeList) {
	        		processMatrixAttributeMap.put(processMatrixAttributeVo.getUuid(), processMatrixAttributeVo);
	        	}

	        	for(Entry<String, Object> entry : attributeData.entrySet()) {
	        		String attributeUuid = entry.getKey();        		
	        		List<String> attributeValueList = JSON.parseArray(entry.getValue().toString(), String.class);
	        		JSONArray attributeArray = new JSONArray(attributeValueList.size());
	        		for(String value : attributeValueList) {
						attributeArray.add(matrixService.matrixAttributeValueHandle(processMatrixAttributeMap.get(attributeUuid), value));
	        		}
	        		resultMap.put(attributeUuid, attributeArray);
	        	}
	        }
		}else {
        	for(Entry<String, Object> entry : attributeData.entrySet()) {
        		String attributeUuid = entry.getKey();        		
        		List<String> attributeValueList = JSON.parseArray(entry.getValue().toString(), String.class);
        		JSONArray attributeArray = new JSONArray(attributeValueList.size());
        		for(String value : attributeValueList) {
					attributeArray.add(matrixService.matrixAttributeValueHandle(value));
        		}
        		resultMap.put(attributeUuid, attributeArray);
        	}
		}		
		return resultMap;
	}

}

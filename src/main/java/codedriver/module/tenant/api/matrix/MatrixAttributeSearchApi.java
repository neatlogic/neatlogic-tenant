package codedriver.module.tenant.api.matrix;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONArray;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.exception.integration.IntegrationHandlerNotFoundException;
import codedriver.framework.integration.core.IIntegrationHandler;
import codedriver.framework.integration.core.IntegrationHandlerFactory;
import codedriver.framework.integration.dao.mapper.IntegrationMapper;
import codedriver.framework.integration.dto.IntegrationVo;
import codedriver.framework.matrix.constvalue.MatrixType;
import codedriver.framework.matrix.dao.mapper.MatrixAttributeMapper;
import codedriver.framework.matrix.dao.mapper.MatrixDataMapper;
import codedriver.framework.matrix.dao.mapper.MatrixExternalMapper;
import codedriver.framework.matrix.dao.mapper.MatrixMapper;
import codedriver.framework.matrix.dto.MatrixAttributeVo;
import codedriver.framework.matrix.dto.MatrixExternalVo;
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

/**
 * @program: codedriver
 * @description:
 * @create: 2020-03-26 19:06
 **/
@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class MatrixAttributeSearchApi extends PrivateApiComponentBase {

    @Autowired
    private MatrixService matrixService;

    @Autowired
    private MatrixAttributeMapper attributeMapper;

    @Autowired
    private MatrixDataMapper matrixDataMapper;

    @Autowired
    private MatrixMapper matrixMapper;

    @Autowired
    private MatrixExternalMapper matrixExternalMapper;
    
	@Autowired
	private IntegrationMapper integrationMapper;

    @Override
    public String getToken() {
        return "matrix/attribute/search";
    }

    @Override
    public String getName() {
        return "矩阵属性检索接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({ 
    	@Param(name = "matrixUuid", desc = "矩阵uuid", type = ApiParamType.STRING, isRequired = true)
    })
    @Output({
    	@Param(name = "processMatrixAttributeList", desc = "矩阵属性集合", explode = MatrixAttributeVo[].class),
    	@Param(name = "type", desc = "类型", type = ApiParamType.ENUM, rule = "custom,external")
    })
    @Description( desc = "矩阵属性检索接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
    	JSONObject resultObj = new JSONObject();
        String matrixUuid = jsonObj.getString("matrixUuid");
        MatrixVo matrixVo = matrixMapper.getMatrixByUuid(matrixUuid);
    	if(matrixVo == null) {
    		throw new MatrixNotFoundException(matrixUuid);
    	}
    	if (matrixVo.getType().equals(MatrixType.CUSTOM.getValue())){
    		resultObj.put("type", MatrixType.CUSTOM.getValue());
    		List<MatrixAttributeVo> processMatrixAttributeList = attributeMapper.getMatrixAttributeByMatrixUuid(matrixUuid);
        	if(CollectionUtils.isNotEmpty(processMatrixAttributeList)) {
        		List<String> attributeUuidList = processMatrixAttributeList.stream().map(MatrixAttributeVo :: getUuid).collect(Collectors.toList());
    			Map<String, Long> attributeDataCountMap = matrixDataMapper.checkMatrixAttributeHasDataByAttributeUuidList(matrixUuid, attributeUuidList);
        		for(MatrixAttributeVo processMatrixAttributeVo : processMatrixAttributeList) {
        			long count = attributeDataCountMap.get(processMatrixAttributeVo.getUuid());
        			processMatrixAttributeVo.setIsDeletable(count == 0 ? 1 : 0);
        		}
        	}
            resultObj.put("processMatrixAttributeList", processMatrixAttributeList);
    	}else {
    		resultObj.put("type", MatrixType.EXTERNAL.getValue());
    		MatrixExternalVo externalVo = matrixExternalMapper.getMatrixExternalByMatrixUuid(matrixUuid);
            if(externalVo == null) {
                resultObj.put("processMatrixAttributeList", new JSONArray());
                return resultObj;
            }
            IntegrationVo integrationVo = integrationMapper.getIntegrationByUuid(externalVo.getIntegrationUuid());
            if(integrationVo != null) {
            	IIntegrationHandler handler = IntegrationHandlerFactory.getHandler(integrationVo.getHandler());
        		if (handler == null) {
        			throw new IntegrationHandlerNotFoundException(integrationVo.getHandler());
        		}
        		resultObj.put("processMatrixAttributeList", matrixService.getExternalMatrixAttributeList(matrixUuid, integrationVo));       		
            }
    	}
    	return resultObj;
    }
}

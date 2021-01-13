package codedriver.module.tenant.api.matrix;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import codedriver.framework.auth.core.AuthAction;
import codedriver.module.tenant.auth.label.MATRIX_MODIFY;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.matrix.constvalue.MatrixAttributeType;
import codedriver.framework.matrix.constvalue.MatrixType;
import codedriver.framework.matrix.dao.mapper.MatrixAttributeMapper;
import codedriver.framework.matrix.dao.mapper.MatrixDataMapper;
import codedriver.framework.matrix.dao.mapper.MatrixMapper;
import codedriver.framework.matrix.dto.MatrixAttributeVo;
import codedriver.framework.matrix.dto.MatrixColumnVo;
import codedriver.framework.matrix.dto.MatrixVo;
import codedriver.framework.matrix.exception.MatrixAttributeNotFoundException;
import codedriver.framework.matrix.exception.MatrixExternalException;
import codedriver.framework.matrix.exception.MatrixNotFoundException;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.util.UuidUtil;

/**
 * @program: codedriver
 * @description:
 * @create: 2020-03-30 15:26
 **/
@Service
@Transactional
@AuthAction(action = MATRIX_MODIFY.class)
@OperationType(type = OperationTypeEnum.CREATE)
public class MatrixDataSaveApi extends PrivateApiComponentBase {

    @Autowired
    private MatrixMapper matrixMapper;
    
    @Autowired
    private MatrixAttributeMapper matrixAttributeMapper;

    @Autowired
    private MatrixDataMapper matrixDataMapper;

    @Override
    public String getToken() {
        return "matrix/data/save";
    }

    @Override
    public String getName() {
        return "矩阵数据保存接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({ 
    	@Param( name = "matrixUuid", desc = "矩阵uuid", type = ApiParamType.STRING, isRequired = true),
        @Param( name = "rowData", desc = "矩阵数据中的一行数据", type = ApiParamType.JSONOBJECT, isRequired = true)
    	
    })
    @Description(desc = "矩阵数据保存接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
    	String matrixUuid = jsonObj.getString("matrixUuid");
    	MatrixVo matrixVo = matrixMapper.getMatrixByUuid(matrixUuid);
		if(matrixVo == null) {
			throw new MatrixNotFoundException(matrixUuid);
		}
		if(MatrixType.CUSTOM.getValue().equals(matrixVo.getType())) {
			List<MatrixAttributeVo> attributeList = matrixAttributeMapper.getMatrixAttributeByMatrixUuid(matrixUuid);
	    	List<String> attributeUuidList = attributeList.stream().map(MatrixAttributeVo::getUuid).collect(Collectors.toList());
	    	JSONObject rowDataObj = jsonObj.getJSONObject("rowData");
	    	for(String columnUuid : rowDataObj.keySet()) {
	    		if(!"uuid".equals(columnUuid) && !"id".equals(columnUuid) && !attributeUuidList.contains(columnUuid)) {
	    			throw new MatrixAttributeNotFoundException(matrixUuid, columnUuid);
	    		}
	    	}

	    	boolean hasData = false;
	    	List<MatrixColumnVo> rowData = new ArrayList<>();
	    	for(MatrixAttributeVo processMatrixAttributeVo : attributeList) {
    			String value = rowDataObj.getString(processMatrixAttributeVo.getUuid());
    			if(StringUtils.isNotBlank(value)) {
    				hasData = true;
    				if(MatrixAttributeType.USER.getValue().equals(processMatrixAttributeVo.getType())) {
        				value = value.split("#")[1];
        			}else if(MatrixAttributeType.TEAM.getValue().equals(processMatrixAttributeVo.getType())) {
        				value = value.split("#")[1];
        			}else if(MatrixAttributeType.ROLE.getValue().equals(processMatrixAttributeVo.getType())) {
        				value = value.split("#")[1];
        			}
    			}
        		rowData.add(new MatrixColumnVo(processMatrixAttributeVo.getUuid(), value));
    		}
	    	String uuidValue = rowDataObj.getString("uuid");
	    	if(uuidValue == null) {
	    		if(hasData) {
		    		rowData.add(new MatrixColumnVo("uuid", UuidUtil.randomUuid()));    		
		    		matrixDataMapper.insertDynamicTableData(rowData, matrixUuid);    			
	    		}
	    	}else {
	    		if(hasData) {
		    		MatrixColumnVo uuidColumn = new MatrixColumnVo("uuid", uuidValue);
		    		matrixDataMapper.updateDynamicTableDataByUuid(rowData, uuidColumn, matrixUuid);    			
	    		}else {
	    			matrixDataMapper.deleteDynamicTableDataByUuid(matrixUuid, uuidValue);
	    		}
	    	}
		}else {
			throw new MatrixExternalException("矩阵外部数据源没有保存一行数据操作");
		}
    	
        return null;
    }
}

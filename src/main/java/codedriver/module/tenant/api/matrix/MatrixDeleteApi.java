package codedriver.module.tenant.api.matrix;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.matrix.constvalue.MatrixType;
import codedriver.framework.matrix.dao.mapper.MatrixAttributeMapper;
import codedriver.framework.matrix.dao.mapper.MatrixExternalMapper;
import codedriver.framework.matrix.dao.mapper.MatrixMapper;
import codedriver.framework.matrix.dto.MatrixVo;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

/**
 * @program: codedriver
 * @description:
 * @create: 2020-03-26 19:03
 **/
@Service
@Transactional
@OperationType(type = OperationTypeEnum.DELETE)
public class MatrixDeleteApi extends PrivateApiComponentBase {

    @Autowired
    private MatrixMapper matrixMapper;
    
    @Autowired
    private MatrixExternalMapper matrixExternalMapper;
    
    @Autowired
    private MatrixAttributeMapper matrixAttributeMapper;

    @Override
    public String getToken() {
        return "matrix/delete";
    }

    @Override
    public String getName() {
        return "矩阵删除接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({ @Param( name = "uuid", desc = "矩阵uuid", type = ApiParamType.STRING, isRequired = true)})
    @Description(desc = "矩阵删除接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
    	String uuid = jsonObj.getString("uuid");
    	MatrixVo matrixVo = matrixMapper.getMatrixByUuid(uuid);
    	if(matrixVo != null) {
        	matrixMapper.deleteMatrixByUuid(uuid);
        	if(MatrixType.CUSTOM.getValue().equals(matrixVo.getType())) {
        		matrixAttributeMapper.deleteAttributeByMatrixUuid(uuid);
        		matrixAttributeMapper.dropMatrixDynamicTable(uuid);
        	}else {
        		matrixExternalMapper.deleteMatrixExternalByMatrixUuid(uuid);
        	}
    	}
        return null;
    }
}

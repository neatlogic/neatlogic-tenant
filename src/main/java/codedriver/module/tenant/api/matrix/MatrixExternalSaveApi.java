package codedriver.module.tenant.api.matrix;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.matrix.constvalue.MatrixType;
import codedriver.framework.matrix.dao.mapper.MatrixExternalMapper;
import codedriver.framework.matrix.dao.mapper.MatrixMapper;
import codedriver.framework.matrix.dto.MatrixExternalVo;
import codedriver.framework.matrix.dto.MatrixVo;
import codedriver.framework.matrix.exception.MatrixExternalException;
import codedriver.framework.matrix.exception.MatrixNotFoundException;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.tenant.auth.label.MATRIX_MODIFY;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
@Transactional
@AuthAction(action = MATRIX_MODIFY.class)
@OperationType(type = OperationTypeEnum.CREATE)
public class MatrixExternalSaveApi extends PrivateApiComponentBase {

    @Autowired
    private MatrixExternalMapper externalMapper;

    @Autowired
    private MatrixMapper matrixMapper;

    @Override
    public String getToken() {
        return "matrix/external/save";
    }

    @Override
    public String getName() {
        return "外部数据源矩阵保存接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
    	@Param( name = "matrixUuid", type = ApiParamType.STRING, isRequired = true, desc = "矩阵uuid"),
    	@Param( name = "integrationUuid", type = ApiParamType.STRING, isRequired = true, desc = "集成设置uuid")
    })
    @Description(desc = "外部数据源矩阵保存接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        MatrixExternalVo externalVo = JSON.toJavaObject(jsonObj, MatrixExternalVo.class);
        MatrixVo matrixVo = matrixMapper.getMatrixByUuid(externalVo.getMatrixUuid());
        if(matrixVo == null) {
        	throw new MatrixNotFoundException(externalVo.getMatrixUuid());
        }
        
        if(MatrixType.EXTERNAL.getValue().equals(matrixVo.getType())) {
            if(externalMapper.getMatrixExternalIsExists(externalVo.getMatrixUuid()) == 0) {
            	externalMapper.insertMatrixExternal(externalVo);
            }else {
            	externalMapper.updateMatrixExternal(externalVo);
            }
        }else {
        	throw new MatrixExternalException("矩阵:'" + externalVo.getMatrixUuid() + "'不是外部数据源类型");
        }
        return null;
    }
}

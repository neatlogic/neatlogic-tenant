package codedriver.module.tenant.api.matrix;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dto.FieldValidResultVo;
import codedriver.framework.matrix.dao.mapper.MatrixMapper;
import codedriver.framework.matrix.dto.MatrixVo;
import codedriver.framework.matrix.exception.MatrixNameRepeatException;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.IValid;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.util.UuidUtil;
import codedriver.module.tenant.auth.label.MATRIX_MODIFY;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @program: codedriver
 * @description:
 * @create: 2020-03-26 19:02
 **/
@Service
@Transactional
@AuthAction(action = MATRIX_MODIFY.class)
@OperationType(type = OperationTypeEnum.CREATE)
public class MatrixSaveApi extends PrivateApiComponentBase {

    @Autowired
    private MatrixMapper matrixMapper;

    @Override
    public String getToken() {
        return "matrix/save";
    }

    @Override
    public String getName() {
        return "数据源矩阵保存";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({ @Param( name = "name", type = ApiParamType.STRING, desc = "矩阵名称", isRequired = true, xss = true),
             @Param( name = "type", type = ApiParamType.STRING, desc = "矩阵类型", isRequired = true),
             @Param( name = "uuid", type = ApiParamType.STRING, desc = "矩阵uuid")
    })
    @Output({
            @Param( name = "matrix", explode = MatrixVo.class, desc = "矩阵数据源")
    })
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject returnObj = new JSONObject();
        MatrixVo matrixVo = JSON.toJavaObject(jsonObj, MatrixVo.class);
        matrixVo.setLcu(UserContext.get().getUserUuid(true));
        if (StringUtils.isNotBlank(matrixVo.getUuid())){
        	if(matrixMapper.checkMatrixNameIsRepeat(matrixVo) > 0) {
        		throw new MatrixNameRepeatException(matrixVo.getName());
        	}
            matrixMapper.updateMatrixNameAndLcu(matrixVo);
        }else {
            matrixVo.setFcu(UserContext.get().getUserUuid(true));
            matrixVo.setUuid(UuidUtil.randomUuid());
            if(matrixMapper.checkMatrixNameIsRepeat(matrixVo) > 0) {
            	throw new MatrixNameRepeatException(matrixVo.getName());
        	}
            matrixMapper.insertMatrix(matrixVo);
        }
        returnObj.put("matrix", matrixVo);
        return returnObj;
    }

    public IValid name(){
        return value -> {
            MatrixVo matrixVo = JSON.toJavaObject(value, MatrixVo.class);
            if(StringUtils.isBlank(matrixVo.getUuid())){
                matrixVo.setUuid(UuidUtil.randomUuid());
            }
            if(matrixMapper.checkMatrixNameIsRepeat(matrixVo) > 0) {
                return new FieldValidResultVo(new MatrixNameRepeatException(matrixVo.getName()));
            }
            return new FieldValidResultVo();
        };
    }
}

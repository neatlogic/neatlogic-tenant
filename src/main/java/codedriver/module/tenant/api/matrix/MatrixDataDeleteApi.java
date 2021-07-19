package codedriver.module.tenant.api.matrix;

import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.matrix.constvalue.MatrixType;
import codedriver.framework.matrix.dao.mapper.MatrixDataMapper;
import codedriver.framework.matrix.dao.mapper.MatrixMapper;
import codedriver.framework.matrix.dto.MatrixVo;
import codedriver.framework.matrix.exception.MatrixExternalDeleteDataException;
import codedriver.framework.matrix.exception.MatrixExternalException;
import codedriver.framework.matrix.exception.MatrixNotFoundException;
import codedriver.framework.matrix.exception.MatrixViewDeleteDataException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.tenant.auth.label.MATRIX_MODIFY;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * @program: codedriver
 * @description:
 * @create: 2020-03-30 16:29
 **/
@Service
@Transactional
@AuthAction(action = MATRIX_MODIFY.class)
@OperationType(type = OperationTypeEnum.DELETE)
public class MatrixDataDeleteApi extends PrivateApiComponentBase {

    @Resource
    private MatrixDataMapper dataMapper;

    @Resource
    private MatrixMapper matrixMapper;

    @Override
    public String getToken() {
        return "matrix/data/delete";
    }

    @Override
    public String getName() {
        return "矩阵数据删除接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "matrixUuid", desc = "矩阵uuid", type = ApiParamType.STRING, isRequired = true),
            @Param(name = "uuidList", desc = "矩阵数据uuid列表", type = ApiParamType.JSONARRAY, isRequired = true)})
    @Description(desc = "矩阵数据删除接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String matrixUuid = jsonObj.getString("matrixUuid");
        MatrixVo matrixVo = matrixMapper.getMatrixByUuid(matrixUuid);
        if (matrixVo == null) {
            throw new MatrixNotFoundException(matrixUuid);
        }
        if (MatrixType.CUSTOM.getValue().equals(matrixVo.getType())) {
            List<String> uuidList = JSON.parseArray(jsonObj.getString("uuidList"), String.class);
            for (String uuid : uuidList) {
                dataMapper.deleteDynamicTableDataByUuid(matrixUuid, uuid, TenantContext.get().getTenantUuid());
            }
        } else if (MatrixType.EXTERNAL.getValue().equals(matrixVo.getType())) {
            throw new MatrixExternalDeleteDataException();
        } else if (MatrixType.VIEW.getValue().equals(matrixVo.getType())) {
            throw new MatrixViewDeleteDataException();
        }

        return null;
    }
}

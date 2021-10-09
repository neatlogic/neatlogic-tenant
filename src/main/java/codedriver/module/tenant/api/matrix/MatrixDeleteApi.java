/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.matrix;

import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dao.mapper.SchemaMapper;
import codedriver.framework.dependency.constvalue.CalleeType;
import codedriver.framework.dependency.core.DependencyManager;
import codedriver.framework.matrix.constvalue.MatrixType;
import codedriver.framework.matrix.dao.mapper.MatrixAttributeMapper;
import codedriver.framework.matrix.dao.mapper.MatrixExternalMapper;
import codedriver.framework.matrix.dao.mapper.MatrixMapper;
import codedriver.framework.matrix.dao.mapper.MatrixViewMapper;
import codedriver.framework.matrix.dto.MatrixVo;
import codedriver.framework.matrix.exception.MatrixReferencedCannotBeDeletedException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.auth.label.MATRIX_MODIFY;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * @program: codedriver
 * @description:
 * @create: 2020-03-26 19:03
 **/
@Service
@Transactional
@AuthAction(action = MATRIX_MODIFY.class)
@OperationType(type = OperationTypeEnum.DELETE)
public class MatrixDeleteApi extends PrivateApiComponentBase {

    @Resource
    private MatrixMapper matrixMapper;

    @Resource
    private MatrixExternalMapper matrixExternalMapper;

    @Resource
    private MatrixViewMapper matrixViewMapper;

    @Resource
    private MatrixAttributeMapper matrixAttributeMapper;

    @Resource
    private SchemaMapper schemaMapper;

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

    @Input({@Param(name = "uuid", desc = "矩阵uuid", type = ApiParamType.STRING, isRequired = true)})
    @Description(desc = "矩阵删除接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String uuid = jsonObj.getString("uuid");
        MatrixVo matrixVo = matrixMapper.getMatrixByUuid(uuid);
        if (matrixVo != null) {
            if (DependencyManager.getDependencyCount(CalleeType.MATRIX, uuid) > 0) {
                throw new MatrixReferencedCannotBeDeletedException(uuid);
            }
            matrixMapper.deleteMatrixByUuid(uuid);
            if (MatrixType.CUSTOM.getValue().equals(matrixVo.getType())) {
                matrixAttributeMapper.deleteAttributeByMatrixUuid(uuid);
                matrixAttributeMapper.dropMatrixDynamicTable(uuid, TenantContext.get().getDataDbName());
            } else if (MatrixType.EXTERNAL.getValue().equals(matrixVo.getType())) {
                matrixExternalMapper.deleteMatrixExternalByMatrixUuid(uuid);
            } else if (MatrixType.VIEW.getValue().equals(matrixVo.getType())) {
                matrixViewMapper.deleteMatrixViewByMatrixUuid(uuid);
                schemaMapper.deleteView(TenantContext.get().getDataDbName() + ".matrix_" + uuid);
            }
        }
        return null;
    }
}

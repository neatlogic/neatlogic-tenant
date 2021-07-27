/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.matrix;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.matrix.constvalue.MatrixType;
import codedriver.framework.matrix.dao.mapper.MatrixExternalMapper;
import codedriver.framework.matrix.dao.mapper.MatrixMapper;
import codedriver.framework.matrix.dto.MatrixExternalVo;
import codedriver.framework.matrix.dto.MatrixVo;
import codedriver.framework.matrix.exception.MatrixExternalNotFoundException;
import codedriver.framework.matrix.exception.MatrixNotFoundException;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @program: codedriver
 * @description:
 * @create: 2020-04-03 19:06
 **/
@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class MatrixExternalGetApi extends PrivateApiComponentBase {

    @Resource
    private MatrixExternalMapper externalMapper;

    @Resource
    private MatrixMapper matrixMapper;

    @Override
    public String getToken() {
        return "matrix/external/get";
    }

    @Override
    public String getName() {
        return "外部数据源矩阵获取接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "matrixUuid", desc = "矩阵uuid", isRequired = true, type = ApiParamType.STRING)})
    @Description(desc = "外部数据源矩阵获取接口")
    @Output({@Param(name = "Return", explode = MatrixExternalVo.class, desc = "外部矩阵数据源")})
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String matrixUuid = jsonObj.getString("matrixUuid");
        MatrixVo matrixVo = matrixMapper.getMatrixByUuid(matrixUuid);
        if (matrixVo == null) {
            throw new MatrixNotFoundException(matrixUuid);
        }

        if (MatrixType.EXTERNAL.getValue().equals(matrixVo.getType())) {
            return externalMapper.getMatrixExternalByMatrixUuid(matrixUuid);
        } else {
            throw new MatrixExternalNotFoundException(matrixVo.getName());
        }
    }
}

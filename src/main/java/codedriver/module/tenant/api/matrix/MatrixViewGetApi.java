/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.matrix;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.matrix.core.IMatrixDataSourceHandler;
import codedriver.framework.matrix.core.MatrixDataSourceHandlerFactory;
import codedriver.framework.matrix.dao.mapper.MatrixMapper;
import codedriver.framework.matrix.dto.MatrixViewVo;
import codedriver.framework.matrix.dto.MatrixVo;
import codedriver.framework.matrix.exception.MatrixDataSourceHandlerNotFoundException;
import codedriver.framework.matrix.exception.MatrixNotFoundException;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
@Deprecated
@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class MatrixViewGetApi extends PrivateApiComponentBase {

    @Resource
    private MatrixMapper matrixMapper;

    @Override
    public String getToken() {
        return "matrix/view/get";
    }

    @Override
    public String getName() {
        return "视图矩阵获取接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "matrixUuid", desc = "矩阵uuid", isRequired = true, type = ApiParamType.STRING)
    })
    @Output({
            @Param(name = "Return", explode = MatrixViewVo.class, desc = "视图矩阵数据源")
    })
    @Description(desc = "视图矩阵获取接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String matrixUuid = jsonObj.getString("matrixUuid");
        MatrixVo matrixVo = matrixMapper.getMatrixByUuid(matrixUuid);
        if (matrixVo == null) {
            throw new MatrixNotFoundException(matrixUuid);
        }
        IMatrixDataSourceHandler matrixDataSourceHandler = MatrixDataSourceHandlerFactory.getHandler(matrixVo.getType());
        if (matrixDataSourceHandler == null) {
            throw new MatrixDataSourceHandlerNotFoundException(matrixVo.getType());
        }
        return matrixDataSourceHandler.getMatrix(matrixVo.getUuid());

//        if (!MatrixType.VIEW.getValue().equals(matrixVo.getType())) {
//            throw new MatrixViewNotFoundException(matrixVo.getName());
//        }
//        MatrixViewVo matrixViewVo = viewMapper.getMatrixViewByMatrixUuid(matrixUuid);
//        if (matrixViewVo == null) {
//            throw new MatrixViewNotFoundException(matrixVo.getName());
//        }
//        Long fileId = matrixViewVo.getFileId();
//        FileVo fileVo = fileMapper.getFileById(fileId);
//        if (fileVo == null) {
//            throw new FileNotFoundException(fileId);
//        }
//        matrixViewVo.setFileVo(fileVo);
//        return matrixViewVo;
    }
}

/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.matrix;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dto.FieldValidResultVo;
import codedriver.framework.exception.type.ParamNotExistsException;
import codedriver.framework.matrix.constvalue.MatrixType;
import codedriver.framework.matrix.core.IMatrixDataSourceHandler;
import codedriver.framework.matrix.core.MatrixDataSourceHandlerFactory;
import codedriver.framework.matrix.core.MatrixPrivateDataSourceHandlerFactory;
import codedriver.framework.matrix.dao.mapper.*;
import codedriver.framework.matrix.dto.MatrixAttributeVo;
import codedriver.framework.matrix.dto.MatrixExternalVo;
import codedriver.framework.matrix.dto.MatrixVo;
import codedriver.framework.matrix.exception.MatrixDataSourceHandlerNotFoundException;
import codedriver.framework.matrix.exception.MatrixExternalNotFoundException;
import codedriver.framework.matrix.exception.MatrixNotFoundException;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.IValid;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @program: codedriver
 * @description:
 * @create: 2020-03-26 19:06
 **/
@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class MatrixAttributeSearchApi extends PrivateApiComponentBase {

    @Resource
    private MatrixMapper matrixMapper;

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

    @Override
    public boolean disableReturnCircularReferenceDetect() {
        return true;
    }
    @Input({
            @Param(name = "matrixUuid", desc = "矩阵uuid", type = ApiParamType.STRING),
            @Param(name = "type", desc = "类型", type = ApiParamType.ENUM, rule = "custom,external,view,cmdbci,fixed"),
            @Param(name = "ciId", type = ApiParamType.LONG, desc = "ci模型id")
    })
    @Output({
            @Param(name = "tbodyList", desc = "矩阵属性集合", explode = MatrixAttributeVo[].class),
            @Param(name = "type", desc = "类型", type = ApiParamType.ENUM, rule = "custom,external,view,cmdbci,fixed")
    })
    @Description(desc = "矩阵属性检索接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject resultObj = new JSONObject();
        MatrixVo matrixVo = null;
        String matrixUuid = jsonObj.getString("matrixUuid");
        if (StringUtils.isNotBlank(matrixUuid)) {
            matrixVo = MatrixPrivateDataSourceHandlerFactory.getMatrixVo(matrixUuid);
            if (matrixVo == null) {
                matrixVo = matrixMapper.getMatrixByUuid(matrixUuid);
                if (matrixVo == null) {
                    throw new MatrixNotFoundException(matrixUuid);
                }
            }
        } else {
            String type = jsonObj.getString("type");
            if (StringUtils.isBlank(type)) {
                throw new ParamNotExistsException("matrixUuid", "type");
            }
            matrixVo = jsonObj.toJavaObject(MatrixVo.class);
        }
        String type = matrixVo.getType();
        resultObj.put("type", type);
        IMatrixDataSourceHandler matrixDataSourceHandler = MatrixDataSourceHandlerFactory.getHandler(type);
        if (matrixDataSourceHandler == null) {
            throw new MatrixDataSourceHandlerNotFoundException(type);
        }
        List<MatrixAttributeVo> matrixAttributeList = matrixDataSourceHandler.getAttributeList(matrixVo);
        resultObj.put("tbodyList", matrixAttributeList);
        return resultObj;
    }

    /**
     * 校验矩阵的外部数据源是否存在
     **/
    public IValid matrixUuid() {
        return value -> {
            String matrixUuid = value.getString("matrixUuid");
            MatrixVo matrixVo = MatrixPrivateDataSourceHandlerFactory.getMatrixVo(matrixUuid);
            if (matrixVo == null) {
                matrixVo = matrixMapper.getMatrixByUuid(matrixUuid);
            }
            if (MatrixType.EXTERNAL.getValue().equals(matrixVo.getType())) {
                MatrixExternalVo externalVo = matrixMapper.getMatrixExternalByMatrixUuid(matrixUuid);
                if (externalVo == null) {
                    return new FieldValidResultVo(new MatrixExternalNotFoundException(matrixVo.getName()));
                }
            }
            return new FieldValidResultVo();
        };
    }
}

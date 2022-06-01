/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.matrix;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dto.FieldValidResultVo;
import codedriver.framework.matrix.core.IMatrixDataSourceHandler;
import codedriver.framework.matrix.core.MatrixDataSourceHandlerFactory;
import codedriver.framework.matrix.dao.mapper.MatrixMapper;
import codedriver.framework.matrix.dto.MatrixVo;
import codedriver.framework.matrix.exception.*;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.IValid;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.util.RegexUtils;
import codedriver.framework.util.UuidUtil;
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
@OperationType(type = OperationTypeEnum.CREATE)
public class MatrixCopyApi extends PrivateApiComponentBase {

    @Resource
    private MatrixMapper matrixMapper;

    @Override
    public String getToken() {
        return "matrix/copy";
    }

    @Override
    public String getName() {
        return "矩阵数据源复制接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "uuid", desc = "矩阵数据源uuid", isRequired = true, type = ApiParamType.STRING),
            @Param(name = "name", desc = "矩阵名称", isRequired = true, type = ApiParamType.STRING),
            @Param(name = "label", type = ApiParamType.REGEX, rule = RegexUtils.ENGLISH_NAME, desc = "矩阵唯一标识", isRequired = true, xss = true)
    })
    @Description(desc = "矩阵数据源复制接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String uuid = jsonObj.getString("uuid");
        MatrixVo matrixVo = matrixMapper.getMatrixByUuid(uuid);
        if (matrixVo == null) {
            throw new MatrixNotFoundException(uuid);
        }
        String targetMatrixUuid = UuidUtil.randomUuid();
        while (matrixMapper.checkMatrixIsExists(targetMatrixUuid) > 0) {
            targetMatrixUuid = UuidUtil.randomUuid();
        }
        matrixVo.setUuid(targetMatrixUuid);
        String name = jsonObj.getString("name");
        //判断name是否存在
        matrixVo.setName(name);
        if (matrixMapper.checkMatrixNameIsRepeat(matrixVo) > 0) {
            throw new MatrixNameRepeatException(name);
        }
        String label = jsonObj.getString("label");
        matrixVo.setLabel(label);
        if (matrixMapper.checkMatrixLabelIsRepeat(matrixVo) > 0) {
            throw new MatrixLabelRepeatException(label);
        }
        IMatrixDataSourceHandler matrixDataSourceHandler = MatrixDataSourceHandlerFactory.getHandler(matrixVo.getType());
        if (matrixDataSourceHandler == null) {
            throw new MatrixDataSourceHandlerNotFoundException(matrixVo.getType());
        }
        matrixDataSourceHandler.copyMatrix(uuid, matrixVo);
        return null;
    }

    public IValid name() {
        return value -> {
            MatrixVo matrixVo = JSONObject.toJavaObject(value, MatrixVo.class);
            matrixVo.setUuid(UuidUtil.randomUuid());
            if (matrixMapper.checkMatrixNameIsRepeat(matrixVo) > 0) {
                return new FieldValidResultVo(new MatrixNameRepeatException(matrixVo.getName()));
            }
            return new FieldValidResultVo();
        };
    }
}

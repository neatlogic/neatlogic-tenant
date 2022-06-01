/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.matrix;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.auth.label.MATRIX_MODIFY;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dto.FieldValidResultVo;
import codedriver.framework.matrix.dao.mapper.MatrixMapper;
import codedriver.framework.matrix.dto.MatrixVo;
import codedriver.framework.matrix.exception.MatrixNameRepeatException;
import codedriver.framework.matrix.exception.MatrixNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.IValid;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.util.RegexUtils;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * @program: codedriver
 * @description:
 * @create: 2020-03-27 17:49
 **/
@Service
@Transactional
@AuthAction(action = MATRIX_MODIFY.class)
@OperationType(type = OperationTypeEnum.UPDATE)
public class MatrixNameUpdateApi extends PrivateApiComponentBase {

    @Resource
    private MatrixMapper matrixMapper;

    @Override
    public String getToken() {
        return "matrix/name/update";
    }

    @Override
    public String getName() {
        return "矩阵名称变更接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "name", desc = "矩阵名称", type = ApiParamType.REGEX, rule = RegexUtils.NAME, isRequired = true, maxLength = 50),
            @Param(name = "uuid", desc = "uuid", type = ApiParamType.STRING, isRequired = true)
    })
    @Description(desc = "矩阵名称变更接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        MatrixVo matrixVo = JSONObject.toJavaObject(jsonObj, MatrixVo.class);
        if (matrixMapper.checkMatrixIsExists(matrixVo.getUuid()) == 0) {
            throw new MatrixNotFoundException(matrixVo.getUuid());
        }
        if (matrixMapper.checkMatrixNameIsRepeat(matrixVo) > 0) {
            throw new MatrixNameRepeatException(matrixVo.getName());
        }
        matrixVo.setLcu(UserContext.get().getUserUuid(true));
        matrixMapper.updateMatrixNameAndLcu(matrixVo);
        return null;
    }

    public IValid name() {
        return value -> {
            MatrixVo matrixVo = JSONObject.toJavaObject(value, MatrixVo.class);
            if (matrixMapper.checkMatrixNameIsRepeat(matrixVo) > 0) {
                return new FieldValidResultVo(new MatrixNameRepeatException(matrixVo.getName()));
            }
            return new FieldValidResultVo();
        };
    }
}

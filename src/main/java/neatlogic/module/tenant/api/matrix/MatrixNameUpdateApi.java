/*
 * Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package neatlogic.module.tenant.api.matrix;

import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.MATRIX_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dto.FieldValidResultVo;
import neatlogic.framework.matrix.dao.mapper.MatrixMapper;
import neatlogic.framework.matrix.dto.MatrixVo;
import neatlogic.framework.matrix.exception.MatrixNameRepeatException;
import neatlogic.framework.matrix.exception.MatrixNotFoundException;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.IValid;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.RegexUtils;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * @program: neatlogic
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

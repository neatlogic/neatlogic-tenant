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

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dto.FieldValidResultVo;
import neatlogic.framework.exception.type.ParamNotExistsException;
import neatlogic.framework.matrix.core.IMatrixDataSourceHandler;
import neatlogic.framework.matrix.core.MatrixDataSourceHandlerFactory;
import neatlogic.framework.matrix.dao.mapper.MatrixMapper;
import neatlogic.framework.matrix.dto.*;
import neatlogic.framework.matrix.exception.*;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.IValid;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.RegexUtils;
import neatlogic.framework.util.UuidUtil;
import neatlogic.framework.auth.label.MATRIX_MODIFY;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * @program: neatlogic
 * @description:
 * @create: 2020-03-26 19:02
 **/
@Service
@Transactional
@AuthAction(action = MATRIX_MODIFY.class)
@OperationType(type = OperationTypeEnum.CREATE)
public class MatrixSaveApi extends PrivateApiComponentBase {

    @Resource
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

    @Input({
            @Param(name = "name", type = ApiParamType.STRING, desc = "矩阵名称", xss = true),
            @Param(name = "label", type = ApiParamType.REGEX, rule = RegexUtils.ENGLISH_NAME, desc = "矩阵唯一标识", xss = true),
            @Param(name = "type", type = ApiParamType.ENUM, rule = "custom,external,view,cmdbci", desc = "矩阵类型"),
            @Param(name = "uuid", type = ApiParamType.STRING, desc = "矩阵uuid"),
            @Param(name = "integrationUuid", type = ApiParamType.STRING, desc = "集成设置uuid"),
            @Param(name = "fileId", type = ApiParamType.LONG, desc = "视图配置文件id"),
            @Param(name = "ciId", type = ApiParamType.LONG, desc = "ci模型id"),
            @Param(name = "config", type = ApiParamType.JSONOBJECT, desc = "配置信息")
    })
    @Output({
            @Param(name = "matrix", explode = MatrixVo.class, desc = "矩阵数据源")
    })
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject returnObj = new JSONObject();
        MatrixVo matrixVo = JSONObject.toJavaObject(jsonObj, MatrixVo.class);
        if (StringUtils.isBlank(matrixVo.getUuid())) {
            matrixVo.setUuid(UuidUtil.randomUuid());
            if (StringUtils.isBlank(matrixVo.getLabel())) {
                throw new ParamNotExistsException("label");
            }
            if (StringUtils.isBlank(matrixVo.getName())) {
                throw new ParamNotExistsException("name");
            }
            if (StringUtils.isBlank(matrixVo.getType())) {
                throw new ParamNotExistsException("type");
            }
            if (matrixMapper.checkMatrixLabelIsRepeat(matrixVo) > 0) {
                throw new MatrixLabelRepeatException(matrixVo.getLabel());
            }
            if (matrixMapper.checkMatrixNameIsRepeat(matrixVo) > 0) {
                throw new MatrixNameRepeatException(matrixVo.getName());
            }
        } else {
            MatrixVo oldMatrix = matrixMapper.getMatrixByUuid(matrixVo.getUuid());
            if (oldMatrix == null) {
                throw new MatrixNotFoundException(matrixVo.getUuid());
            }
            matrixVo.setName(oldMatrix.getName());
            matrixVo.setType(oldMatrix.getType());
        }

        IMatrixDataSourceHandler matrixDataSourceHandler = MatrixDataSourceHandlerFactory.getHandler(matrixVo.getType());
        if (matrixDataSourceHandler == null) {
            throw new MatrixDataSourceHandlerNotFoundException(matrixVo.getType());
        }
        matrixDataSourceHandler.saveMatrix(matrixVo);
        returnObj.put("matrix", matrixVo);
        return returnObj;
    }

    public IValid name() {
        return value -> {
            MatrixVo matrixVo = JSONObject.toJavaObject(value, MatrixVo.class);
            if (StringUtils.isBlank(matrixVo.getUuid())) {
                matrixVo.setUuid(UuidUtil.randomUuid());
            }
            if (matrixMapper.checkMatrixNameIsRepeat(matrixVo) > 0) {
                return new FieldValidResultVo(new MatrixNameRepeatException(matrixVo.getName()));
            }
            return new FieldValidResultVo();
        };
    }

    public IValid label() {
        return value -> {
            MatrixVo matrixVo = JSONObject.toJavaObject(value, MatrixVo.class);
            if (StringUtils.isBlank(matrixVo.getUuid())) {
                matrixVo.setUuid(UuidUtil.randomUuid());
            }
            if (matrixMapper.checkMatrixLabelIsRepeat(matrixVo) > 0) {
                return new FieldValidResultVo(new MatrixLabelRepeatException(matrixVo.getLabel()));
            }
            return new FieldValidResultVo();
        };
    }

//    public IValid integrationUuid(){
//        return value -> {
//            IntegrationVo integrationVo = integrationMapper.getIntegrationByUuid(value.getString("integrationUuid"));
//            IIntegrationHandler handler = IntegrationHandlerFactory.getHandler(integrationVo.getHandler());
//            if (handler == null) {
//                return new FieldValidResultVo(new IntegrationHandlerNotFoundException(integrationVo.getHandler()));
//            }
//            IntegrationResultVo resultVo = handler.sendRequest(integrationVo, FrameworkRequestFrom.TEST);
//            if(StringUtils.isNotBlank(resultVo.getError())){
//                return new FieldValidResultVo(new MatrixExternalAccessException());
//            }
//            try{
//                handler.validate(resultVo);
//            }catch (ApiRuntimeException ex){
//                return new FieldValidResultVo(new ApiRuntimeException(ex.getMessage()));
//            }
//            return new FieldValidResultVo();
//        };
//    }

}

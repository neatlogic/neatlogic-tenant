/*
 *
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 */

package neatlogic.module.tenant.api.matrix;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.MATRIX_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dto.FieldValidResultVo;
import neatlogic.framework.exception.type.ParamNotExistsException;
import neatlogic.framework.matrix.core.IMatrixDataSourceHandler;
import neatlogic.framework.matrix.core.MatrixDataSourceHandlerFactory;
import neatlogic.framework.matrix.dao.mapper.MatrixMapper;
import neatlogic.framework.matrix.dto.MatrixVo;
import neatlogic.framework.matrix.exception.MatrixDataSourceHandlerNotFoundException;
import neatlogic.framework.matrix.exception.MatrixLabelRepeatException;
import neatlogic.framework.matrix.exception.MatrixNameRepeatException;
import neatlogic.framework.matrix.exception.MatrixNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.IValid;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.RegexUtils;
import neatlogic.framework.util.UuidUtil;
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
        return "nmtam.matrixsaveapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "name", type = ApiParamType.STRING, desc = "common.name", xss = true),
            @Param(name = "label", type = ApiParamType.REGEX, rule = RegexUtils.ENGLISH_NAME, desc = "common.uniquename", xss = true),
            @Param(name = "type", type = ApiParamType.ENUM, rule = "custom,external,view,cmdbci,cmdbcustomview", desc = "common.type"),
            @Param(name = "uuid", type = ApiParamType.STRING, desc = "common.uuid"),
            @Param(name = "integrationUuid", type = ApiParamType.STRING, desc = "term.framework.integrationuuid"),
            @Param(name = "fileId", type = ApiParamType.LONG, desc = "common.fileid"),
            @Param(name = "ciId", type = ApiParamType.LONG, desc = "term.cmdb.ciid"),
            @Param(name = "config", type = ApiParamType.JSONOBJECT, desc = "common.config")
    })
    @Output({
            @Param(name = "matrix", explode = MatrixVo.class, desc = "common.matrix")
    })
    @Description(desc = "nmtam.matrixsaveapi.getname")
    @ResubmitInterval(value = 2)
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

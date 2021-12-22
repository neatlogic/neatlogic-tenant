/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.matrix;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dto.FieldValidResultVo;
import codedriver.framework.exception.core.ApiRuntimeException;
import codedriver.framework.exception.integration.IntegrationHandlerNotFoundException;
import codedriver.framework.exception.type.ParamNotExistsException;
import codedriver.framework.integration.core.IIntegrationHandler;
import codedriver.framework.integration.core.IntegrationHandlerFactory;
import codedriver.framework.integration.dao.mapper.IntegrationMapper;
import codedriver.framework.integration.dto.IntegrationResultVo;
import codedriver.framework.integration.dto.IntegrationVo;
import codedriver.framework.matrix.core.IMatrixDataSourceHandler;
import codedriver.framework.matrix.core.MatrixDataSourceHandlerFactory;
import codedriver.framework.matrix.dao.mapper.MatrixMapper;
import codedriver.framework.matrix.dto.*;
import codedriver.framework.matrix.exception.*;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.IValid;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.util.UuidUtil;
import codedriver.framework.auth.label.MATRIX_MODIFY;
import codedriver.module.framework.integration.handler.FrameworkRequestFrom;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * @program: codedriver
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

    @Resource
    private IntegrationMapper integrationMapper;

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
            @Param(name = "label", type = ApiParamType.REGEX, rule = "^[A-Za-z]+$", desc = "矩阵唯一标识", xss = true),
            @Param(name = "type", type = ApiParamType.STRING, desc = "矩阵类型"),
            @Param(name = "uuid", type = ApiParamType.STRING, desc = "矩阵uuid"),
            @Param(name = "integrationUuid", type = ApiParamType.STRING, desc = "集成设置uuid"),
            @Param(name = "fileId", type = ApiParamType.LONG, desc = "视图配置文件id"),
            @Param(name = "ciId", type = ApiParamType.LONG, desc = "ci模型id")
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

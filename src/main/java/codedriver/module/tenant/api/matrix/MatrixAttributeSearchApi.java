/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.matrix;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dto.FieldValidResultVo;
import codedriver.framework.matrix.constvalue.MatrixType;
import codedriver.framework.matrix.core.IMatrixDataSourceHandler;
import codedriver.framework.matrix.core.MatrixDataSourceHandlerFactory;
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

    @Resource
    private MatrixExternalMapper matrixExternalMapper;

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
            @Param(name = "matrixUuid", desc = "矩阵uuid", type = ApiParamType.STRING, isRequired = true)
    })
    @Output({
            @Param(name = "tbodyList", desc = "矩阵属性集合", explode = MatrixAttributeVo[].class),
            @Param(name = "type", desc = "类型", type = ApiParamType.ENUM, rule = "custom,external,view")
    })
    @Description(desc = "矩阵属性检索接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject resultObj = new JSONObject();
        String matrixUuid = jsonObj.getString("matrixUuid");
        MatrixVo matrixVo = matrixMapper.getMatrixByUuid(matrixUuid);
        if (matrixVo == null) {
            throw new MatrixNotFoundException(matrixUuid);
        }
        String type = matrixVo.getType();
//        if (MatrixType.CUSTOM.getValue().equals(type)) {
//            List<MatrixAttributeVo> matrixAttributeList = attributeMapper.getMatrixAttributeByMatrixUuid(matrixUuid);
//            if (CollectionUtils.isNotEmpty(matrixAttributeList)) {
//                List<String> attributeUuidList = matrixAttributeList.stream().map(MatrixAttributeVo::getUuid).collect(Collectors.toList());
//                Map<String, Long> attributeDataCountMap = matrixDataMapper.checkMatrixAttributeHasDataByAttributeUuidList(matrixUuid, attributeUuidList, TenantContext.get().getDataDbName());
//                for (MatrixAttributeVo matrixAttributeVo : matrixAttributeList) {
//                    long count = attributeDataCountMap.get(matrixAttributeVo.getUuid());
//                    matrixAttributeVo.setIsDeletable(count == 0 ? 1 : 0);
//                }
//            }
//            resultObj.put("processMatrixAttributeList", matrixAttributeList);
//        } else if (MatrixType.EXTERNAL.getValue().equals(type)) {
//            MatrixExternalVo externalVo = matrixExternalMapper.getMatrixExternalByMatrixUuid(matrixUuid);
//            if (externalVo == null) {
//                throw new MatrixExternalNotFoundException(matrixVo.getName());
//            }
//            IntegrationVo integrationVo = integrationMapper.getIntegrationByUuid(externalVo.getIntegrationUuid());
//            if (integrationVo != null) {
//                IIntegrationHandler handler = IntegrationHandlerFactory.getHandler(integrationVo.getHandler());
//                if (handler == null) {
//                    throw new IntegrationHandlerNotFoundException(integrationVo.getHandler());
//                }
//                resultObj.put("processMatrixAttributeList", matrixService.getExternalMatrixAttributeList(matrixUuid, integrationVo));
//            }
//        } else if (MatrixType.VIEW.getValue().equals(type)) {
//            MatrixViewVo matrixViewVo = matrixViewMapper.getMatrixViewByMatrixUuid(matrixUuid);
//            if (matrixViewVo == null) {
//                throw new MatrixViewNotFoundException(matrixVo.getName());
//            }
//            JSONArray attributeList = (JSONArray) JSONPath.read(matrixViewVo.getConfig(), "attributeList");
//            resultObj.put("processMatrixAttributeList", attributeList);
//        }
        resultObj.put("type", type);
        IMatrixDataSourceHandler matrixDataSourceHandler = MatrixDataSourceHandlerFactory.getHandler(matrixVo.getType());
        if (matrixDataSourceHandler == null) {
            throw new MatrixDataSourceHandlerNotFoundException(matrixVo.getType());
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
            MatrixVo matrixVo = matrixMapper.getMatrixByUuid(matrixUuid);
            if (MatrixType.EXTERNAL.getValue().equals(matrixVo.getType())) {
                MatrixExternalVo externalVo = matrixExternalMapper.getMatrixExternalByMatrixUuid(matrixUuid);
                if (externalVo == null) {
                    return new FieldValidResultVo(new MatrixExternalNotFoundException(matrixVo.getName()));
                }
            }
            return new FieldValidResultVo();
        };
    }
}

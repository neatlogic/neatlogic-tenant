/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.matrix;

import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.constvalue.Expression;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.exception.integration.IntegrationHandlerNotFoundException;
import codedriver.framework.exception.type.ParamIrregularException;
import codedriver.framework.integration.core.IIntegrationHandler;
import codedriver.framework.integration.core.IntegrationHandlerFactory;
import codedriver.framework.integration.core.RequestFrom;
import codedriver.framework.integration.dao.mapper.IntegrationMapper;
import codedriver.framework.integration.dto.IntegrationResultVo;
import codedriver.framework.integration.dto.IntegrationVo;
import codedriver.framework.matrix.constvalue.MatrixType;
import codedriver.framework.matrix.dao.mapper.MatrixAttributeMapper;
import codedriver.framework.matrix.dao.mapper.MatrixDataMapper;
import codedriver.framework.matrix.dao.mapper.MatrixExternalMapper;
import codedriver.framework.matrix.dao.mapper.MatrixMapper;
import codedriver.framework.matrix.dto.*;
import codedriver.framework.matrix.exception.MatrixAttributeNotFoundException;
import codedriver.framework.matrix.exception.MatrixExternalException;
import codedriver.framework.matrix.exception.MatrixExternalNotFoundException;
import codedriver.framework.matrix.exception.MatrixNotFoundException;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.tenant.service.matrix.MatrixService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class MatrixColumnDataInitForTableApi extends PrivateApiComponentBase {

    private final static Logger logger = LoggerFactory.getLogger(MatrixColumnDataInitForTableApi.class);

    @Resource
    private MatrixService matrixService;

    @Resource
    private MatrixMapper matrixMapper;

    @Resource
    private MatrixDataMapper matrixDataMapper;

    @Resource
    private MatrixAttributeMapper matrixAttributeMapper;

    @Resource
    private MatrixExternalMapper matrixExternalMapper;

    @Resource
    private IntegrationMapper integrationMapper;

    @Override
    public String getToken() {
        return "matrix/column/data/init/fortable";
    }

    @Override
    public String getName() {
        return "矩阵属性数据回显-table接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "matrixUuid", desc = "矩阵Uuid", type = ApiParamType.STRING, isRequired = true),
            @Param(name = "columnList", desc = "目标属性集合，数据按这个字段顺序返回", type = ApiParamType.JSONARRAY, isRequired = true),
            @Param(name = "uuidList", desc = "需要回显的数据uuid集合", type = ApiParamType.JSONARRAY),
            @Param(name = "uuidColumn", desc = "uuid对应的属性", type = ApiParamType.STRING),
            @Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否需要分页，默认true"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页条目"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页"),
            @Param(name = "arrayColumnList", desc = "需要将值转化成数组的属性集合", type = ApiParamType.JSONARRAY)
    })
    @Description(desc = "矩阵属性数据回显-table接口")
    @Output({
            @Param(name = "tbodyList", type = ApiParamType.JSONARRAY, desc = "属性数据集合"),
            @Param(name = "theadList", type = ApiParamType.JSONARRAY, desc = "属性列名集合"),
            @Param(name = "type", type = ApiParamType.STRING, desc = "矩阵类型"),
            @Param(explode = BasePageVo.class)
    })
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject returnObj = new JSONObject();
        MatrixDataVo dataVo = JSON.toJavaObject(jsonObj, MatrixDataVo.class);
        MatrixVo matrixVo = matrixMapper.getMatrixByUuid(dataVo.getMatrixUuid());
        if (matrixVo == null) {
            throw new MatrixNotFoundException(dataVo.getMatrixUuid());
        }
        List<String> columnList = dataVo.getColumnList();
        if (CollectionUtils.isEmpty(columnList)) {
            throw new ParamIrregularException("columnList");
        }
        if (MatrixType.CUSTOM.getValue().equals(matrixVo.getType())) {
            returnObj.put("type", MatrixType.CUSTOM.getValue());
            Map<String, MatrixAttributeVo> attributeMap = new HashMap<>();
            List<MatrixAttributeVo> processMatrixAttributeList = matrixAttributeMapper.getMatrixAttributeByMatrixUuid(dataVo.getMatrixUuid());
            for (MatrixAttributeVo attribute : processMatrixAttributeList) {
                attributeMap.put(attribute.getUuid(), attribute);
            }
            // theadList
            JSONArray theadList = new JSONArray();
            for (String column : dataVo.getColumnList()) {
                MatrixAttributeVo attribute = attributeMap.get(column);
                if (attribute != null) {
                    JSONObject theadObj = new JSONObject();
                    theadObj.put("key", attribute.getUuid());
                    theadObj.put("title", attribute.getName());
                    theadList.add(theadObj);
                } else {
                    throw new MatrixAttributeNotFoundException(dataVo.getMatrixUuid(), column);
                }
            }
            returnObj.put("theadList", theadList);
            // tbodyList
            List<Map<String, String>> dataMapList = matrixDataMapper.getDynamicTableDataByUuidList(dataVo, TenantContext.get().getTenantUuid());
            List<Map<String, Object>> tbodyList = matrixService.matrixTableDataValueHandle(processMatrixAttributeList, dataMapList);
            returnObj.put("tbodyList", tbodyList);
            if (dataVo.getNeedPage()) {
                int rowNum = matrixDataMapper.getDynamicTableDataByUuidCount(dataVo, TenantContext.get().getTenantUuid());
                int pageCount = PageUtil.getPageCount(rowNum, dataVo.getPageSize());
                returnObj.put("currentPage", dataVo.getCurrentPage());
                returnObj.put("pageSize", dataVo.getPageSize());
                returnObj.put("pageCount", pageCount);
                returnObj.put("rowNum", rowNum);
            }
        } else {
            returnObj.put("type", MatrixType.EXTERNAL.getValue());
            MatrixExternalVo externalVo = matrixExternalMapper.getMatrixExternalByMatrixUuid(dataVo.getMatrixUuid());
            if (externalVo == null) {
                throw new MatrixExternalNotFoundException(matrixVo.getName());
            }
            IntegrationVo integrationVo = integrationMapper.getIntegrationByUuid(externalVo.getIntegrationUuid());
            IIntegrationHandler handler = IntegrationHandlerFactory.getHandler(integrationVo.getHandler());
            if (handler == null) {
                throw new IntegrationHandlerNotFoundException(integrationVo.getHandler());
            }

            Map<String, MatrixAttributeVo> attributeMap = new HashMap<>();
            List<MatrixAttributeVo> processMatrixAttributeList = matrixService.getExternalMatrixAttributeList(dataVo.getMatrixUuid(), integrationVo);
            for (MatrixAttributeVo processMatrixAttributeVo : processMatrixAttributeList) {
                attributeMap.put(processMatrixAttributeVo.getUuid(), processMatrixAttributeVo);
            }

            // theadList
            JSONArray theadList = new JSONArray();
            for (String column : dataVo.getColumnList()) {
                MatrixAttributeVo attribute = attributeMap.get(column);
                if (attribute != null) {
                    JSONObject theadObj = new JSONObject();
                    theadObj.put("key", attribute.getUuid());
                    theadObj.put("title", attribute.getName());
                    theadList.add(theadObj);
                } else {
                    throw new MatrixAttributeNotFoundException(dataVo.getMatrixUuid(), column);
                }
            }
            String uuidColumn = jsonObj.getString("uuidColumn");
            if (!attributeMap.containsKey(uuidColumn)) {
                throw new MatrixAttributeNotFoundException(dataVo.getMatrixUuid(), uuidColumn);
            }
            integrationVo.getParamObj().putAll(jsonObj);
            List<String> uuidList = dataVo.getUuidList();
            if (CollectionUtils.isNotEmpty(uuidList)) {
                List<Map<String, JSONObject>> resultList = new ArrayList<>();
                List<MatrixColumnVo> sourceColumnList = new ArrayList<>();
                MatrixColumnVo sourceColumnVo = new MatrixColumnVo();
                sourceColumnVo.setColumn(uuidColumn);
                for (String uuidValue : uuidList) {
                    sourceColumnVo.setValue(uuidValue);
                    sourceColumnVo.setExpression(Expression.EQUAL.getExpression());
                    sourceColumnList.clear();
                    sourceColumnList.add(sourceColumnVo);
                    integrationVo.getParamObj().put("sourceColumnList", sourceColumnList);
                    IntegrationResultVo resultVo = handler.sendRequest(integrationVo, RequestFrom.MATRIX);
                    if (StringUtils.isNotBlank(resultVo.getError())) {
                        throw new MatrixExternalException("外部接口访问异常");
                    } else {
                        resultList.addAll(matrixService.getExternalDataTbodyList(resultVo, dataVo.getColumnList(), dataVo.getPageSize(), null));
                    }
                }
                returnObj.put("tbodyList", resultList);
            } else {
                IntegrationResultVo resultVo = handler.sendRequest(integrationVo, RequestFrom.MATRIX);
                if (StringUtils.isNotBlank(resultVo.getError())) {
                    logger.error(resultVo.getError());
                    throw new MatrixExternalException("外部接口访问异常");
                } else {
                    matrixService.getExternalDataTbodyList(resultVo, dataVo.getColumnList(), dataVo.getPageSize(), returnObj);
                }
            }
            /** 将arrayColumnList包含的属性值转成数组 **/
            List<String> arrayColumnList = JSON.parseArray(JSON.toJSONString(jsonObj.getJSONArray("arrayColumnList")), String.class);
            if (CollectionUtils.isNotEmpty(arrayColumnList)) {
                JSONArray tbodyList = returnObj.getJSONArray("tbodyList");
                if (CollectionUtils.isNotEmpty(tbodyList)) {
                    matrixService.arrayColumnDataConversion(arrayColumnList, tbodyList);
                }
            }
            returnObj.put("theadList", theadList);
        }

        return returnObj;
    }
}

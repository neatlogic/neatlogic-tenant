/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.matrix;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.constvalue.Expression;
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
import codedriver.framework.matrix.dao.mapper.MatrixExternalMapper;
import codedriver.framework.matrix.dao.mapper.MatrixMapper;
import codedriver.framework.matrix.dao.mapper.MatrixViewMapper;
import codedriver.framework.matrix.dto.*;
import codedriver.framework.matrix.exception.*;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.tenant.service.matrix.MatrixService;
import com.alibaba.fastjson.*;
import com.google.common.base.Objects;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.Map.Entry;

@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class MatrixColumnDataSearchForSelectNewApi extends PrivateApiComponentBase {

    private final static Logger logger = LoggerFactory.getLogger(MatrixColumnDataSearchForSelectNewApi.class);
    /**
     * 下拉列表value和text列的组合连接符
     **/
    public final static String SELECT_COMPOSE_JOINER = "&=&";
    @Resource
    private MatrixService matrixService;

    @Resource
    private MatrixMapper matrixMapper;

    @Resource
    private MatrixAttributeMapper matrixAttributeMapper;

    @Resource
    private MatrixExternalMapper matrixExternalMapper;

    @Resource
    private MatrixViewMapper viewMapper;

    @Resource
    private IntegrationMapper integrationMapper;

    @Override
    public String getToken() {
        return "matrix/column/data/search/forselect/new";
    }

    @Override
    public String getName() {
        return "矩阵属性数据查询-下拉级联接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "keyword", desc = "关键字", type = ApiParamType.STRING, xss = true),
            @Param(name = "matrixUuid", desc = "矩阵Uuid", type = ApiParamType.STRING, isRequired = true),
            @Param(name = "keywordColumn", desc = "关键字属性uuid", type = ApiParamType.STRING),
            @Param(name = "columnList", desc = "属性uuid列表", type = ApiParamType.JSONARRAY, isRequired = true),
            @Param(name = "sourceColumnList", desc = "源属性集合", type = ApiParamType.JSONARRAY),
            @Param(name = "pageSize", desc = "显示条目数", type = ApiParamType.INTEGER),
            @Param(name = "defaultValue", desc = "精确匹配回显数据参数", type = ApiParamType.JSONARRAY),
            @Param(name = "filterList", desc = "根据列头uuid,搜索具体的列值，支持多个列分别搜索，注意仅支持静态列表  [{uuid:***,valueList:[]},{uuid:***,valueList:[]}]", type = ApiParamType.JSONARRAY)
    })
    @Output({
            @Param(name = "columnDataList", type = ApiParamType.JSONARRAY, desc = "属性数据集合")
    })
    @Description(desc = "矩阵属性数据查询-下拉级联接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {

        MatrixDataVo dataVo = JSONObject.toJavaObject(jsonObj, MatrixDataVo.class);
        MatrixVo matrixVo = matrixMapper.getMatrixByUuid(dataVo.getMatrixUuid());
        if (matrixVo == null) {
            throw new MatrixNotFoundException(dataVo.getMatrixUuid());
        }

        JSONArray defaultValue = dataVo.getDefaultValue();

        List<String> columnList = dataVo.getColumnList();
        if (CollectionUtils.isEmpty(columnList)) {
            throw new ParamIrregularException("columnList");
        }
        String keywordColumn = jsonObj.getString("keywordColumn");
        List<Map<String, JSONObject>> resultList = new ArrayList<>();
        JSONObject returnObj = new JSONObject();
        if (MatrixType.CUSTOM.getValue().equals(matrixVo.getType())) {
            List<MatrixAttributeVo> attributeList = matrixAttributeMapper.getMatrixAttributeByMatrixUuid(dataVo.getMatrixUuid());
            if (CollectionUtils.isNotEmpty(attributeList)) {
                Map<String, MatrixAttributeVo> matrixAttributeMap = new HashMap<>();
                for (MatrixAttributeVo matrixAttributeVo : attributeList) {
                    matrixAttributeMap.put(matrixAttributeVo.getUuid(), matrixAttributeVo);
                }
                /** 属性集合去重 **/
                List<String> distinctColumList = new ArrayList<>();
                for (String column : columnList) {
                    if (!matrixAttributeMap.containsKey(column)) {
                        throw new MatrixAttributeNotFoundException(dataVo.getMatrixUuid(), column);
                    }
                    if (!distinctColumList.contains(column)) {
                        distinctColumList.add(column);
                    }
                }
                dataVo.setColumnList(distinctColumList);
                if (CollectionUtils.isNotEmpty(defaultValue)) {
                    for (String value : defaultValue.toJavaList(String.class)) {
                        if (value.contains(SELECT_COMPOSE_JOINER)) {
                            List<MatrixColumnVo> sourceColumnList = new ArrayList<>();
                            String[] split = value.split(SELECT_COMPOSE_JOINER);
                            if (StringUtils.isNotBlank(columnList.get(0))) {
                                MatrixColumnVo matrixColumnVo = new MatrixColumnVo(columnList.get(0), split[0]);
                                matrixColumnVo.setExpression(Expression.EQUAL.getExpression());
                                sourceColumnList.add(matrixColumnVo);
                            }
                            dataVo.setSourceColumnList(sourceColumnList);
                            if (columnList.size() >= 2 && StringUtils.isNotBlank(columnList.get(1))) {
                                MatrixAttributeVo matrixAttribute = matrixAttributeMap.get(columnList.get(1));
                                if (matrixAttribute == null) {
                                    throw new MatrixAttributeNotFoundException(dataVo.getMatrixUuid(), columnList.get(1));
                                }
                                dataVo.setKeyword(split[1]);
                                List<Map<String, String>> dataMapList = matrixService.matrixAttributeValueKeyWordSearch(matrixAttribute, dataVo);
                                if (CollectionUtils.isNotEmpty(dataMapList)) {
                                    for (Map<String, String> dataMap : dataMapList) {
                                        Map<String, JSONObject> resultMap = new HashMap<>(dataMap.size());
                                        for (Entry<String, String> entry : dataMap.entrySet()) {
                                            String attributeUuid = entry.getKey();
                                            resultMap.put(attributeUuid, matrixService.matrixAttributeValueHandle(matrixAttributeMap.get(attributeUuid), entry.getValue()));
                                        }
                                        JSONObject textObj = resultMap.get(columnList.get(1));
                                        if (MapUtils.isNotEmpty(textObj) && Objects.equal(textObj.get("text"), split[1])) {
                                            resultList.add(resultMap);
                                        }
                                    }
                                } else {
                                    return returnObj;
                                }

                            } else {
                                return returnObj;
                            }
                        }
                    }
                } else {
                    MatrixAttributeVo matrixAttribute = null;
                    if (StringUtils.isNotBlank(keywordColumn) && StringUtils.isNotBlank(dataVo.getKeyword())) {
                        matrixAttribute = matrixAttributeMap.get(keywordColumn);
                        if (matrixAttribute == null) {
                            throw new MatrixAttributeNotFoundException(dataVo.getMatrixUuid(), keywordColumn);
                        }
                    }
                    List<Map<String, String>> dataMapList = matrixService.matrixAttributeValueKeyWordSearch(matrixAttribute, dataVo);
                    for (Map<String, String> dataMap : dataMapList) {
                        Map<String, JSONObject> resultMap = new HashMap<>(dataMap.size());
                        for (Entry<String, String> entry : dataMap.entrySet()) {
                            String attributeUuid = entry.getKey();
                            resultMap.put(attributeUuid, matrixService.matrixAttributeValueHandle(matrixAttributeMap.get(attributeUuid), entry.getValue()));
                        }
                        resultList.add(resultMap);
                    }
                }
            }

        } else if (MatrixType.EXTERNAL.getValue().equals(matrixVo.getType())) {
            MatrixExternalVo externalVo = matrixExternalMapper.getMatrixExternalByMatrixUuid(dataVo.getMatrixUuid());
            if (externalVo == null) {
                throw new MatrixExternalNotFoundException(matrixVo.getName());
            }
            IntegrationVo integrationVo = integrationMapper.getIntegrationByUuid(externalVo.getIntegrationUuid());
            IIntegrationHandler handler = IntegrationHandlerFactory.getHandler(integrationVo.getHandler());
            if (handler == null) {
                throw new IntegrationHandlerNotFoundException(integrationVo.getHandler());
            }
            List<String> attributeList = new ArrayList<>();
            List<MatrixAttributeVo> matrixAttributeList = matrixService.getExternalMatrixAttributeList(dataVo.getMatrixUuid(), integrationVo);
            for (MatrixAttributeVo matrixAttributeVo : matrixAttributeList) {
                attributeList.add(matrixAttributeVo.getUuid());
            }

            for (String column : columnList) {
                if (!attributeList.contains(column)) {
                    throw new MatrixAttributeNotFoundException(dataVo.getMatrixUuid(), column);
                }
            }
            List<MatrixColumnVo> sourceColumnList = new ArrayList<>();
            jsonObj.put("sourceColumnList", sourceColumnList); //防止集成管理 js length 异常
            if (CollectionUtils.isNotEmpty(defaultValue)) {
                for (String value : defaultValue.toJavaList(String.class)) {
                    if (value.contains(SELECT_COMPOSE_JOINER)) {

                        String[] split = value.split(SELECT_COMPOSE_JOINER);
                        for (int i = 0; i < split.length; i++) {
                            String column = columnList.get(i);
                            if (StringUtils.isNotBlank(column)) {
                                MatrixColumnVo matrixColumnVo = new MatrixColumnVo(column, split[i]);
                                matrixColumnVo.setExpression(Expression.EQUAL.getExpression());
                                sourceColumnList.add(matrixColumnVo);
                            }
                        }
                        // dataVo.setSourceColumnList(sourceColumnList);
                        integrationVo.getParamObj().putAll(jsonObj);
                        IntegrationResultVo resultVo = handler.sendRequest(integrationVo, RequestFrom.MATRIX);
                        if (StringUtils.isNotBlank(resultVo.getError())) {
                            logger.error(resultVo.getError());
                            throw new MatrixExternalAccessException();
                        } else {
                            resultList.addAll(matrixService.getExternalDataTbodyList(resultVo, columnList, null));
                        }
                    }
                }
            } else {
                if (StringUtils.isNotBlank(keywordColumn) && StringUtils.isNotBlank(dataVo.getKeyword())) {
                    if (!attributeList.contains(keywordColumn)) {
                        throw new MatrixAttributeNotFoundException(dataVo.getMatrixUuid(), keywordColumn);
                    }
                }
                if (StringUtils.isNotBlank(keywordColumn) && StringUtils.isNotBlank(dataVo.getKeyword())) {
                    MatrixColumnVo matrixColumnVo = new MatrixColumnVo();
                    matrixColumnVo.setColumn(keywordColumn);
                    matrixColumnVo.setExpression(Expression.LIKE.getExpression());
                    matrixColumnVo.setValue(dataVo.getKeyword());
//                    sourceColumnList = dataVo.getSourceColumnList();
                    sourceColumnList.add(matrixColumnVo);
//                    jsonObj.put("sourceColumnList", sourceColumnList);
                }
                integrationVo.getParamObj().putAll(jsonObj);
                IntegrationResultVo resultVo = handler.sendRequest(integrationVo, RequestFrom.MATRIX);
                if (StringUtils.isNotBlank(resultVo.getError())) {
                    logger.error(resultVo.getError());
                    throw new MatrixExternalAccessException();
                } else {
                    resultList = matrixService.getExternalDataTbodyList(resultVo, columnList, null);
                }
            }
        } else if (MatrixType.VIEW.getValue().equals(matrixVo.getType())) {
            MatrixViewVo matrixViewVo = viewMapper.getMatrixViewByMatrixUuid(dataVo.getMatrixUuid());
            if (matrixViewVo == null) {
                throw new MatrixViewNotFoundException(matrixVo.getName());
            }
            JSONArray attributeList = (JSONArray) JSONPath.read(matrixViewVo.getConfig(), "attributeList");
//			List<MatrixAttributeVo> attributeList = matrixAttributeMapper.getMatrixAttributeByMatrixUuid(dataVo.getMatrixUuid());
            if (CollectionUtils.isNotEmpty(attributeList)) {
                Map<String, MatrixAttributeVo> matrixAttributeMap = new HashMap<>();
                List<MatrixAttributeVo> attributeVoList = attributeList.toJavaList(MatrixAttributeVo.class);
                for (MatrixAttributeVo matrixAttributeVo : attributeVoList) {
                    matrixAttributeMap.put(matrixAttributeVo.getUuid(), matrixAttributeVo);
                }
                /** 属性集合去重 **/
                List<String> distinctColumList = new ArrayList<>();
                for (String column : columnList) {
                    if (!matrixAttributeMap.containsKey(column)) {
                        throw new MatrixAttributeNotFoundException(dataVo.getMatrixUuid(), column);
                    }
                    if (!distinctColumList.contains(column)) {
                        distinctColumList.add(column);
                    }
                }
                dataVo.setColumnList(distinctColumList);
                List<Map<String, String>> dataMapList = null;
                if (CollectionUtils.isNotEmpty(defaultValue)) {
                    for (String value : defaultValue.toJavaList(String.class)) {
                        if (value.contains(SELECT_COMPOSE_JOINER)) {
                            List<MatrixColumnVo> sourceColumnList = new ArrayList<>();
                            String[] split = value.split(SELECT_COMPOSE_JOINER);
                            if (StringUtils.isNotBlank(columnList.get(0))) {
                                MatrixColumnVo matrixColumnVo = new MatrixColumnVo(columnList.get(0), split[0]);
                                matrixColumnVo.setExpression(Expression.EQUAL.getExpression());
                                sourceColumnList.add(matrixColumnVo);
                            }
                            dataVo.setSourceColumnList(sourceColumnList);
                            if (columnList.size() >= 2 && StringUtils.isNotBlank(columnList.get(1))) {
                                MatrixAttributeVo matrixAttribute = matrixAttributeMap.get(columnList.get(1));
                                if (matrixAttribute == null) {
                                    throw new MatrixAttributeNotFoundException(dataVo.getMatrixUuid(), columnList.get(1));
                                }
                                dataVo.setKeyword(split[1]);
                                dataMapList = matrixService.matrixAttributeValueKeyWordSearch(matrixAttribute, dataVo);
                                if (CollectionUtils.isNotEmpty(dataMapList)) {
                                    for (Map<String, String> dataMap : dataMapList) {
                                        Map<String, JSONObject> resultMap = new HashMap<>(dataMap.size());
                                        for (Entry<String, String> entry : dataMap.entrySet()) {
                                            String attributeUuid = entry.getKey();
                                            resultMap.put(attributeUuid, matrixService.matrixAttributeValueHandle(matrixAttributeMap.get(attributeUuid), entry.getValue()));
                                        }
                                        JSONObject textObj = resultMap.get(columnList.get(1));
                                        if (MapUtils.isNotEmpty(textObj) && Objects.equal(textObj.get("text"), split[1])) {
                                            resultList.add(resultMap);
                                            ;
                                        }
                                    }
                                } else {
                                    return returnObj;
                                }

                            } else {
                                return returnObj;
                            }
                        }
                    }
                } else {
                    MatrixAttributeVo matrixAttribute = null;
                    if (StringUtils.isNotBlank(keywordColumn) && StringUtils.isNotBlank(dataVo.getKeyword())) {
                        matrixAttribute = matrixAttributeMap.get(keywordColumn);
                        if (matrixAttribute == null) {
                            throw new MatrixAttributeNotFoundException(dataVo.getMatrixUuid(), keywordColumn);
                        }
                    }
                    dataMapList = matrixService.matrixAttributeValueKeyWordSearch(matrixAttribute, dataVo);
                    for (Map<String, String> dataMap : dataMapList) {
                        Map<String, JSONObject> resultMap = new HashMap<>(dataMap.size());
                        for (Entry<String, String> entry : dataMap.entrySet()) {
                            String attributeUuid = entry.getKey();
                            resultMap.put(attributeUuid, matrixService.matrixAttributeValueHandle(matrixAttributeMap.get(attributeUuid), entry.getValue()));
                        }
                        resultList.add(resultMap);
                    }
                }
            }
        }

        if (columnList.size() == 2) {
            for (Map<String, JSONObject> resultObj : resultList) {
                JSONObject firstObj = resultObj.get(columnList.get(0));
                String firstValue = firstObj.getString("value");
                String firstText = firstObj.getString("text");
                JSONObject secondObj = resultObj.get(columnList.get(1));
                String secondText = secondObj.getString("text");
                firstObj.put("compose", firstValue + SELECT_COMPOSE_JOINER + secondText);
                secondObj.put("compose", secondText + "(" + firstText + ")");
            }
        }

        //去重
        List<String> exsited = new ArrayList<>();
        Iterator<Map<String, JSONObject>> iterator = resultList.iterator();
        while (iterator.hasNext()) {
            Map<String, JSONObject> resultObj = iterator.next();
            JSONObject firstObj = resultObj.get(columnList.get(0));
            String firstValue = firstObj.getString("compose");
            if (StringUtils.isNotBlank(firstValue)) {
                firstValue = firstObj.getString("value");
            }
            if (exsited.contains(firstValue)) {
                iterator.remove();
            } else {
                exsited.add(firstValue);
            }
        }
        returnObj.put("columnDataList", resultList);
        return returnObj;
    }
}

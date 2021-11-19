/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.matrix;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.exception.type.ParamIrregularException;
import codedriver.framework.matrix.core.IMatrixDataSourceHandler;
import codedriver.framework.matrix.core.MatrixDataSourceHandlerFactory;
import codedriver.framework.matrix.dao.mapper.MatrixMapper;
import codedriver.framework.matrix.dto.*;
import codedriver.framework.matrix.exception.*;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.*;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class MatrixColumnDataSearchForSelectNewApi extends PrivateApiComponentBase {

    private final static Logger logger = LoggerFactory.getLogger(MatrixColumnDataSearchForSelectNewApi.class);
    /**
     * 下拉列表value和text列的组合连接符
     **/
    private final static String SELECT_COMPOSE_JOINER = "&=&";

    @Resource
    private MatrixMapper matrixMapper;

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

    @Override
    public boolean disableReturnCircularReferenceDetect() {
       return true;
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
            @Param(name = "tbodyList", type = ApiParamType.JSONARRAY, desc = "属性数据集合")
    })
    @Description(desc = "矩阵属性数据查询-下拉级联接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        MatrixDataVo dataVo = JSONObject.toJavaObject(jsonObj, MatrixDataVo.class);

        List<String> columnList = dataVo.getColumnList();
        if (CollectionUtils.isEmpty(columnList)) {
            throw new ParamIrregularException("columnList");
        }
        /** 属性集合去重 **/
        List<String> distinctColumList = new ArrayList<>();
        for (String column : columnList) {
            if (!distinctColumList.contains(column)) {
                distinctColumList.add(column);
            }
        }
        columnList = distinctColumList;
        dataVo.setColumnList(distinctColumList);
        MatrixVo matrixVo = matrixMapper.getMatrixByUuid(dataVo.getMatrixUuid());
        if (matrixVo == null) {
            throw new MatrixNotFoundException(dataVo.getMatrixUuid());
        }

//        JSONArray defaultValue = dataVo.getDefaultValue();
//        String keywordColumn = jsonObj.getString("keywordColumn");
//        List<Map<String, JSONObject>> resultList = new ArrayList<>();
        JSONObject returnObj = new JSONObject();
//        if (MatrixType.CUSTOM.getValue().equals(matrixVo.getType())) {
//            List<MatrixAttributeVo> attributeList = matrixAttributeMapper.getMatrixAttributeByMatrixUuid(dataVo.getMatrixUuid());
//            if (CollectionUtils.isNotEmpty(attributeList)) {
//                Map<String, MatrixAttributeVo> matrixAttributeMap = new HashMap<>();
//                for (MatrixAttributeVo matrixAttributeVo : attributeList) {
//                    matrixAttributeMap.put(matrixAttributeVo.getUuid(), matrixAttributeVo);
//                }
//                for (String column : columnList) {
//                    if (!matrixAttributeMap.containsKey(column)) {
//                        throw new MatrixAttributeNotFoundException(dataVo.getMatrixUuid(), column);
//                    }
//                }
//                if (CollectionUtils.isNotEmpty(defaultValue)) {
//                    for (String value : defaultValue.toJavaList(String.class)) {
//                        if (value.contains(SELECT_COMPOSE_JOINER)) {
//                            List<MatrixColumnVo> sourceColumnList = new ArrayList<>();
//                            String[] split = value.split(SELECT_COMPOSE_JOINER);
//                            if (StringUtils.isNotBlank(columnList.get(0))) {
//                                MatrixColumnVo matrixColumnVo = new MatrixColumnVo(columnList.get(0), split[0]);
//                                matrixColumnVo.setExpression(Expression.EQUAL.getExpression());
//                                sourceColumnList.add(matrixColumnVo);
//                            }
//                            dataVo.setSourceColumnList(sourceColumnList);
//                            if (columnList.size() >= 2) {
//                                keywordColumn = columnList.get(1);
//                            } else {
//                                keywordColumn = columnList.get(0);
//                            }
//                            MatrixAttributeVo matrixAttribute = matrixAttributeMap.get(keywordColumn);
//                            if (matrixAttribute == null) {
//                                throw new MatrixAttributeNotFoundException(dataVo.getMatrixUuid(), keywordColumn);
//                            }
//                            dataVo.setKeyword(split[1]);
//                            List<Map<String, String>> dataMapList = matrixService.matrixAttributeValueKeyWordSearch(matrixAttribute, dataVo);
//                            if (CollectionUtils.isNotEmpty(dataMapList)) {
//                                for (Map<String, String> dataMap : dataMapList) {
//                                    Map<String, JSONObject> resultMap = new HashMap<>(dataMap.size());
//                                    for (Entry<String, String> entry : dataMap.entrySet()) {
//                                        String attributeUuid = entry.getKey();
//                                        resultMap.put(attributeUuid, matrixService.matrixAttributeValueHandle(matrixAttributeMap.get(attributeUuid), entry.getValue()));
//                                    }
//                                    JSONObject textObj = resultMap.get(keywordColumn);
//                                    if (MapUtils.isNotEmpty(textObj) && Objects.equal(textObj.get("text"), split[1])) {
//                                        resultList.add(resultMap);
//                                    }
//                                }
//                            }
//                        }
//                    }
//                } else {
//                    MatrixAttributeVo matrixAttribute = null;
//                    if (StringUtils.isNotBlank(keywordColumn) && StringUtils.isNotBlank(dataVo.getKeyword())) {
//                        matrixAttribute = matrixAttributeMap.get(keywordColumn);
//                        if (matrixAttribute == null) {
//                            throw new MatrixAttributeNotFoundException(dataVo.getMatrixUuid(), keywordColumn);
//                        }
//                    }
//                    List<Map<String, String>> dataMapList = matrixService.matrixAttributeValueKeyWordSearch(matrixAttribute, dataVo);
//                    for (Map<String, String> dataMap : dataMapList) {
//                        Map<String, JSONObject> resultMap = new HashMap<>(dataMap.size());
//                        for (Entry<String, String> entry : dataMap.entrySet()) {
//                            String attributeUuid = entry.getKey();
//                            resultMap.put(attributeUuid, matrixService.matrixAttributeValueHandle(matrixAttributeMap.get(attributeUuid), entry.getValue()));
//                        }
//                        resultList.add(resultMap);
//                    }
//                }
//            }
//        } else if (MatrixType.VIEW.getValue().equals(matrixVo.getType())) {
//            MatrixViewVo matrixViewVo = viewMapper.getMatrixViewByMatrixUuid(dataVo.getMatrixUuid());
//            if (matrixViewVo == null) {
//                throw new MatrixViewNotFoundException(matrixVo.getName());
//            }
//            JSONArray attributeArray = (JSONArray) JSONPath.read(matrixViewVo.getConfig(), "attributeList");
//            if (CollectionUtils.isNotEmpty(attributeArray)) {
//                List<MatrixAttributeVo> attributeList = attributeArray.toJavaList(MatrixAttributeVo.class);
//                Map<String, MatrixAttributeVo> matrixAttributeMap = new HashMap<>();
//                for (MatrixAttributeVo matrixAttributeVo : attributeList) {
//                    matrixAttributeMap.put(matrixAttributeVo.getUuid(), matrixAttributeVo);
//                }
//                for (String column : columnList) {
//                    if (!matrixAttributeMap.containsKey(column)) {
//                        throw new MatrixAttributeNotFoundException(dataVo.getMatrixUuid(), column);
//                    }
//                }
//                if (CollectionUtils.isNotEmpty(defaultValue)) {
//                    for (String value : defaultValue.toJavaList(String.class)) {
//                        if (value.contains(SELECT_COMPOSE_JOINER)) {
//                            List<MatrixColumnVo> sourceColumnList = new ArrayList<>();
//                            String[] split = value.split(SELECT_COMPOSE_JOINER);
//                            if (StringUtils.isNotBlank(columnList.get(0))) {
//                                MatrixColumnVo matrixColumnVo = new MatrixColumnVo(columnList.get(0), split[0]);
//                                matrixColumnVo.setExpression(Expression.EQUAL.getExpression());
//                                sourceColumnList.add(matrixColumnVo);
//                            }
//                            dataVo.setSourceColumnList(sourceColumnList);
//                            if (columnList.size() >= 2) {
//                                keywordColumn = columnList.get(1);
//                            } else {
//                                keywordColumn = columnList.get(0);
//                            }
//                            MatrixAttributeVo matrixAttribute = matrixAttributeMap.get(keywordColumn);
//                            if (matrixAttribute == null) {
//                                throw new MatrixAttributeNotFoundException(dataVo.getMatrixUuid(), keywordColumn);
//                            }
//                            dataVo.setKeyword(split[1]);
//                            List<Map<String, String>> dataMapList = matrixService.matrixAttributeValueKeyWordSearch(matrixAttribute, dataVo);
//                            if (CollectionUtils.isNotEmpty(dataMapList)) {
//                                for (Map<String, String> dataMap : dataMapList) {
//                                    Map<String, JSONObject> resultMap = new HashMap<>(dataMap.size());
//                                    for (Entry<String, String> entry : dataMap.entrySet()) {
//                                        String attributeUuid = entry.getKey();
//                                        resultMap.put(attributeUuid, matrixService.matrixAttributeValueHandle(matrixAttributeMap.get(attributeUuid), entry.getValue()));
//                                    }
//                                    JSONObject textObj = resultMap.get(keywordColumn);
//                                    if (MapUtils.isNotEmpty(textObj) && Objects.equal(textObj.get("text"), split[1])) {
//                                        resultList.add(resultMap);
//                                    }
//                                }
//                            }
//                        }
//                    }
//                } else {
//                    MatrixAttributeVo matrixAttribute = null;
//                    if (StringUtils.isNotBlank(keywordColumn) && StringUtils.isNotBlank(dataVo.getKeyword())) {
//                        matrixAttribute = matrixAttributeMap.get(keywordColumn);
//                        if (matrixAttribute == null) {
//                            throw new MatrixAttributeNotFoundException(dataVo.getMatrixUuid(), keywordColumn);
//                        }
//                    }
//                    List<Map<String, String>> dataMapList = matrixService.matrixAttributeValueKeyWordSearch(matrixAttribute, dataVo);
//                    for (Map<String, String> dataMap : dataMapList) {
//                        Map<String, JSONObject> resultMap = new HashMap<>(dataMap.size());
//                        for (Entry<String, String> entry : dataMap.entrySet()) {
//                            String attributeUuid = entry.getKey();
//                            resultMap.put(attributeUuid, matrixService.matrixAttributeValueHandle(matrixAttributeMap.get(attributeUuid), entry.getValue()));
//                        }
//                        resultList.add(resultMap);
//                    }
//                }
//            }
//        } else if (MatrixType.EXTERNAL.getValue().equals(matrixVo.getType())) {
//            MatrixExternalVo externalVo = matrixExternalMapper.getMatrixExternalByMatrixUuid(dataVo.getMatrixUuid());
//            if (externalVo == null) {
//                throw new MatrixExternalNotFoundException(matrixVo.getName());
//            }
//            IntegrationVo integrationVo = integrationMapper.getIntegrationByUuid(externalVo.getIntegrationUuid());
//            IIntegrationHandler handler = IntegrationHandlerFactory.getHandler(integrationVo.getHandler());
//            if (handler == null) {
//                throw new IntegrationHandlerNotFoundException(integrationVo.getHandler());
//            }
//            List<MatrixAttributeVo> matrixAttributeList = matrixService.getExternalMatrixAttributeList(dataVo.getMatrixUuid(), integrationVo);
//            if (CollectionUtils.isNotEmpty(matrixAttributeList)) {
//                List<String> attributeList = matrixAttributeList.stream().map(MatrixAttributeVo::getUuid).collect(Collectors.toList());
//                for (String column : columnList) {
//                    if (!attributeList.contains(column)) {
//                        throw new MatrixAttributeNotFoundException(dataVo.getMatrixUuid(), column);
//                    }
//                }
//                List<MatrixColumnVo> sourceColumnList = new ArrayList<>();
//                jsonObj.put("sourceColumnList", sourceColumnList); //防止集成管理 js length 异常
//                if (CollectionUtils.isNotEmpty(defaultValue)) {
//                    for (String value : defaultValue.toJavaList(String.class)) {
//                        if (value.contains(SELECT_COMPOSE_JOINER)) {
//                            String[] split = value.split(SELECT_COMPOSE_JOINER);
//                            //当下拉框配置的值和显示文字列为同一列时，value值是这样的20210101&=&20210101，split数组第一和第二个元素相同，这时需要去重
//                            List<String> splitList = new ArrayList<>();
//                            for (String str : split) {
//                                if (!splitList.contains(str)) {
//                                    splitList.add(str);
//                                }
//                            }
//                            int min = Math.min(splitList.size(), columnList.size());
//                            for (int i = 0; i < min; i++) {
//                                String column = columnList.get(i);
//                                if (StringUtils.isNotBlank(column)) {
//                                    MatrixColumnVo matrixColumnVo = new MatrixColumnVo(column, splitList.get(i));
//                                    matrixColumnVo.setExpression(Expression.EQUAL.getExpression());
//                                    sourceColumnList.add(matrixColumnVo);
//                                }
//                            }
//                            integrationVo.getParamObj().putAll(jsonObj);
//                            IntegrationResultVo resultVo = handler.sendRequest(integrationVo, RequestFrom.MATRIX);
//                            if (StringUtils.isNotBlank(resultVo.getError())) {
//                                logger.error(resultVo.getError());
//                                throw new MatrixExternalAccessException();
//                            }
//                            resultList.addAll(matrixService.getExternalDataTbodyList(resultVo, columnList));
//                        }
//                    }
//                } else {
//                    if (StringUtils.isNotBlank(keywordColumn) && StringUtils.isNotBlank(dataVo.getKeyword())) {
//                        if (!attributeList.contains(keywordColumn)) {
//                            throw new MatrixAttributeNotFoundException(dataVo.getMatrixUuid(), keywordColumn);
//                        }
//                        MatrixColumnVo matrixColumnVo = new MatrixColumnVo();
//                        matrixColumnVo.setColumn(keywordColumn);
//                        matrixColumnVo.setExpression(Expression.LIKE.getExpression());
//                        matrixColumnVo.setValue(dataVo.getKeyword());
//                        sourceColumnList.add(matrixColumnVo);
//                    }
//                    integrationVo.getParamObj().putAll(jsonObj);
//                    IntegrationResultVo resultVo = handler.sendRequest(integrationVo, RequestFrom.MATRIX);
//                    if (StringUtils.isNotBlank(resultVo.getError())) {
//                        logger.error(resultVo.getError());
//                        throw new MatrixExternalAccessException();
//                    }
//                    resultList = matrixService.getExternalDataTbodyList(resultVo, columnList);
//                }
//                //去重
//                String firstColumn = columnList.get(0);
//                String secondColumn = columnList.get(0);
//                if (columnList.size() >= 2) {
//                    secondColumn = columnList.get(1);
//                }
//                List<String> exsited = new ArrayList<>();
//                Iterator<Map<String, JSONObject>> iterator = resultList.iterator();
//                while (iterator.hasNext()) {
//                    Map<String, JSONObject> resultObj = iterator.next();
//                    JSONObject firstObj = resultObj.get(firstColumn);
//                    JSONObject secondObj = resultObj.get(secondColumn);
//                    String firstValue = firstObj.getString("value");
//                    String secondText = secondObj.getString("text");
//                    String compose = firstValue + SELECT_COMPOSE_JOINER + secondText;
//                    if (exsited.contains(compose)) {
//                        iterator.remove();
//                    } else {
//                        exsited.add(compose);
//                    }
//                }
//            }
//        }
        IMatrixDataSourceHandler matrixDataSourceHandler = MatrixDataSourceHandlerFactory.getHandler(matrixVo.getType());
        if (matrixDataSourceHandler == null) {
            throw new MatrixDataSourceHandlerNotFoundException(matrixVo.getType());
        }
        List<Map<String, JSONObject>> resultList = matrixDataSourceHandler.TableColumnDataSearch(dataVo);
        if (columnList.size() >= 2) {
            for (Map<String, JSONObject> resultObj : resultList) {
                JSONObject firstObj = resultObj.get(columnList.get(0));
                String firstValue = firstObj.getString("value");
                String firstText = firstObj.getString("text");
                JSONObject secondObj = resultObj.get(columnList.get(1));
                String secondText = secondObj.getString("text");
                secondObj.put("compose", secondText + "(" + firstText + ")");
                firstObj.put("compose", firstValue + SELECT_COMPOSE_JOINER + secondText);
            }
        } else if (columnList.size() == 1) {
            for (Map<String, JSONObject> resultObj : resultList) {
                JSONObject firstObj = resultObj.get(columnList.get(0));
                String firstValue = firstObj.getString("value");
                String firstText = firstObj.getString("text");
                firstObj.put("compose", firstValue + SELECT_COMPOSE_JOINER + firstText);
            }
        }

        returnObj.put("columnDataList", resultList);//TODO linbq 等前端改完再删
        returnObj.put("tbodyList", resultList);
        return returnObj;
    }
}

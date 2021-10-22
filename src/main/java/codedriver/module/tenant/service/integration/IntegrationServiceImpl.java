/*
 * Copyright(c) 2021 TechSureCo.,Ltd.AllRightsReserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.service.integration;

import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.integration.dto.IntegrationResultVo;
import codedriver.framework.integration.dto.IntegrationVo;
import codedriver.framework.integration.dto.table.ColumnVo;
import codedriver.framework.util.javascript.JavascriptUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author linbq
 * @since 2021/10/21 17:36
 **/
@Service
public class IntegrationServiceImpl implements IntegrationService {

    private static final Logger logger = LoggerFactory.getLogger(IntegrationServiceImpl.class);

    @Override
    public List<ColumnVo> getColumnList(IntegrationVo integrationVo) {
        List<ColumnVo> resultList = new ArrayList<>();
        JSONObject config = integrationVo.getConfig();
        if (MapUtils.isNotEmpty(config)) {
            JSONObject output = config.getJSONObject("output");
            if (MapUtils.isNotEmpty(output)) {
                String content = output.getString("content");
                if (StringUtils.isNotBlank(content)) {
                    try {
                        content = JavascriptUtil.transform(new JSONObject(), content);
                        JSONObject contentObj = JSON.parseObject(content);
                        if (MapUtils.isNotEmpty(contentObj)) {
                            JSONArray theadList = contentObj.getJSONArray("theadList");
                            if (CollectionUtils.isNotEmpty(theadList)) {
                                for (int i = 0; i < theadList.size(); i++) {
                                    JSONObject theadObj = theadList.getJSONObject(i);
                                    ColumnVo columnVo = new ColumnVo();
                                    columnVo.setUuid(theadObj.getString("key"));
                                    columnVo.setName(theadObj.getString("title"));
                                    columnVo.setType(theadObj.getString("type"));
                                    columnVo.setPrimaryKey(theadObj.getInteger("primaryKey"));
                                    Integer isSearchable = theadObj.getInteger("isSearchable");
                                    isSearchable = (isSearchable == null || isSearchable.intValue() != 1) ? 0 : 1;
                                    columnVo.setIsSearchable(isSearchable);
                                    columnVo.setSort(i);
                                    columnVo.setIsRequired(0);
                                    resultList.add(columnVo);
                                }
                            }
                        }
                    } catch (Exception ex) {
                        logger.error(ex.getMessage(), ex);
                    }
                }
            }
        }
        return resultList;
    }

    @Override
    public JSONArray getTheadList(IntegrationVo integrationVo, List<String> columnList) {
        JSONArray resultList = new JSONArray();
        JSONObject config = integrationVo.getConfig();
        if (MapUtils.isNotEmpty(config)) {
            JSONObject output = config.getJSONObject("output");
            if (MapUtils.isNotEmpty(output)) {
                String content = output.getString("content");
                if (StringUtils.isNotBlank(content)) {
                    try {
                        content = JavascriptUtil.transform(new JSONObject(), content);
                        JSONObject contentObj = JSON.parseObject(content);
                        if (MapUtils.isNotEmpty(contentObj)) {
                            JSONArray theadList = contentObj.getJSONArray("theadList");
                            if (CollectionUtils.isNotEmpty(theadList)) {
                                if (CollectionUtils.isEmpty(columnList)) {
                                    return theadList;
                                }
                                Map<String, JSONObject> theadMap = new HashMap<>();
                                for (int i = 0; i < theadList.size(); i++) {
                                    JSONObject theadObj = theadList.getJSONObject(i);
                                    String key = theadObj.getString("key");
                                    theadMap.put(key, theadObj);
                                }
                                for (String column : columnList) {
                                    JSONObject theadObj = theadMap.get(column);
                                    if (theadObj != null) {
                                        resultList.add(theadObj);
                                    }
                                }
                            }
                        }
                    } catch (Exception ex) {
                        logger.error(ex.getMessage(), ex);
                    }
                }
            }
        }
        return resultList;
    }

    @Override
    public List<Map<String, Object>> getTbodyList(IntegrationResultVo resultVo, List<String> columnList) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        if (resultVo != null && StringUtils.isNotBlank(resultVo.getTransformedResult())) {
            JSONObject transformedResult = JSONObject.parseObject(resultVo.getTransformedResult());
            if (MapUtils.isNotEmpty(transformedResult)) {
                JSONArray tbodyList = transformedResult.getJSONArray("tbodyList");
                if (CollectionUtils.isNotEmpty(tbodyList)) {
                    for (int i = 0; i < tbodyList.size(); i++) {
                        JSONObject rowData = tbodyList.getJSONObject(i);
                        Map<String, Object> resultMap = new HashMap<>(columnList.size());
                        for (String column : columnList) {
                            resultMap.put(column, rowData.get(column));
//                            String columnValue = rowData.getString(column);
//                            resultMap.put(column, matrixAttributeValueHandle(columnValue));
                        }
                        resultList.add(resultMap);
                    }
                }
            }
        }
        return resultList;
    }

//    @Override
//    public void arrayColumnDataConversion(List<String> arrayColumnList, List<Map<String, Object>> tbodyList) {
//        for (Map<String, Object> rowData : tbodyList) {
//            for (Map.Entry<String, Object> entry : rowData.entrySet()) {
//                if (arrayColumnList.contains(entry.getKey())) {
//                    List<ValueTextVo> valueObjList = new ArrayList<>();
//                    Object valueObj = entry.getValue();
//                    String value = valueObj.getString("value");
//                    if (StringUtils.isNotBlank(value)) {
//                        if (value.startsWith("[") && value.endsWith("]")) {
//                            List<String> valueList = valueObj.getJSONArray("value").toJavaList(String.class);
//                            for (String valueStr : valueList) {
//                                valueObjList.add(new ValueTextVo(valueStr, valueStr));
//                            }
//                        } else {
//                            valueObjList.add(new ValueTextVo(value, value));
//                        }
//                    }
//                    valueObj.put("value", valueObjList);
//                }
//            }
//        }
//    }
}

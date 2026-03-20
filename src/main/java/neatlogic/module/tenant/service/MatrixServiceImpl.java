/*
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package neatlogic.module.tenant.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.crossover.IMatrixCrossoverService;
import neatlogic.framework.matrix.constvalue.SearchExpression;
import neatlogic.framework.matrix.core.IMatrixDataSourceHandler;
import neatlogic.framework.matrix.core.MatrixDataSourceHandlerFactory;
import neatlogic.framework.matrix.core.MatrixPrivateDataSourceHandlerFactory;
import neatlogic.framework.matrix.dao.mapper.MatrixMapper;
import neatlogic.framework.matrix.dto.*;
import neatlogic.framework.matrix.exception.MatrixAttributeNotFoundException;
import neatlogic.framework.matrix.exception.MatrixDataSourceHandlerNotFoundException;
import neatlogic.framework.matrix.exception.MatrixNotFoundException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service
public class MatrixServiceImpl implements MatrixService, IMatrixCrossoverService {

    @Resource
    private MatrixMapper matrixMapper;

    @Override
    public JSONObject searchMatrixColumnDataForSelect(
            String matrixUuid,
            String keywordColumn,
            String valueField,
            String textField,
            List<String> hiddenFieldList,
            List<MatrixFilterVo> filterList,
            String keyword,
            Integer currentPage,
            Integer pageSize,
            Boolean needPage,
            JSONArray defaultValue
    ) {
        MatrixVo matrixVo = MatrixPrivateDataSourceHandlerFactory.getMatrixVo(matrixUuid);
        if (matrixVo == null) {
            matrixVo = matrixMapper.getMatrixByUuid(matrixUuid);
            if (matrixVo == null) {
                throw new MatrixNotFoundException(matrixUuid);
            }
        }
        IMatrixDataSourceHandler matrixDataSourceHandler = MatrixDataSourceHandlerFactory.getHandler(matrixVo.getType());
        if (matrixDataSourceHandler == null) {
            throw new MatrixDataSourceHandlerNotFoundException(matrixVo.getType());
        }

        List<MatrixAttributeVo> matrixAttributeList = matrixDataSourceHandler.getAttributeList(matrixVo);
        if (CollectionUtils.isEmpty(matrixAttributeList)) {
            return new JSONObject();
        }
        MatrixDataVo dataVo = new MatrixDataVo();
        dataVo.setMatrixUuid(matrixUuid);
        List<String> columnList = new ArrayList<>();
        columnList.add(valueField);
        columnList.add(textField);
        if (CollectionUtils.isNotEmpty(hiddenFieldList)) {
            columnList.addAll(hiddenFieldList);
        }
        dataVo.setColumnList(columnList);
        Set<String> notNullColumnSet = new HashSet<>();
        notNullColumnSet.add(valueField);
        notNullColumnSet.add(textField);
        dataVo.setNotNullColumnList(new ArrayList<>(notNullColumnSet));
        dataVo.setFilterList(filterList);
        if (StringUtils.isNotBlank(keyword)) {
            dataVo.setKeyword(keyword);
            if (StringUtils.isNotBlank(keywordColumn)) {
                dataVo.setKeywordColumn(keywordColumn);
            } else {
                dataVo.setKeywordColumn(textField);
            }
        }
        dataVo.setDistinct(true);

        List<Map<String, JSONObject>> resultList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(defaultValue)) {
            List<MatrixDefaultValueFilterVo> defaultValueFilterList = new ArrayList<>();
            for (Object defaultValueObject : defaultValue) {
                if (defaultValueObject instanceof JSONObject defaultValueObj) {
                    String value = defaultValueObj.getString("value");
                    String text = defaultValueObj.getString("text");
                    MatrixDefaultValueFilterVo matrixDefaultValueFilterVo = new MatrixDefaultValueFilterVo(
                            new MatrixKeywordFilterVo(valueField, SearchExpression.EQ.getExpression(), value),
                            new MatrixKeywordFilterVo(textField, SearchExpression.EQ.getExpression(), text)
                    );
                    defaultValueFilterList.add(matrixDefaultValueFilterVo);
                } else if (defaultValueObject instanceof String defaultValueStr) {
                    MatrixDefaultValueFilterVo matrixDefaultValueFilterVo = new MatrixDefaultValueFilterVo(
                            new MatrixKeywordFilterVo(valueField, SearchExpression.EQ.getExpression(), defaultValueStr),
                            null
                    );
                    defaultValueFilterList.add(matrixDefaultValueFilterVo);
                } else {
                    String defaultValueStr = defaultValueObject.toString();
                    MatrixDefaultValueFilterVo matrixDefaultValueFilterVo = new MatrixDefaultValueFilterVo(
                            new MatrixKeywordFilterVo(valueField, SearchExpression.EQ.getExpression(), defaultValueStr),
                            null
                    );
                    defaultValueFilterList.add(matrixDefaultValueFilterVo);
                }
            }
            dataVo.setDefaultValueFilterList(defaultValueFilterList);
            resultList = matrixDataSourceHandler.searchTableDataNew(dataVo);
            deduplicateData(null, valueField, textField, resultList);
        } else {
            List<Map<String, JSONObject>> previousPageList = new ArrayList<>();
            dataVo.setCurrentPage(currentPage);
            dataVo.setPageSize(pageSize);
            int startNum = dataVo.getStartNum();
            int currentPageBackup = dataVo.getCurrentPage();
            if (Objects.equals(needPage, false)) {
                dataVo.setNeedPage(needPage);
                dataVo.getPageSize();
                pageSize = Integer.MAX_VALUE;
            }
            int page = 0;
            while (resultList.size() < pageSize) {
                page++;
                dataVo.setCurrentPage(page);
                List<Map<String, JSONObject>> list = matrixDataSourceHandler.searchTableDataNew(dataVo);
                deduplicateData(previousPageList, valueField, textField, list);
                for (Map<String, JSONObject> element : list) {
                    previousPageList.add(element);
                    if (previousPageList.size() > startNum) {
                        resultList.add(element);
                        if (resultList.size() >= pageSize) {
                            break;
                        }
                    }
                }
                if (page >= dataVo.getPageCount()) {
                    break;
                }
            }
            if (Objects.equals(needPage, false)) {
                dataVo.setPageCount(1);
            }
            dataVo.setCurrentPage(currentPageBackup);
        }
        JSONArray dataList = new JSONArray();
        if (CollectionUtils.isNotEmpty(resultList)) {
            for (Map<String, JSONObject> result : resultList) {
                JSONObject element = new JSONObject();
                JSONObject valueObj = result.get(valueField);
                if (MapUtils.isNotEmpty(valueObj)) {
                    String valueStr = valueObj.getString("value");
                    element.put("value", valueStr);
                }
                JSONObject textObj = result.get(textField);
                if (MapUtils.isNotEmpty(textObj)) {
                    String textStr = textObj.getString("text");
                    element.put("text", textStr);
                }
                for (String hiddenField : hiddenFieldList) {
                    if (StringUtils.isBlank(hiddenField)) {
                        continue;
                    }
                    JSONObject hiddenFieldObj = result.get(hiddenField);
                    if (MapUtils.isNotEmpty(hiddenFieldObj)) {
                        String hiddenFieldValue = hiddenFieldObj.getString("value");
                        element.put(hiddenField, hiddenFieldValue);
                    }
                }
                dataList.add(element);
            }
        }
        JSONObject returnObj = new JSONObject();
        returnObj.put("dataList", dataList);
        returnObj.put("currentPage", dataVo.getCurrentPage());
        returnObj.put("pageSize", dataVo.getPageSize());
        returnObj.put("pageCount", dataVo.getPageCount());
        returnObj.put("rowNum", dataVo.getRowNum());
        return returnObj;
    }

    @Override
    public JSONObject searchMatrixColumnDataForSelect(
            String keyword,
            Integer currentPage,
            Integer pageSize,
            Boolean needPage,
            JSONArray defaultValue,
            String matrixLabel,
            String keywordColumnUniqueIdentifier,
            String valueFieldUniqueIdentifier,
            String textFieldUniqueIdentifier,
            List<String> hiddenFieldUniqueIdentifierList,
            List<MatrixFilterVo> filterList
    ) {
        MatrixVo matrixVo = MatrixPrivateDataSourceHandlerFactory.getMatrixVoByLabel(matrixLabel);
        if (matrixVo == null) {
            matrixVo = matrixMapper.getMatrixByLabel(matrixLabel);
            if (matrixVo == null) {
                throw new MatrixNotFoundException(matrixLabel);
            }
        }
        String matrixUuid = matrixVo.getUuid();
        IMatrixDataSourceHandler matrixDataSourceHandler = MatrixDataSourceHandlerFactory.getHandler(matrixVo.getType());
        if (matrixDataSourceHandler == null) {
            throw new MatrixDataSourceHandlerNotFoundException(matrixVo.getType());
        }

        List<MatrixAttributeVo> matrixAttributeList = matrixDataSourceHandler.getAttributeList(matrixVo);
        if (CollectionUtils.isEmpty(matrixAttributeList)) {
            return new JSONObject();
        }
        Map<String, String> uniqueIdentifierToUuidMap = new HashMap<>();
        for (MatrixAttributeVo matrixAttributeVo : matrixAttributeList) {
            String uniqueIdentifier = matrixAttributeVo.getUniqueIdentifier();
            if (StringUtils.isBlank(uniqueIdentifier)) {
                continue;
            }
            uniqueIdentifierToUuidMap.put(uniqueIdentifier, matrixAttributeVo.getUuid());
        }
        String keywordColumn = uniqueIdentifierToUuidMap.get(keywordColumnUniqueIdentifier);
        if (StringUtils.isBlank(keywordColumn)) {
            throw new MatrixAttributeNotFoundException(matrixVo.getName(), keywordColumnUniqueIdentifier);
        }
        String valueField = uniqueIdentifierToUuidMap.get(valueFieldUniqueIdentifier);
        if (StringUtils.isBlank(valueField)) {
            throw new MatrixAttributeNotFoundException(matrixVo.getName(), valueFieldUniqueIdentifier);
        }
        String textField = uniqueIdentifierToUuidMap.get(textFieldUniqueIdentifier);
        if (StringUtils.isBlank(textField)) {
            throw new MatrixAttributeNotFoundException(matrixVo.getName(), textFieldUniqueIdentifier);
        }
        List<String> hiddenFieldList = new ArrayList<>();
        for (String hiddenFieldUniqueIdentifier : hiddenFieldUniqueIdentifierList) {
            if (StringUtils.isBlank(hiddenFieldUniqueIdentifier)) {
                continue;
            }
            String hiddenField = uniqueIdentifierToUuidMap.get(hiddenFieldUniqueIdentifier);
            if (StringUtils.isBlank(hiddenField)) {
                throw new MatrixAttributeNotFoundException(matrixVo.getName(), hiddenFieldUniqueIdentifier);
            }
            hiddenFieldList.add(hiddenField);
        }
        if (CollectionUtils.isNotEmpty(filterList)) {
            Iterator<MatrixFilterVo> iterator = filterList.iterator();
            while (iterator.hasNext()) {
                MatrixFilterVo matrixFilterVo = iterator.next();
                if (StringUtils.isBlank(matrixFilterVo.getUuid())) {
                    if (StringUtils.isBlank(matrixFilterVo.getUniqueIdentifier())) {
                        iterator.remove();
                        continue;
                    }
                    String attrUuid = uniqueIdentifierToUuidMap.get(matrixFilterVo.getUniqueIdentifier());
                    if (StringUtils.isBlank(attrUuid)) {
                        iterator.remove();
                        continue;
                    }
                    matrixFilterVo.setUuid(attrUuid);
                }
                if (CollectionUtils.isEmpty(matrixFilterVo.getValueList())
                        && !Objects.equals(matrixFilterVo.getExpression(), SearchExpression.NULL.getExpression())
                        && !Objects.equals(matrixFilterVo.getExpression(), SearchExpression.NOTNULL.getExpression())
                ) {
                    iterator.remove();
                }
            }
        }
        return searchMatrixColumnDataForSelect(
                matrixUuid,
                keywordColumn,
                valueField,
                textField,
                hiddenFieldList,
                filterList,
                keyword,
                currentPage,
                pageSize,
                needPage,
                defaultValue);
    }

    private void deduplicateData(List<Map<String, JSONObject>> previousPageList, String valueField, String textField, List<Map<String, JSONObject>> resultList) {
        List<String> duplicateValue = new ArrayList<>();
        List<String> duplicateText = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(previousPageList)) {
            for (Map<String, JSONObject> resultObj : previousPageList) {
                JSONObject firstObj = resultObj.get(valueField);
                if (MapUtils.isEmpty(firstObj)) {
                    continue;
                }
                JSONObject secondObj = resultObj.get(textField);
                if (MapUtils.isEmpty(secondObj)) {
                    continue;
                }
                String value = firstObj.getString("value");
                if (duplicateValue.contains(value)) {
                    continue;
                } else {
                    duplicateValue.add(value);
                }
                String text = secondObj.getString("text");
                if (!duplicateText.contains(text)) {
                    duplicateText.add(text);
                }
            }
        }
        Iterator<Map<String, JSONObject>> iterator = resultList.iterator();
        while (iterator.hasNext()) {
            Map<String, JSONObject> resultObj = iterator.next();
            JSONObject firstObj = resultObj.get(valueField);
            if (MapUtils.isEmpty(firstObj)) {
                iterator.remove();
                continue;
            }
            JSONObject secondObj = resultObj.get(textField);
            if (MapUtils.isEmpty(secondObj)) {
                iterator.remove();
                continue;
            }
            String value = firstObj.getString("value");
            if (StringUtils.isBlank(value)) {
                iterator.remove();
                continue;
            }
            if (duplicateValue.contains(value)) {
                iterator.remove();
                continue;
            } else {
                duplicateValue.add(value);
            }
            String text = secondObj.getString("text");
            if (StringUtils.isBlank(text)) {
                iterator.remove();
                continue;
            }
            if (duplicateText.contains(text)) {
                iterator.remove();
            } else {
                duplicateText.add(text);
            }
        }
    }
}

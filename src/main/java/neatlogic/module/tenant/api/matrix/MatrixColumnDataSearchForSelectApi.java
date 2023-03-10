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

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.common.dto.ValueTextVo;
import neatlogic.framework.matrix.constvalue.SearchExpression;
import neatlogic.framework.matrix.core.IMatrixDataSourceHandler;
import neatlogic.framework.matrix.core.MatrixDataSourceHandlerFactory;
import neatlogic.framework.matrix.core.MatrixPrivateDataSourceHandlerFactory;
import neatlogic.framework.matrix.dao.mapper.MatrixMapper;
import neatlogic.framework.matrix.dto.*;
import neatlogic.framework.matrix.exception.MatrixAttributeNotFoundException;
import neatlogic.framework.matrix.exception.MatrixDataSourceHandlerNotFoundException;
import neatlogic.framework.matrix.exception.MatrixNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class MatrixColumnDataSearchForSelectApi extends PrivateApiComponentBase {

    private String SELECT_COMPOSE_JOINER = "&=&";
    @Resource
    private MatrixMapper matrixMapper;

    @Override
    public String getToken() {
        return "matrix/column/data/search/forselect";
    }

    @Override
    public String getName() {
        return "????????????????????????-????????????";
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
            @Param(name = "keyword", desc = "?????????", type = ApiParamType.STRING, xss = true),
            @Param(name = "matrixUuid", desc = "??????Uuid", type = ApiParamType.STRING, isRequired = true),
            @Param(name = "keywordColumn", desc = "???????????????uuid", type = ApiParamType.STRING),
            @Param(name = "valueField", desc = "value??????uuid", type = ApiParamType.STRING, isRequired = true),
            @Param(name = "textField", desc = "text??????uuid", type = ApiParamType.STRING, isRequired = true),
            @Param(name = "pageSize", desc = "???????????????", type = ApiParamType.INTEGER),
            @Param(name = "defaultValue", desc = "??????????????????????????????", type = ApiParamType.JSONARRAY),
            @Param(name = "filterList", desc = "??????????????????", type = ApiParamType.JSONARRAY)
    })
    @Output({
            @Param(name = "dataList", type = ApiParamType.JSONARRAY, desc = "?????????????????????value???????????????value&=&text?????????value?????????text???????????????"),
            @Param(explode = BasePageVo.class)
    })
    @Description(desc = "????????????????????????-??????????????????")
    @Example(example = "" +
            "{" +
            "\"matrixUuid(??????uuid?????????)\": \"825e6ba09050406eb0de8c4bdcd4e27c\"," +
            "\"columnList(??????????????????????????????????????????)\": [" +
            "\"92196814d8da4ad9bed63e1d650d7e98\"," +
            "\"a4e9978fd06d46d78b13f947a2b1b188\"," +
            "\"cf2c2677c18540a79e60cfd9d531b50c\"," +
            "\"b5d685e9e5fb4ce0baa0604de812e93b\"," +
            "\"6e4abb9b532b49139cec798a3828c7cd\"," +
            "\"3d2f5475138744938f0bb1da1f82002c\"," +
            "\"96d449a58b664a31b32bb1c28090aeee\"" +
            "]," +
            "\"searchColumnList(??????????????????????????????)\": [" +
            "\"92196814d8da4ad9bed63e1d650d7e98\"," +
            "\"a4e9978fd06d46d78b13f947a2b1b188\"" +
            "\t]," +
            "\"currentPage\": 1," +
            "\"pageSize\": 10," +
            "\"sourceColumnList(???????????????????????????)\": [" +
            "{" +
            "\"column(??????uuid?????????)\": \"a4e9978fd06d46d78b13f947a2b1b188\"," +
            "\"expression(????????????????????????)\": \"like|notlike|equal|unequal|include|exclude|between|greater-than|less-than|is-null|match|is-not-null\"," +
            "\"valueList(????????????????????????)\": [" +
            "\"1\"" +
            "]" +
            "}" +
            "]," +
            "\"filterList(?????????????????????????????????)\": [" +
            "{" +
            "\"uuid(??????uuid?????????)\": \"92196814d8da4ad9bed63e1d650d7e98\"," +
            "\"valueList(????????????????????????)\": [" +
            "\"2\"" +
            "]" +
            "}" +
            "]" +
            "}")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        jsonObj.remove("needPage");
        MatrixDataVo dataVo = jsonObj.toJavaObject(MatrixDataVo.class);
        MatrixVo matrixVo = MatrixPrivateDataSourceHandlerFactory.getMatrixVo(dataVo.getMatrixUuid());
        if (matrixVo == null) {
            matrixVo = matrixMapper.getMatrixByUuid(dataVo.getMatrixUuid());
            if (matrixVo == null) {
                throw new MatrixNotFoundException(dataVo.getMatrixUuid());
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
        List<MatrixFilterVo> filterList = dataVo.getFilterList();
        if (CollectionUtils.isNotEmpty(filterList)) {
            Iterator<MatrixFilterVo> iterator = filterList.iterator();
            while (iterator.hasNext()) {
                MatrixFilterVo matrixFilterVo = iterator.next();
                if (StringUtils.isBlank(matrixFilterVo.getUuid())) {
                    iterator.remove();
                } else if (CollectionUtils.isEmpty(matrixFilterVo.getValueList())) {
                    iterator.remove();
                }
            }
        }
        Set<String> notNullColumnSet = new HashSet<>();
        List<String> attributeList = matrixAttributeList.stream().map(MatrixAttributeVo::getUuid).collect(Collectors.toList());
        String valueField = jsonObj.getString("valueField");
        if (!attributeList.contains(valueField)) {
            throw new MatrixAttributeNotFoundException(matrixVo.getName(), valueField);
        }
        notNullColumnSet.add(valueField);
        String textField = jsonObj.getString("textField");
        if (!attributeList.contains(textField)) {
            throw new MatrixAttributeNotFoundException(matrixVo.getName(), textField);
        }
        dataVo.setKeywordColumn(textField);
        notNullColumnSet.add(textField);
        List<String> columnList = new ArrayList<>();
        columnList.add(valueField);
        columnList.add(textField);
        dataVo.setColumnList(columnList);
        dataVo.setNotNullColumnList(new ArrayList<>(notNullColumnSet));
        JSONArray defaultValue = dataVo.getDefaultValue();
        if (CollectionUtils.isNotEmpty(defaultValue)) {
            List<MatrixDefaultValueFilterVo> defaultValueFilterList = new ArrayList<>();
            for (String value : defaultValue.toJavaList(String.class)) {
                if (!value.contains(SELECT_COMPOSE_JOINER)) {
                    continue;
                }
                String[] split = value.split(SELECT_COMPOSE_JOINER);
                MatrixDefaultValueFilterVo matrixDefaultValueFilterVo = new MatrixDefaultValueFilterVo(
                        new MatrixKeywordFilterVo(valueField, SearchExpression.EQ.getExpression(), split[0]),
                        new MatrixKeywordFilterVo(textField, SearchExpression.EQ.getExpression(), split[1])
                );
                defaultValueFilterList.add(matrixDefaultValueFilterVo);
            }
            dataVo.setDefaultValueFilterList(defaultValueFilterList);
            dataVo.setDefaultValue(null);
        }
        List<ValueTextVo> dataList = new ArrayList<>();
        List<Map<String, JSONObject>> resultList = matrixDataSourceHandler.searchTableDataNew(dataVo);
        deduplicateData(valueField, textField, resultList);
        if (CollectionUtils.isNotEmpty(resultList)) {
            for (Map<String, JSONObject> result : resultList) {
                String valueStr = null;
                JSONObject valueObj = result.get(valueField);
                if (MapUtils.isNotEmpty(valueObj)) {
                    valueStr = valueObj.getString("value");
                }
                String textStr = null;
                JSONObject textObj = result.get(textField);
                if (MapUtils.isNotEmpty(textObj)) {
                    textStr = textObj.getString("text");
                }
                dataList.add(new ValueTextVo(valueStr + "&=&" + textStr, textStr));
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

    private void deduplicateData(String valueField, String textField, List<Map<String, JSONObject>> resultList) {
        List<String> exsited = new ArrayList<>();
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
            String firstValue = firstObj.getString("value");
            String secondText = secondObj.getString("text");
            String compose = firstValue + SELECT_COMPOSE_JOINER + secondText;
            if (exsited.contains(compose)) {
                iterator.remove();
            } else {
                exsited.add(compose);
            }
        }
    }
}

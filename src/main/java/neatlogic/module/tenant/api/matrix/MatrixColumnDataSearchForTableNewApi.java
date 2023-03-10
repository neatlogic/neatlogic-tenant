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
import neatlogic.framework.util.TableResultUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class MatrixColumnDataSearchForTableNewApi extends PrivateApiComponentBase {

    @Resource
    private MatrixMapper matrixMapper;

    @Override
    public String getToken() {
        return "matrix/column/data/search/fortable/new";
    }

    @Override
    public String getName() {
        return "????????????????????????-table??????";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "matrixUuid", desc = "??????Uuid", type = ApiParamType.STRING, isRequired = true),
            @Param(name = "defaultValue", desc = "?????????????????????uuid??????", type = ApiParamType.JSONARRAY),
            @Param(name = "columnList", desc = "??????????????????????????????????????????????????????", type = ApiParamType.JSONARRAY, isRequired = true, minSize = 1),
            @Param(name = "searchColumnList ", desc = "??????????????????", type = ApiParamType.JSONARRAY),
            @Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "???????????????????????????true"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "????????????"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "?????????"),
            @Param(name = "filterList", desc = "??????????????????", type = ApiParamType.JSONARRAY)
    })
    @Description(desc = "????????????????????????-table??????")
    @Output({
            @Param(name = "tbodyList", type = ApiParamType.JSONARRAY, desc = "??????????????????"),
            @Param(name = "theadList", type = ApiParamType.JSONARRAY, desc = "??????????????????"),
            @Param(name = "searchColumnDetailList", type = ApiParamType.JSONARRAY, desc = "????????????????????????"),
            @Param(name = "type", type = ApiParamType.STRING, desc = "????????????"),
            @Param(explode = BasePageVo.class)
    })
    @Example(example = "" +
            "{" +
            "\"matrixUuid(??????uuid?????????)\": \"825e6ba09050406eb0de8c4bdcd4e27c\"," +
            "\"uuidColumn(uuid????????????????????????)\": \"uuid\"," +
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
        MatrixDataVo dataVo = jsonObj.toJavaObject(MatrixDataVo.class);
        List<String> columnList = dataVo.getColumnList();

        MatrixVo matrixVo = MatrixPrivateDataSourceHandlerFactory.getMatrixVo(dataVo.getMatrixUuid());
        if (matrixVo == null) {
            matrixVo = matrixMapper.getMatrixByUuid(dataVo.getMatrixUuid());
            if (matrixVo == null) {
                throw new MatrixNotFoundException(dataVo.getMatrixUuid());
            }
        }
        String type = matrixVo.getType();
        IMatrixDataSourceHandler matrixDataSourceHandler = MatrixDataSourceHandlerFactory.getHandler(type);
        if (matrixDataSourceHandler == null) {
            throw new MatrixDataSourceHandlerNotFoundException(type);
        }
        List<MatrixFilterVo> filterList = dataVo.getFilterList();
        if (CollectionUtils.isNotEmpty(filterList)) {
            Iterator<MatrixFilterVo> iterator = filterList.iterator();
            while (iterator.hasNext()) {
                MatrixFilterVo matrixFilterVo = iterator.next();
                if (StringUtils.isBlank(matrixFilterVo.getUuid())) {
                    iterator.remove();
                } else if (CollectionUtils.isEmpty(matrixFilterVo.getValueList())
                        && !Objects.equals(matrixFilterVo.getExpression(), SearchExpression.NULL.getExpression())
                        && !Objects.equals(matrixFilterVo.getExpression(), SearchExpression.NOTNULL.getExpression())
                ) {
                    iterator.remove();
                }
            }
        }
        List<MatrixAttributeVo> matrixAttributeList = matrixDataSourceHandler.getAttributeList(matrixVo);
        if (CollectionUtils.isEmpty(matrixAttributeList)) {
            return new JSONObject();
        }
        List<String> attributeUuidList = matrixAttributeList.stream().map(MatrixAttributeVo::getUuid).collect(Collectors.toList());
        List<String> notFoundColumnList = ListUtils.removeAll(columnList, attributeUuidList);
        if (CollectionUtils.isNotEmpty(notFoundColumnList)) {
            throw new MatrixAttributeNotFoundException(matrixVo.getName(), String.join(",", notFoundColumnList));
        }
        List<Map<String, JSONObject>> tbodyList = matrixDataSourceHandler.searchTableDataNew(dataVo);
        JSONArray theadList = getTheadList(dataVo.getMatrixUuid(), matrixAttributeList, dataVo.getColumnList());
        JSONObject returnObj = TableResultUtil.getResult(theadList, tbodyList, dataVo);
        JSONArray searchColumnArray = jsonObj.getJSONArray("searchColumnList");
        returnObj.put("searchColumnDetailList", getSearchColumnDetailList(dataVo.getMatrixUuid(), matrixAttributeList, searchColumnArray));
        returnObj.put("type", type);
        return returnObj;
    }

    private List<MatrixAttributeVo> getSearchColumnDetailList(String matrixUuid, List<MatrixAttributeVo> attributeList, JSONArray searchColumnArray) {
        if (CollectionUtils.isNotEmpty(searchColumnArray)) {
            Map<String, MatrixAttributeVo> attributeMap = new HashMap<>();
            for (MatrixAttributeVo attribute : attributeList) {
                attributeMap.put(attribute.getUuid(), attribute);
            }
            List<MatrixAttributeVo> searchColumnDetailList = new ArrayList<>();
            List<String> searchColumnList = searchColumnArray.toJavaList(String.class);
            for (String column : searchColumnList) {
                MatrixAttributeVo attribute = attributeMap.get(column);
                if (attribute == null) {
                    throw new MatrixAttributeNotFoundException(matrixUuid, column);
                }
                searchColumnDetailList.add(attribute);
            }
            return searchColumnDetailList;
        }
        return null;
    }

    private JSONArray getTheadList(String matrixUuid, List<MatrixAttributeVo> attributeList, List<String> columnList) {
        Map<String, MatrixAttributeVo> attributeMap = new HashMap<>();
        for (MatrixAttributeVo attribute : attributeList) {
            attributeMap.put(attribute.getUuid(), attribute);
        }
        JSONArray theadList = new JSONArray();
        for (String column : columnList) {
            MatrixAttributeVo attribute = attributeMap.get(column);
            if (attribute == null) {
                throw new MatrixAttributeNotFoundException(matrixUuid, column);
            }
            JSONObject theadObj = new JSONObject();
            theadObj.put("key", attribute.getUuid());
            theadObj.put("title", attribute.getName());
            theadList.add(theadObj);
        }
        return theadList;
    }
}

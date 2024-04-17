/*Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

package neatlogic.module.tenant.api.matrix;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.exception.type.ParamNotExistsException;
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
        return "矩阵属性数据查询-table接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "matrixUuid", desc = "矩阵Uuid", type = ApiParamType.STRING),
            @Param(name = "defaultValue", desc = "需要回显的数据uuid集合", type = ApiParamType.JSONARRAY),
            @Param(name = "columnList", desc = "目标属性集合，数据按这个字段顺序返回", type = ApiParamType.JSONARRAY, minSize = 1),
            @Param(name = "searchColumnList ", desc = "搜索属性集合", type = ApiParamType.JSONARRAY),
            @Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否需要分页，默认true"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页条目"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页"),
            @Param(name = "filterList", desc = "过滤条件集合", type = ApiParamType.JSONARRAY),

            @Param(name = "matrixLabel", desc = "矩阵名", type = ApiParamType.STRING),
            @Param(name = "columnNameList", desc = "目标属性集合，数据按这个字段顺序返回", type = ApiParamType.JSONARRAY, minSize = 1),
            @Param(name = "isAllColumn", desc = "是否返回所有属性", type = ApiParamType.INTEGER),
    })
    @Description(desc = "矩阵属性数据查询-table接口")
    @Output({
            @Param(name = "tbodyList", type = ApiParamType.JSONARRAY, desc = "属性数据集合"),
            @Param(name = "theadList", type = ApiParamType.JSONARRAY, desc = "属性列名集合"),
            @Param(name = "searchColumnDetailList", type = ApiParamType.JSONARRAY, desc = "搜索属性详情集合"),
            @Param(name = "type", type = ApiParamType.STRING, desc = "矩阵类型"),
            @Param(explode = BasePageVo.class)
    })
    @Example(example = "" +
            "{" +
            "\"matrixUuid(矩阵uuid，必填)\": \"825e6ba09050406eb0de8c4bdcd4e27c\"," +
            "\"uuidColumn(uuid对应的属性，选填)\": \"uuid\"," +
            "\"columnList(需要返回数据的字段列表，必填)\": [" +
            "\"92196814d8da4ad9bed63e1d650d7e98\"," +
            "\"a4e9978fd06d46d78b13f947a2b1b188\"," +
            "\"cf2c2677c18540a79e60cfd9d531b50c\"," +
            "\"b5d685e9e5fb4ce0baa0604de812e93b\"," +
            "\"6e4abb9b532b49139cec798a3828c7cd\"," +
            "\"3d2f5475138744938f0bb1da1f82002c\"," +
            "\"96d449a58b664a31b32bb1c28090aeee\"" +
            "]," +
            "\"searchColumnList(可搜索字段列表，选填)\": [" +
            "\"92196814d8da4ad9bed63e1d650d7e98\"," +
            "\"a4e9978fd06d46d78b13f947a2b1b188\"" +
            "\t]," +
            "\"currentPage\": 1," +
            "\"pageSize\": 10," +
            "\"sourceColumnList(过滤条件列表，选填)\": [" +
            "{" +
            "\"column(字段uuid，必填)\": \"a4e9978fd06d46d78b13f947a2b1b188\"," +
            "\"expression(过滤表达式，必填)\": \"like|notlike|equal|unequal|include|exclude|between|greater-than|less-than|is-null|match|is-not-null\"," +
            "\"valueList(过滤值列表，必填)\": [" +
            "\"1\"" +
            "]" +
            "}" +
            "]," +
            "\"filterList(联动过滤条件列表，选填)\": [" +
            "{" +
            "\"uuid(字段uuid，必填)\": \"92196814d8da4ad9bed63e1d650d7e98\"," +
            "\"valueList(过滤值列表，必填)\": [" +
            "\"2\"" +
            "]" +
            "}" +
            "]" +
            "}")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        MatrixDataVo dataVo = jsonObj.toJavaObject(MatrixDataVo.class);
        if (StringUtils.isBlank(dataVo.getMatrixUuid()) && StringUtils.isBlank(dataVo.getMatrixLabel())) {
            throw new ParamNotExistsException("matrixUuid", "matrixLabel");
        }
        MatrixVo matrixVo = null;
        if (StringUtils.isNotBlank(dataVo.getMatrixUuid())) {
            matrixVo = MatrixPrivateDataSourceHandlerFactory.getMatrixVo(dataVo.getMatrixUuid());
            if (matrixVo == null) {
                matrixVo = matrixMapper.getMatrixByUuid(dataVo.getMatrixUuid());
                if (matrixVo == null) {
                    throw new MatrixNotFoundException(dataVo.getMatrixUuid());
                }
            }
        } else if (StringUtils.isNotBlank(dataVo.getMatrixLabel())) {
            matrixVo = MatrixPrivateDataSourceHandlerFactory.getMatrixVoByLabel(dataVo.getMatrixLabel());
            if (matrixVo == null) {
                matrixVo = matrixMapper.getMatrixByLabel(dataVo.getMatrixLabel());
                if (matrixVo == null) {
                    throw new MatrixNotFoundException(dataVo.getMatrixLabel());
                }
            }
            dataVo.setMatrixUuid(matrixVo.getUuid());
        }

        String type = matrixVo.getType();
        IMatrixDataSourceHandler matrixDataSourceHandler = MatrixDataSourceHandlerFactory.getHandler(type);
        if (matrixDataSourceHandler == null) {
            throw new MatrixDataSourceHandlerNotFoundException(type);
        }
        List<MatrixAttributeVo> matrixAttributeList = matrixDataSourceHandler.getAttributeList(matrixVo);
        if (CollectionUtils.isEmpty(matrixAttributeList)) {
            return new JSONObject();
        }
        Map<String, String> nameToUuidMap = new HashMap<>();
        for (MatrixAttributeVo matrixAttributeVo : matrixAttributeList) {
            String name = matrixAttributeVo.getName();
            if(nameToUuidMap.containsKey(name)) {
                nameToUuidMap.remove(name);
            } else {
                nameToUuidMap.put(name, matrixAttributeVo.getUuid());
            }
        }
        List<MatrixFilterVo> filterList = dataVo.getFilterList();
        if (CollectionUtils.isNotEmpty(filterList)) {
            Iterator<MatrixFilterVo> iterator = filterList.iterator();
            while (iterator.hasNext()) {
                MatrixFilterVo matrixFilterVo = iterator.next();
                if (StringUtils.isBlank(matrixFilterVo.getUuid())) {
                    if (StringUtils.isBlank(matrixFilterVo.getName())) {
                        iterator.remove();
                        continue;
                    }
                    String attrUuid = nameToUuidMap.get(matrixFilterVo.getName());
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

        List<String> attributeUuidList = matrixAttributeList.stream().map(MatrixAttributeVo::getUuid).collect(Collectors.toList());
        List<String> columnNameList = dataVo.getColumnNameList();
        Integer isAllColumn = jsonObj.getInteger("isAllColumn");
        if (Objects.equals(isAllColumn, 1)) {
            dataVo.setColumnList(attributeUuidList);
        } else {
            if (CollectionUtils.isNotEmpty(dataVo.getColumnList())) {
                dataVo.setColumnNameList(null);
                List<String> notFoundColumnList = ListUtils.removeAll(dataVo.getColumnList(), attributeUuidList);
                if (CollectionUtils.isNotEmpty(notFoundColumnList)) {
                    throw new MatrixAttributeNotFoundException(matrixVo.getName(), String.join(",", notFoundColumnList));
                }
            } else if (CollectionUtils.isNotEmpty(columnNameList)) {
                List<String> columnList = new ArrayList<>();
                List<String> notFoundColumnList = new ArrayList<>();
                for (String columnName : columnNameList) {
                    if (StringUtils.isBlank(columnName)) {
                        continue;
                    }
                    String column = nameToUuidMap.get(columnName);
                    if (StringUtils.isBlank(column)) {
                        notFoundColumnList.add(columnName);
                    } else {
                        columnList.add(column);
                    }
                }
                if (CollectionUtils.isNotEmpty(notFoundColumnList)) {
                    throw new MatrixAttributeNotFoundException(matrixVo.getName(), String.join(",", notFoundColumnList));
                }
                dataVo.setColumnList(columnList);
            }
        }
        List<Map<String, JSONObject>> tbodyList = matrixDataSourceHandler.searchTableDataNew(dataVo);
        if (CollectionUtils.isNotEmpty(columnNameList)) {
            List<Map<String, JSONObject>> newTbodyList = new ArrayList<>();
            for (Map<String, JSONObject> tbody : tbodyList) {
                Map<String, JSONObject> newTbody = new HashMap<>();
                for (String columnName : columnNameList) {
                    String attrUuid = nameToUuidMap.get(columnName);
                    if (StringUtils.isBlank(attrUuid)) {
                        continue;
                    }
                    JSONObject jsonObject = tbody.get(attrUuid);
                    if (jsonObject == null) {
                        continue;
                    }
                    newTbody.put(columnName, jsonObject);
                }
                newTbodyList.add(newTbody);
            }
            tbodyList = newTbodyList;
        }
        JSONArray theadList = getTheadList(dataVo.getMatrixUuid(), matrixAttributeList, dataVo.getColumnList(), columnNameList);
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

    private JSONArray getTheadList(String matrixUuid, List<MatrixAttributeVo> attributeList, List<String> columnList, List<String> columnNameList) {
        if (columnNameList == null) {
            columnNameList = new ArrayList<>();
        }
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
            if (columnNameList.contains(attribute.getName())) {
                theadObj.put("key", attribute.getName());
            } else {
                theadObj.put("key", attribute.getUuid());
            }
            theadObj.put("title", attribute.getName());
            theadList.add(theadObj);
        }
        return theadList;
    }
}

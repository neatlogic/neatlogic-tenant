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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.exception.type.ParamIrregularException;
import neatlogic.framework.matrix.core.IMatrixDataSourceHandler;
import neatlogic.framework.matrix.core.MatrixDataSourceHandlerFactory;
import neatlogic.framework.matrix.core.MatrixPrivateDataSourceHandlerFactory;
import neatlogic.framework.matrix.dao.mapper.MatrixMapper;
import neatlogic.framework.matrix.dto.MatrixAttributeVo;
import neatlogic.framework.matrix.dto.MatrixColumnVo;
import neatlogic.framework.matrix.dto.MatrixDataVo;
import neatlogic.framework.matrix.dto.MatrixVo;
import neatlogic.framework.matrix.exception.MatrixAttributeNotFoundException;
import neatlogic.framework.matrix.exception.MatrixDataSourceHandlerNotFoundException;
import neatlogic.framework.matrix.exception.MatrixNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;
import java.util.*;

//@Service
@Deprecated
@OperationType(type = OperationTypeEnum.SEARCH)
public class MatrixColumnDataSearchForTableApi extends PrivateApiComponentBase {

    @Resource
    private MatrixMapper matrixMapper;

    @Override
    public String getToken() {
        return "matrix/column/data/search/fortable";
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
            @Param(name = "matrixUuid", desc = "矩阵Uuid", type = ApiParamType.STRING, isRequired = true),
            @Param(name = "defaultValue", desc = "需要回显的数据uuid集合", type = ApiParamType.JSONARRAY),
            @Param(name = "uuidColumn", desc = "uuid对应的属性", type = ApiParamType.STRING),
            @Param(name = "columnList", desc = "目标属性集合，数据按这个字段顺序返回", type = ApiParamType.JSONARRAY, isRequired = true),
            @Param(name = "searchColumnList ", desc = "搜索属性集合", type = ApiParamType.JSONARRAY),
            @Param(name = "sourceColumnList", desc = "搜索过滤值集合", type = ApiParamType.JSONARRAY),//TODO 前端传入是List<String>，而MatrixDataVo对象中sourceColumnList是List<MatrixColumnVo>
            @Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否需要分页，默认true"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页条目"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页"),
            @Param(name = "arrayColumnList", desc = "需要将值转化成数组的属性集合", type = ApiParamType.JSONARRAY),
            @Param(name = "attrFilterList", desc = "配置项矩阵属性过滤条件", type = ApiParamType.JSONARRAY),
            @Param(name = "relFilterList", desc = "配置项矩阵关系过滤条件", type = ApiParamType.JSONARRAY),
            @Param(name = "filterCiEntityId", desc = "配置项矩阵id过滤条件", type = ApiParamType.LONG),
            @Param(name = "filterCiId", desc = "配置项矩阵子模型过滤条件", type = ApiParamType.LONG),
            @Param(name = "filterList", desc = "联动过滤数据集合", type = ApiParamType.JSONARRAY)
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
        List<String> columnList = dataVo.getColumnList();
        if (CollectionUtils.isEmpty(columnList)) {
            throw new ParamIrregularException("columnList");
        }
        MatrixVo matrixVo = MatrixPrivateDataSourceHandlerFactory.getMatrixVo(dataVo.getMatrixUuid());
        if (matrixVo == null) {
            matrixVo = matrixMapper.getMatrixByUuid(dataVo.getMatrixUuid());
            if (matrixVo == null) {
                throw new MatrixNotFoundException(dataVo.getMatrixUuid());
            }
        }
        JSONArray searchColumnArray = jsonObj.getJSONArray("searchColumnList");
        JSONArray defaultValue = dataVo.getDefaultValue();
        if (CollectionUtils.isEmpty(defaultValue)) {
            JSONArray uuidList = jsonObj.getJSONArray("uuidList");
            dataVo.setDefaultValue(uuidList);
        }
        String type = matrixVo.getType();
        IMatrixDataSourceHandler matrixDataSourceHandler = MatrixDataSourceHandlerFactory.getHandler(type);
        if (matrixDataSourceHandler == null) {
            throw new MatrixDataSourceHandlerNotFoundException(type);
        }
        List<MatrixColumnVo> sourceColumnList = dataVo.getSourceColumnList();
        if (CollectionUtils.isNotEmpty(sourceColumnList)) {
            Iterator<MatrixColumnVo> iterator = sourceColumnList.iterator();
            while (iterator.hasNext()) {
                MatrixColumnVo matrixColumnVo = iterator.next();
                if (StringUtils.isBlank(matrixColumnVo.getColumn())) {
                    iterator.remove();
                } else if (CollectionUtils.isEmpty(matrixColumnVo.getValueList())) {
                    iterator.remove();
                }
            }
        }
        JSONObject returnObj = matrixDataSourceHandler.searchTableData(dataVo);
        List<MatrixAttributeVo> matrixAttributeList = matrixDataSourceHandler.getAttributeList(matrixVo);
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
}

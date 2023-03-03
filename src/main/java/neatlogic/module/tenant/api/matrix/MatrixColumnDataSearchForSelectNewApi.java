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
import neatlogic.framework.matrix.core.IMatrixDataSourceHandler;
import neatlogic.framework.matrix.core.MatrixDataSourceHandlerFactory;
import neatlogic.framework.matrix.core.MatrixPrivateDataSourceHandlerFactory;
import neatlogic.framework.matrix.dao.mapper.MatrixMapper;
import neatlogic.framework.matrix.dto.MatrixColumnVo;
import neatlogic.framework.matrix.dto.MatrixDataVo;
import neatlogic.framework.matrix.dto.MatrixVo;
import neatlogic.framework.matrix.exception.MatrixDataSourceHandlerNotFoundException;
import neatlogic.framework.matrix.exception.MatrixNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Deprecated
@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class MatrixColumnDataSearchForSelectNewApi extends PrivateApiComponentBase {

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
            @Param(name = "sourceColumnList", desc = "搜索过滤值集合", type = ApiParamType.JSONARRAY),
            @Param(name = "pageSize", desc = "显示条目数", type = ApiParamType.INTEGER),
            @Param(name = "defaultValue", desc = "精确匹配回显数据参数", type = ApiParamType.JSONARRAY),
            @Param(name = "attrFilterList", desc = "配置项矩阵属性过滤条件", type = ApiParamType.JSONARRAY),
            @Param(name = "relFilterList", desc = "配置项矩阵关系过滤条件", type = ApiParamType.JSONARRAY),
            @Param(name = "filterList", desc = "联动过滤数据集合", type = ApiParamType.JSONARRAY)
    })
    @Output({
            @Param(name = "tbodyList", type = ApiParamType.JSONARRAY, desc = "属性数据集合")
    })
    @Description(desc = "矩阵属性数据查询-下拉级联接口")
    @Example(example = "" +
            "{" +
            "\"matrixUuid(矩阵uuid，必填)\": \"825e6ba09050406eb0de8c4bdcd4e27c\"," +
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
        List<Map<String, JSONObject>> resultList = matrixDataSourceHandler.searchTableColumnData(dataVo);
        JSONObject returnObj = new JSONObject();
        returnObj.put("columnDataList", resultList);//TODO linbq 等前端改完再删
        returnObj.put("tbodyList", resultList);
        returnObj.put("currentPage", dataVo.getCurrentPage());
        returnObj.put("pageSize", dataVo.getPageSize());
        returnObj.put("pageCount", dataVo.getPageCount());
        returnObj.put("rowNum", dataVo.getRowNum());
        return returnObj;
    }
}

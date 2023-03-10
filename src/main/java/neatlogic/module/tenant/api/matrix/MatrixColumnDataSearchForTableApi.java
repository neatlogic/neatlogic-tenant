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
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service
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
        return "????????????????????????-table??????";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "matrixUuid", desc = "??????Uuid", type = ApiParamType.STRING, isRequired = true),
            @Param(name = "defaultValue", desc = "?????????????????????uuid??????", type = ApiParamType.JSONARRAY),
            @Param(name = "uuidColumn", desc = "uuid???????????????", type = ApiParamType.STRING),
            @Param(name = "columnList", desc = "??????????????????????????????????????????????????????", type = ApiParamType.JSONARRAY, isRequired = true),
            @Param(name = "searchColumnList ", desc = "??????????????????", type = ApiParamType.JSONARRAY),
            @Param(name = "sourceColumnList", desc = "?????????????????????", type = ApiParamType.JSONARRAY),//TODO ???????????????List<String>??????MatrixDataVo?????????sourceColumnList???List<MatrixColumnVo>
            @Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "???????????????????????????true"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "????????????"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "?????????"),
            @Param(name = "arrayColumnList", desc = "??????????????????????????????????????????", type = ApiParamType.JSONARRAY),
            @Param(name = "attrFilterList", desc = "?????????????????????????????????", type = ApiParamType.JSONARRAY),
            @Param(name = "relFilterList", desc = "?????????????????????????????????", type = ApiParamType.JSONARRAY),
            @Param(name = "filterCiEntityId", desc = "???????????????id????????????", type = ApiParamType.LONG),
            @Param(name = "filterCiId", desc = "????????????????????????????????????", type = ApiParamType.LONG),
            @Param(name = "filterList", desc = "????????????????????????", type = ApiParamType.JSONARRAY)
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

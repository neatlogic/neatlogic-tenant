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
import neatlogic.framework.matrix.dao.mapper.*;
import neatlogic.framework.matrix.dto.*;
import neatlogic.framework.matrix.exception.*;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service
@Deprecated //这个接口被MatrixColumnDataSearchForTableApi替代
@OperationType(type = OperationTypeEnum.SEARCH)
public class MatrixColumnDataInitForTableApi extends PrivateApiComponentBase {

    private final static Logger logger = LoggerFactory.getLogger(MatrixColumnDataInitForTableApi.class);

    @Resource
    private MatrixMapper matrixMapper;

    @Override
    public String getToken() {
        return "matrix/column/data/init/fortable";
    }

    @Override
    public String getName() {
        return "矩阵属性数据回显-table接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "matrixUuid", desc = "矩阵Uuid", type = ApiParamType.STRING, isRequired = true),
            @Param(name = "columnList", desc = "目标属性集合，数据按这个字段顺序返回", type = ApiParamType.JSONARRAY, isRequired = true),
            @Param(name = "uuidList", desc = "需要回显的数据uuid集合", type = ApiParamType.JSONARRAY),
            @Param(name = "uuidColumn", desc = "uuid对应的属性", type = ApiParamType.STRING),
            @Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否需要分页，默认true"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页条目"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页"),
            @Param(name = "arrayColumnList", desc = "需要将值转化成数组的属性集合", type = ApiParamType.JSONARRAY)
    })
    @Description(desc = "矩阵属性数据回显-table接口")
    @Output({
            @Param(name = "tbodyList", type = ApiParamType.JSONARRAY, desc = "属性数据集合"),
            @Param(name = "theadList", type = ApiParamType.JSONARRAY, desc = "属性列名集合"),
            @Param(name = "type", type = ApiParamType.STRING, desc = "矩阵类型"),
            @Param(explode = BasePageVo.class)
    })
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        MatrixDataVo dataVo = JSONObject.toJavaObject(jsonObj, MatrixDataVo.class);
        List<String> columnList = dataVo.getColumnList();
        if (CollectionUtils.isEmpty(columnList)) {
            throw new ParamIrregularException("columnList");
        }
        MatrixVo matrixVo = matrixMapper.getMatrixByUuid(dataVo.getMatrixUuid());
        if (matrixVo == null) {
            throw new MatrixNotFoundException(dataVo.getMatrixUuid());
        }
        JSONArray defaultValue = dataVo.getDefaultValue();
        if (CollectionUtils.isEmpty(defaultValue)) {
            JSONArray uuidList = jsonObj.getJSONArray("uuidList");
            dataVo.setDefaultValue(uuidList);
        }
        String type = matrixVo.getType();
//        if (MatrixType.CUSTOM.getValue().equals(type)) {
//            List<MatrixAttributeVo> matrixAttributeList = matrixAttributeMapper.getMatrixAttributeByMatrixUuid(dataVo.getMatrixUuid());
//            if (CollectionUtils.isNotEmpty(matrixAttributeList)) {
//                if (dataVo.getNeedPage()) {
//                    int rowNum = matrixDataMapper.getDynamicTableDataByUuidCount(dataVo);
//                    dataVo.setRowNum(rowNum);
//                }
//                List<Map<String, String>> dataMapList = matrixDataMapper.getDynamicTableDataByUuidList(dataVo);
//                List<Map<String, Object>> tbodyList = matrixService.matrixTableDataValueHandle(matrixAttributeList, dataMapList);
//                JSONArray theadList = getTheadList(dataVo.getMatrixUuid(), matrixAttributeList, columnList);
//                returnObj = TableResultUtil.getResult(theadList, tbodyList, dataVo);
//            }
//        } else if (MatrixType.VIEW.getValue().equals(type)) {
//            MatrixViewVo matrixViewVo = viewMapper.getMatrixViewByMatrixUuid(dataVo.getMatrixUuid());
//            if (matrixViewVo == null) {
//                throw new MatrixViewNotFoundException(matrixVo.getName());
//            }
//            JSONArray attributeList = (JSONArray) JSONPath.read(matrixViewVo.getConfig(), "attributeList");
//            if (CollectionUtils.isNotEmpty(attributeList)) {
//                List<MatrixAttributeVo> matrixAttributeList = attributeList.toJavaList(MatrixAttributeVo.class);
//                List<Map<String, String>> dataMapList = matrixDataMapper.getDynamicTableDataByUuidList(dataVo);
//                List<Map<String, Object>> tbodyList = matrixService.matrixTableDataValueHandle(matrixAttributeList, dataMapList);
//                if (dataVo.getNeedPage()) {
//                    int rowNum = matrixDataMapper.getDynamicTableDataByUuidCount(dataVo);
//                    dataVo.setRowNum(rowNum);
//                }
//                JSONArray theadList = getTheadList(dataVo.getMatrixUuid(), matrixAttributeList, columnList);
//                returnObj = TableResultUtil.getResult(theadList, tbodyList, dataVo);
//            }
//        } else if (MatrixType.EXTERNAL.getValue().equals(type)) {
//            MatrixExternalVo externalVo = matrixExternalMapper.getMatrixExternalByMatrixUuid(dataVo.getMatrixUuid());
//            if (externalVo == null) {
//                throw new MatrixExternalNotFoundException(matrixVo.getName());
//            }
//            IntegrationVo integrationVo = integrationMapper.getIntegrationByUuid(externalVo.getIntegrationUuid());
//            if (integrationVo == null) {
//                throw new IntegrationNotFoundException(externalVo.getIntegrationUuid());
//            }
//            IIntegrationHandler handler = IntegrationHandlerFactory.getHandler(integrationVo.getHandler());
//            if (handler == null) {
//                throw new IntegrationHandlerNotFoundException(integrationVo.getHandler());
//            }
//
//            List<MatrixAttributeVo> matrixAttributeList = matrixService.getExternalMatrixAttributeList(dataVo.getMatrixUuid(), integrationVo);
//            if (CollectionUtils.isNotEmpty(matrixAttributeList)) {
//                JSONArray theadList = getTheadList(dataVo.getMatrixUuid(), matrixAttributeList, columnList);
//                returnObj.put("theadList", theadList);
//                List<Map<String, JSONObject>> tbodyList = new ArrayList<>();
//                integrationVo.getParamObj().putAll(jsonObj);
//                List<String> uuidList = dataVo.getUuidList();
//                if (CollectionUtils.isNotEmpty(uuidList)) {
//                    String uuidColumn = jsonObj.getString("uuidColumn");
//                    boolean uuidColumnExist = false;
//                    for (MatrixAttributeVo matrixAttributeVo : matrixAttributeList) {
//                        if (Objects.equals(matrixAttributeVo.getUuid(), uuidColumn)) {
//                            uuidColumnExist = true;
//                        }
//                    }
//                    if (!uuidColumnExist) {
//                        throw new MatrixAttributeNotFoundException(dataVo.getMatrixUuid(), uuidColumn);
//                    }
//                    List<MatrixColumnVo> sourceColumnList = new ArrayList<>();
//                    MatrixColumnVo sourceColumnVo = new MatrixColumnVo();
//                    sourceColumnVo.setColumn(uuidColumn);
//                    for (String uuidValue : uuidList) {
//                        sourceColumnVo.setValue(uuidValue);
//                        sourceColumnVo.setExpression(Expression.EQUAL.getExpression());
//                        sourceColumnList.clear();
//                        sourceColumnList.add(sourceColumnVo);
//                        integrationVo.getParamObj().put("sourceColumnList", sourceColumnList);
//                        IntegrationResultVo resultVo = handler.sendRequest(integrationVo, RequestFrom.MATRIX);
//                        if (StringUtils.isNotBlank(resultVo.getError())) {
//                            logger.error(resultVo.getError());
//                            throw new MatrixExternalAccessException();
//                        }
//                        handler.validate(resultVo);
//                        tbodyList.addAll(matrixService.getExternalDataTbodyList(resultVo, dataVo.getColumnList()));
//                    }
//                    returnObj.put("tbodyList", tbodyList);
//                } else {
//                    IntegrationResultVo resultVo = handler.sendRequest(integrationVo, RequestFrom.MATRIX);
//                    if (StringUtils.isNotBlank(resultVo.getError())) {
//                        logger.error(resultVo.getError());
//                        throw new MatrixExternalAccessException();
//                    }
//                    handler.validate(resultVo);
//                    JSONObject transformedResult = JSONObject.parseObject(resultVo.getTransformedResult());
//                    returnObj.put("currentPage", transformedResult.get("currentPage"));
//                    returnObj.put("pageSize", transformedResult.get("pageSize"));
//                    returnObj.put("pageCount", transformedResult.get("pageCount"));
//                    returnObj.put("rowNum", transformedResult.get("rowNum"));
//                    tbodyList = matrixService.getExternalDataTbodyList(resultVo, dataVo.getColumnList());
//                }
//                /** 将arrayColumnList包含的属性值转成数组 **/
//                JSONArray arrayColumnArray = jsonObj.getJSONArray("arrayColumnList");
//                if (CollectionUtils.isNotEmpty(arrayColumnArray)) {
//                    List<String> arrayColumnList = arrayColumnArray.toJavaList(String.class);
//                    if (CollectionUtils.isNotEmpty(tbodyList)) {
//                        matrixService.arrayColumnDataConversion(arrayColumnList, tbodyList);
//                    }
//                }
//            }
//        }
        IMatrixDataSourceHandler matrixDataSourceHandler = MatrixDataSourceHandlerFactory.getHandler(type);
        if (matrixDataSourceHandler == null) {
            throw new MatrixDataSourceHandlerNotFoundException(type);
        }
        JSONObject returnObj = matrixDataSourceHandler.searchTableData(dataVo);
        returnObj.put("type", type);
        return returnObj;
    }

//    private JSONArray getTheadList(String matrixUuid, List<MatrixAttributeVo> attributeList, List<String> columnList) {
//        Map<String, MatrixAttributeVo> attributeMap = new HashMap<>();
//        for (MatrixAttributeVo attribute : attributeList) {
//            attributeMap.put(attribute.getUuid(), attribute);
//        }
//        JSONArray theadList = new JSONArray();
//        for (String column : columnList) {
//            MatrixAttributeVo attribute = attributeMap.get(column);
//            if (attribute == null) {
//                throw new MatrixAttributeNotFoundException(matrixUuid, column);
//            }
//            JSONObject theadObj = new JSONObject();
//            theadObj.put("key", attribute.getUuid());
//            theadObj.put("title", attribute.getName());
//            theadList.add(theadObj);
//        }
//        return theadList;
//    }
}

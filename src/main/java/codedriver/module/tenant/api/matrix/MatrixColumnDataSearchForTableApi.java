/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.matrix;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.exception.integration.IntegrationHandlerNotFoundException;
import codedriver.framework.exception.type.ParamIrregularException;
import codedriver.framework.integration.core.IIntegrationHandler;
import codedriver.framework.integration.core.IntegrationHandlerFactory;
import codedriver.framework.integration.core.RequestFrom;
import codedriver.framework.integration.dao.mapper.IntegrationMapper;
import codedriver.framework.integration.dto.IntegrationResultVo;
import codedriver.framework.integration.dto.IntegrationVo;
import codedriver.framework.matrix.constvalue.MatrixType;
import codedriver.framework.matrix.dao.mapper.*;
import codedriver.framework.matrix.dto.*;
import codedriver.framework.matrix.exception.*;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.tenant.service.matrix.MatrixService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class MatrixColumnDataSearchForTableApi extends PrivateApiComponentBase {

    private final static Logger logger = LoggerFactory.getLogger(MatrixColumnDataSearchForTableApi.class);

    @Resource
    private MatrixService matrixService;

    @Resource
    private MatrixMapper matrixMapper;

    @Resource
    private MatrixDataMapper matrixDataMapper;

    @Resource
    private MatrixAttributeMapper matrixAttributeMapper;

    @Resource
    private MatrixExternalMapper matrixExternalMapper;

    @Resource
    private MatrixViewMapper viewMapper;

    @Resource
    private IntegrationMapper integrationMapper;

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
            @Param(name = "columnList", desc = "目标属性集合，数据按这个字段顺序返回", type = ApiParamType.JSONARRAY, isRequired = true),
            @Param(name = "searchColumnList ", desc = "搜索属性集合", type = ApiParamType.JSONARRAY),
            @Param(name = "sourceColumnList", desc = "搜索过滤值集合", type = ApiParamType.JSONARRAY),
            @Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否需要分页，默认true"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页条目"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页"),
            @Param(name = "arrayColumnList", desc = "需要将值转化成数组的属性集合", type = ApiParamType.JSONARRAY),
            @Param(name = "filterList", desc = "根据列头uuid,搜索具体的列值，支持多个列分别搜索，注意仅支持静态列表  [{uuid:***,valueList:[]},{uuid:***,valueList:[]}]", type = ApiParamType.JSONARRAY)
    })
    @Description(desc = "矩阵属性数据查询-table接口")
    @Output({
            @Param(name = "tbodyList", type = ApiParamType.JSONARRAY, desc = "属性数据集合"),
            @Param(name = "theadList", type = ApiParamType.JSONARRAY, desc = "属性列名集合"),
            @Param(name = "searchColumnDetailList", type = ApiParamType.JSONARRAY, desc = "搜索属性详情集合"),
            @Param(name = "type", type = ApiParamType.STRING, desc = "矩阵类型"),
            @Param(explode = BasePageVo.class)
    })
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject returnObj = new JSONObject();
        MatrixDataVo dataVo = JSONObject.toJavaObject(jsonObj, MatrixDataVo.class);
        MatrixVo matrixVo = matrixMapper.getMatrixByUuid(dataVo.getMatrixUuid());
        if (matrixVo == null) {
            throw new MatrixNotFoundException(dataVo.getMatrixUuid());
        }
        List<String> columnList = dataVo.getColumnList();
        if (CollectionUtils.isEmpty(columnList)) {
            throw new ParamIrregularException("columnList");
        }
        JSONArray searchColumnArray = jsonObj.getJSONArray("searchColumnList");
        String type = matrixVo.getType();
        if (MatrixType.CUSTOM.getValue().equals(type)) {
            Map<String, MatrixAttributeVo> attributeMap = new HashMap<>();
            List<MatrixAttributeVo> matrixAttributeList = matrixAttributeMapper.getMatrixAttributeByMatrixUuid(dataVo.getMatrixUuid());
            for (MatrixAttributeVo attribute : matrixAttributeList) {
                attributeMap.put(attribute.getUuid(), attribute);
            }
            // theadList
            JSONArray theadList = new JSONArray();
            for (String column : dataVo.getColumnList()) {
                MatrixAttributeVo attribute = attributeMap.get(column);
                if (attribute != null) {
                    JSONObject theadObj = new JSONObject();
                    theadObj.put("key", attribute.getUuid());
                    theadObj.put("title", attribute.getName());
                    theadList.add(theadObj);
                } else {
                    throw new MatrixAttributeNotFoundException(dataVo.getMatrixUuid(), column);
                }
            }
            returnObj.put("theadList", theadList);
            List<Map<String, String>> dataMapList = matrixDataMapper.getDynamicTableDataByColumnList(dataVo);
            List<Map<String, Object>> tbodyList = matrixService.matrixTableDataValueHandle(matrixAttributeList, dataMapList);
            returnObj.put("tbodyList", tbodyList);

            if (CollectionUtils.isNotEmpty(searchColumnArray)) {
                JSONArray searchColumnDetailList = new JSONArray();
                List<String> searchColumnList = searchColumnArray.toJavaList(String.class);
                for (String column : searchColumnList) {
                    MatrixAttributeVo attribute = attributeMap.get(column);
                    if (attribute != null) {
                        searchColumnDetailList.add(attribute);
                    } else {
                        throw new MatrixAttributeNotFoundException(dataVo.getMatrixUuid(), column);
                    }
                }
                returnObj.put("searchColumnDetailList", searchColumnDetailList);
            }

            if (dataVo.getNeedPage()) {
                int rowNum = matrixDataMapper.getDynamicTableDataByColumnCount(dataVo);
                int pageCount = PageUtil.getPageCount(rowNum, dataVo.getPageSize());
                returnObj.put("currentPage", dataVo.getCurrentPage());
                returnObj.put("pageSize", dataVo.getPageSize());
                returnObj.put("pageCount", pageCount);
                returnObj.put("rowNum", rowNum);
            }
        } else if (MatrixType.EXTERNAL.getValue().equals(type)) {
            MatrixExternalVo externalVo = matrixExternalMapper.getMatrixExternalByMatrixUuid(dataVo.getMatrixUuid());
            if (externalVo == null) {
                throw new MatrixExternalNotFoundException(matrixVo.getName());
            }
            IntegrationVo integrationVo = integrationMapper.getIntegrationByUuid(externalVo.getIntegrationUuid());
            IIntegrationHandler handler = IntegrationHandlerFactory.getHandler(integrationVo.getHandler());
            if (handler == null) {
                throw new IntegrationHandlerNotFoundException(integrationVo.getHandler());
            }
            Map<String, MatrixAttributeVo> attributeMap = new HashMap<>();
            List<MatrixAttributeVo> matrixAttributeList = matrixService.getExternalMatrixAttributeList(dataVo.getMatrixUuid(), integrationVo);
            for (MatrixAttributeVo matrixAttributeVo : matrixAttributeList) {
                attributeMap.put(matrixAttributeVo.getUuid(), matrixAttributeVo);
            }

            // theadList
            JSONArray theadList = new JSONArray();
            for (String column : dataVo.getColumnList()) {
                MatrixAttributeVo attribute = attributeMap.get(column);
                if (attribute != null) {
                    JSONObject theadObj = new JSONObject();
                    theadObj.put("key", attribute.getUuid());
                    theadObj.put("title", attribute.getName());
                    theadList.add(theadObj);
                } else {
                    throw new MatrixAttributeNotFoundException(dataVo.getMatrixUuid(), column);
                }
            }

            if (CollectionUtils.isNotEmpty(searchColumnArray)) {
                JSONArray searchColumnDetailList = new JSONArray();
                List<String> searchColumnList = searchColumnArray.toJavaList(String.class);
                for (String column : searchColumnList) {
                    MatrixAttributeVo attribute = attributeMap.get(column);
                    if (attribute != null) {
                        searchColumnDetailList.add(attribute);
                    } else {
                        throw new MatrixAttributeNotFoundException(dataVo.getMatrixUuid(), column);
                    }
                }
                returnObj.put("searchColumnDetailList", searchColumnDetailList);
            }
            jsonObj.put("sourceColumnList", dataVo.getSourceColumnList()); //防止集成管理 js length 异常
            integrationVo.getParamObj().putAll(jsonObj);
            IntegrationResultVo resultVo = handler.sendRequest(integrationVo, RequestFrom.MATRIX);
            if (StringUtils.isNotBlank(resultVo.getError())) {
                logger.error(resultVo.getError());
                throw new MatrixExternalAccessException();
            } else {
                matrixService.getExternalDataTbodyList(resultVo, dataVo.getColumnList(), returnObj);
                /** 将arrayColumnList包含的属性值转成数组 **/
                JSONArray arrayColumnArray = jsonObj.getJSONArray("arrayColumnList");
                if (CollectionUtils.isNotEmpty(arrayColumnArray)) {
                    List<String> arrayColumnList = arrayColumnArray.toJavaList(String.class);
                    JSONArray tbodyList = returnObj.getJSONArray("tbodyList");
                    if (CollectionUtils.isNotEmpty(tbodyList)) {
                        matrixService.arrayColumnDataConversion(arrayColumnList, tbodyList);
                    }
                }
            }
            returnObj.put("theadList", theadList);
        } else if (MatrixType.VIEW.getValue().equals(matrixVo.getType())) {
            MatrixViewVo matrixViewVo = viewMapper.getMatrixViewByMatrixUuid(dataVo.getMatrixUuid());
            if (matrixViewVo == null) {
                throw new MatrixViewNotFoundException(matrixVo.getName());
            }
            JSONArray attributeList = (JSONArray) JSONPath.read(matrixViewVo.getConfig(), "attributeList");
            if (CollectionUtils.isNotEmpty(attributeList)) {
                Map<String, MatrixAttributeVo> attributeMap = new HashMap<>();
                List<MatrixAttributeVo> matrixAttributeList = attributeList.toJavaList(MatrixAttributeVo.class);
                for (MatrixAttributeVo attribute : matrixAttributeList) {
                    attributeMap.put(attribute.getUuid(), attribute);
                }
                // theadList
                JSONArray theadList = new JSONArray();
                for (String column : dataVo.getColumnList()) {
                    MatrixAttributeVo attribute = attributeMap.get(column);
                    if (attribute != null) {
                        JSONObject theadObj = new JSONObject();
                        theadObj.put("key", attribute.getUuid());
                        theadObj.put("title", attribute.getName());
                        theadList.add(theadObj);
                    } else {
                        throw new MatrixAttributeNotFoundException(dataVo.getMatrixUuid(), column);
                    }
                }
                returnObj.put("theadList", theadList);
                List<Map<String, String>> dataMapList = matrixDataMapper.getDynamicTableDataByColumnList(dataVo);
                List<Map<String, Object>> tbodyList = matrixService.matrixTableDataValueHandle(matrixAttributeList, dataMapList);
                returnObj.put("tbodyList", tbodyList);

                if (CollectionUtils.isNotEmpty(searchColumnArray)) {
                    JSONArray searchColumnDetailList = new JSONArray();
                    List<String> searchColumnList = searchColumnArray.toJavaList(String.class);
                    for (String column : searchColumnList) {
                        MatrixAttributeVo attribute = attributeMap.get(column);
                        if (attribute != null) {
                            searchColumnDetailList.add(attribute);
                        } else {
                            throw new MatrixAttributeNotFoundException(dataVo.getMatrixUuid(), column);
                        }
                    }
                    returnObj.put("searchColumnDetailList", searchColumnDetailList);
                }

                if (dataVo.getNeedPage()) {
                    int rowNum = matrixDataMapper.getDynamicTableDataByColumnCount(dataVo);
                    int pageCount = PageUtil.getPageCount(rowNum, dataVo.getPageSize());
                    returnObj.put("currentPage", dataVo.getCurrentPage());
                    returnObj.put("pageSize", dataVo.getPageSize());
                    returnObj.put("pageCount", pageCount);
                    returnObj.put("rowNum", rowNum);
                }
            }
        }
        returnObj.put("type", type);
        return returnObj;
    }
}

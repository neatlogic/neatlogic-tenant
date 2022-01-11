/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.matrix;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.dependency.constvalue.FromType;
import codedriver.framework.dependency.core.DependencyManager;
import codedriver.framework.matrix.core.IMatrixDataSourceHandler;
import codedriver.framework.matrix.core.MatrixDataSourceHandlerFactory;
import codedriver.framework.matrix.dao.mapper.*;
import codedriver.framework.matrix.dto.*;
import codedriver.framework.matrix.exception.*;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @program: codedriver
 * @description:
 * @create: 2020-03-30 16:34
 **/
@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class MatrixDataSearchApi extends PrivateApiComponentBase {

    @Resource
    private MatrixMapper matrixMapper;

    @Override
    public String getToken() {
        return "matrix/data/search";
    }

    @Override
    public String getName() {
        return "矩阵数据检索接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
//            @Param(name = "keyword", desc = "关键字", type = ApiParamType.STRING), 页面上没有搜索框，所以后端不需要支持关键字搜索
            @Param(name = "matrixUuid", desc = "矩阵uuid", type = ApiParamType.STRING, isRequired = true),
            @Param(name = "needPage", desc = "是否分页", type = ApiParamType.BOOLEAN),
            @Param(name = "pageSize", desc = "显示条目数", type = ApiParamType.INTEGER),
            @Param(name = "currentPage", desc = "当前页", type = ApiParamType.INTEGER)
    })
    @Output({
            @Param(name = "tbodyList", type = ApiParamType.JSONARRAY, desc = "矩阵数据集合"),
            @Param(name = "theadList", type = ApiParamType.JSONARRAY, desc = "矩阵属性集合"),
            @Param(name = "referenceCount", type = ApiParamType.INTEGER, desc = "被引用次数"),
            @Param(explode = BasePageVo.class)
    })
    @Description(desc = "矩阵数据检索接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        MatrixDataVo dataVo = JSONObject.toJavaObject(jsonObj, MatrixDataVo.class);
        MatrixVo matrixVo = matrixMapper.getMatrixByUuid(dataVo.getMatrixUuid());
        if (matrixVo == null) {
            throw new MatrixNotFoundException(dataVo.getMatrixUuid());
        }
        IMatrixDataSourceHandler matrixDataSourceHandler = MatrixDataSourceHandlerFactory.getHandler(matrixVo.getType());
        if (matrixDataSourceHandler == null) {
            throw new MatrixDataSourceHandlerNotFoundException(matrixVo.getType());
        }
        JSONObject returnObj = matrixDataSourceHandler.getTableData(dataVo);
//        String type = matrixVo.getType();
//        if (MatrixType.CUSTOM.getValue().equals(type)) {
//            List<MatrixAttributeVo> attributeVoList = attributeMapper.getMatrixAttributeByMatrixUuid(dataVo.getMatrixUuid());
//            if (CollectionUtils.isNotEmpty(attributeVoList)) {
//                List<String> columnList = attributeVoList.stream().map(MatrixAttributeVo::getUuid).collect(Collectors.toList());
//                dataVo.setColumnList(columnList);
//                if (dataVo.getNeedPage()) {
//                    int rowNum = matrixDataMapper.getDynamicTableDataCount(dataVo);
//                    dataVo.setRowNum(rowNum);
//                }
//                List<Map<String, String>> dataList = matrixDataMapper.searchDynamicTableData(dataVo);
//                List<Map<String, Object>> tbodyList = matrixService.matrixTableDataValueHandle(attributeVoList, dataList);
//                JSONArray theadList = getTheadList(attributeVoList);
//                returnObj = TableResultUtil.getResult(theadList, tbodyList, dataVo);
//            }
//        } else if (MatrixType.VIEW.getValue().equals(type)) {
//            MatrixViewVo matrixViewVo = viewMapper.getMatrixViewByMatrixUuid(dataVo.getMatrixUuid());
//            if (matrixViewVo == null) {
//                throw new MatrixViewNotFoundException(matrixVo.getName());
//            }
//            JSONArray attributeList = (JSONArray) JSONPath.read(matrixViewVo.getConfig(), "attributeList");
//            if (CollectionUtils.isNotEmpty(attributeList)) {
//                List<MatrixAttributeVo> attributeVoList = attributeList.toJavaList(MatrixAttributeVo.class);
//                List<String> columnList = attributeVoList.stream().map(MatrixAttributeVo::getUuid).collect(Collectors.toList());
//                dataVo.setColumnList(columnList);
//                if (dataVo.getNeedPage()) {
//                    int rowNum = matrixDataMapper.getDynamicTableDataCount(dataVo);
//                    dataVo.setRowNum(rowNum);
//                }
//                List<Map<String, String>> dataList = matrixDataMapper.searchDynamicTableData(dataVo);
//                List<Map<String, Object>>  tbodyList = matrixService.matrixTableDataValueHandle(attributeVoList, dataList);
//                JSONArray theadList = getTheadList(attributeVoList);
//                returnObj = TableResultUtil.getResult(theadList, tbodyList, dataVo);
//            }
//        } else if (MatrixType.EXTERNAL.getValue().equals(type)) {
//            MatrixExternalVo externalVo = externalMapper.getMatrixExternalByMatrixUuid(dataVo.getMatrixUuid());
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
//            integrationVo.getParamObj().putAll(jsonObj);
//            IntegrationResultVo resultVo = handler.sendRequest(integrationVo, RequestFrom.MATRIX);
//            if (StringUtils.isNotBlank(resultVo.getError())) {
//                logger.error(resultVo.getError());
//                throw new MatrixExternalAccessException();
//            }
//            handler.validate(resultVo);
//            JSONObject transformedResult = JSONObject.parseObject(resultVo.getTransformedResult());
//            returnObj.put("currentPage", transformedResult.get("currentPage"));
//            returnObj.put("pageSize", transformedResult.get("pageSize"));
//            returnObj.put("pageCount", transformedResult.get("pageCount"));
//            returnObj.put("rowNum", transformedResult.get("rowNum"));
//            returnObj.put("theadList", transformedResult.get("theadList"));
////            returnObj.putAll(transformedResult);
//            List<Map<String, Object>> tbodyList = new ArrayList<>();
//            JSONArray tbodyArray = transformedResult.getJSONArray("tbodyList");
//            if (CollectionUtils.isNotEmpty(tbodyArray)) {
//                for (int i = 0; i < tbodyArray.size(); i++) {
//                    JSONObject rowData = tbodyArray.getJSONObject(i);
//                    if (MapUtils.isNotEmpty(rowData)) {
//                        Map<String, Object> rowDataMap = new HashMap<>();
//                        for (Entry<String, Object> entry : rowData.entrySet()) {
//                            rowDataMap.put(entry.getKey(), matrixService.matrixAttributeValueHandle(entry.getValue()));
//                        }
//                        tbodyList.add(rowDataMap);
//                    }
//                }
//            }
//            returnObj.put("tbodyList", tbodyList);
//        }
        int count = DependencyManager.getDependencyCount(FromType.MATRIX, dataVo.getMatrixUuid());
        returnObj.put("referenceCount", count);
        return returnObj;
    }

//    private JSONArray getTheadList(List<MatrixAttributeVo> attributeList) {
//        JSONArray theadList = new JSONArray();
//        JSONObject selectionObj = new JSONObject();
//        selectionObj.put("key", "selection");
//        selectionObj.put("width", 60);
//        theadList.add(selectionObj);
//        for (MatrixAttributeVo attributeVo : attributeList) {
//            JSONObject columnObj = new JSONObject();
//            columnObj.put("title", attributeVo.getName());
//            columnObj.put("key", attributeVo.getUuid());
//            theadList.add(columnObj);
//        }
//        JSONObject actionObj = new JSONObject();
//        actionObj.put("title", "");
//        actionObj.put("key", "action");
//        actionObj.put("align", "right");
//        actionObj.put("width", 10);
//        theadList.add(actionObj);
//        return theadList;
//    }
}

/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.matrix;

import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.dependency.constvalue.CalleeType;
import codedriver.framework.dependency.core.DependencyManager;
import codedriver.framework.exception.integration.IntegrationHandlerNotFoundException;
import codedriver.framework.integration.core.IIntegrationHandler;
import codedriver.framework.integration.core.IntegrationHandlerFactory;
import codedriver.framework.integration.core.RequestFrom;
import codedriver.framework.integration.dao.mapper.IntegrationMapper;
import codedriver.framework.integration.dto.IntegrationResultVo;
import codedriver.framework.integration.dto.IntegrationVo;
import codedriver.framework.matrix.constvalue.MatrixType;
import codedriver.framework.matrix.dao.mapper.*;
import codedriver.framework.matrix.dto.*;
import codedriver.framework.matrix.exception.MatrixExternalAccessException;
import codedriver.framework.matrix.exception.MatrixNotFoundException;
import codedriver.framework.matrix.exception.MatrixViewNotFoundException;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.tenant.service.matrix.MatrixService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @program: codedriver
 * @description:
 * @create: 2020-03-30 16:34
 **/
@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class MatrixDataSearchApi extends PrivateApiComponentBase {

    private final static Logger logger = LoggerFactory.getLogger(MatrixDataSearchApi.class);

    @Resource
    private MatrixService matrixService;

    @Resource
    private MatrixMapper matrixMapper;

    @Resource
    private MatrixAttributeMapper attributeMapper;

    @Resource
    private MatrixDataMapper matrixDataMapper;

    @Resource
    private IntegrationMapper integrationMapper;

    @Resource
    private MatrixExternalMapper externalMapper;

    @Resource
    private MatrixViewMapper viewMapper;

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
            @Param(name = "keyword", desc = "关键字", type = ApiParamType.STRING),
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
        JSONObject returnObj = new JSONObject();
        MatrixDataVo dataVo = JSON.toJavaObject(jsonObj, MatrixDataVo.class);
        MatrixVo matrixVo = matrixMapper.getMatrixByUuid(dataVo.getMatrixUuid());
        if (matrixVo == null) {
            throw new MatrixNotFoundException(dataVo.getMatrixUuid());
        }
        List<Map<String, Object>> tbodyList = new ArrayList<>();
        String type = matrixVo.getType();
        if (MatrixType.CUSTOM.getValue().equals(type)) {
            List<MatrixAttributeVo> attributeVoList = attributeMapper.getMatrixAttributeByMatrixUuid(dataVo.getMatrixUuid());
            if (CollectionUtils.isNotEmpty(attributeVoList)) {
                List<String> columnList = new ArrayList<>();
                JSONArray headList = new JSONArray();
                JSONObject selectionObj = new JSONObject();
                selectionObj.put("key", "selection");
                selectionObj.put("width", 60);
                headList.add(selectionObj);
                for (MatrixAttributeVo attributeVo : attributeVoList) {
                    columnList.add(attributeVo.getUuid());
                    JSONObject columnObj = new JSONObject();
                    columnObj.put("title", attributeVo.getName());
                    columnObj.put("key", attributeVo.getUuid());
                    headList.add(columnObj);
                }
                JSONObject actionObj = new JSONObject();
                actionObj.put("title", "");
                actionObj.put("key", "action");
                actionObj.put("align", "right");
                actionObj.put("width", 10);
                headList.add(actionObj);

                returnObj.put("theadList", headList);

                dataVo.setColumnList(columnList);
                if (dataVo.getNeedPage()) {
                    int rowNum = matrixDataMapper.getDynamicTableDataCount(dataVo, TenantContext.get().getTenantUuid());
                    returnObj.put("pageCount", PageUtil.getPageCount(rowNum, dataVo.getPageSize()));
                    returnObj.put("rowNum", rowNum);
                    returnObj.put("pageSize", dataVo.getPageSize());
                    returnObj.put("currentPage", dataVo.getCurrentPage());
                }

                List<Map<String, String>> dataList = matrixDataMapper.searchDynamicTableData(dataVo, TenantContext.get().getTenantUuid());
                tbodyList = matrixService.matrixTableDataValueHandle(attributeVoList, dataList);
            }
        } else if (MatrixType.EXTERNAL.getValue().equals(type)) {
            MatrixExternalVo externalVo = externalMapper.getMatrixExternalByMatrixUuid(dataVo.getMatrixUuid());
            if (externalVo != null) {
                IntegrationVo integrationVo = integrationMapper.getIntegrationByUuid(externalVo.getIntegrationUuid());
                IIntegrationHandler handler = IntegrationHandlerFactory.getHandler(integrationVo.getHandler());
                if (handler == null) {
                    throw new IntegrationHandlerNotFoundException(integrationVo.getHandler());
                }

                integrationVo.getParamObj().putAll(jsonObj);
                IntegrationResultVo resultVo = handler.sendRequest(integrationVo, RequestFrom.MATRIX);
                if (StringUtils.isNotBlank(resultVo.getError())) {
                    logger.error(resultVo.getError());
                    throw new MatrixExternalAccessException();
                }
                handler.validate(resultVo);
                JSONObject transformedResult = JSONObject.parseObject(resultVo.getTransformedResult());
                returnObj.putAll(transformedResult);
                JSONArray tbodyArray = transformedResult.getJSONArray("tbodyList");
                if (CollectionUtils.isNotEmpty(tbodyArray)) {
                    for (int i = 0; i < tbodyArray.size(); i++) {
                        JSONObject rowData = tbodyArray.getJSONObject(i);
                        Integer pageSize = jsonObj.getInteger("pageSize");
                        pageSize = pageSize == null ? 10 : pageSize;
                        if (MapUtils.isNotEmpty(rowData)) {
                            Map<String, Object> rowDataMap = new HashMap<>();
                            for (Entry<String, Object> entry : rowData.entrySet()) {
                                rowDataMap.put(entry.getKey(), matrixService.matrixAttributeValueHandle(entry.getValue()));
                            }
                            tbodyList.add(rowDataMap);
                            if (tbodyList.size() >= pageSize) {
                                break;
                            }
                        }
                    }
                }
            }
        } else if (MatrixType.VIEW.getValue().equals(type)) {
            MatrixViewVo matrixViewVo = viewMapper.getMatrixViewByMatrixUuid(dataVo.getMatrixUuid());
            if (matrixViewVo == null) {
                throw new MatrixViewNotFoundException(matrixVo.getName());
            }
            JSONArray attributeList = (JSONArray) JSONPath.read(matrixViewVo.getConfig(), "attributeList");
            if (CollectionUtils.isNotEmpty(attributeList)) {
                List<String> columnList = new ArrayList<>();
                JSONArray headList = new JSONArray();
                JSONObject selectionObj = new JSONObject();
                selectionObj.put("key", "selection");
                selectionObj.put("width", 60);
                headList.add(selectionObj);
                List<MatrixAttributeVo> attributeVoList = attributeList.toJavaList(MatrixAttributeVo.class);
                for (MatrixAttributeVo attributeVo : attributeVoList) {
                    columnList.add(attributeVo.getUuid());
                    JSONObject columnObj = new JSONObject();
                    columnObj.put("title", attributeVo.getName());
                    columnObj.put("key", attributeVo.getUuid());
                    headList.add(columnObj);
                }
                JSONObject actionObj = new JSONObject();
                actionObj.put("title", "");
                actionObj.put("key", "action");
                actionObj.put("align", "right");
                actionObj.put("width", 10);
                headList.add(actionObj);

                returnObj.put("theadList", headList);

                dataVo.setColumnList(columnList);
                if (dataVo.getNeedPage()) {
                    int rowNum = matrixDataMapper.getDynamicTableDataCount(dataVo, TenantContext.get().getTenantUuid());
                    returnObj.put("pageCount", PageUtil.getPageCount(rowNum, dataVo.getPageSize()));
                    returnObj.put("rowNum", rowNum);
                    returnObj.put("pageSize", dataVo.getPageSize());
                    returnObj.put("currentPage", dataVo.getCurrentPage());
                }

                List<Map<String, String>> dataList = matrixDataMapper.searchDynamicTableData(dataVo, TenantContext.get().getTenantUuid());
                tbodyList = matrixService.matrixTableDataValueHandle(attributeVoList, dataList);
            }
        }

        returnObj.put("tbodyList", tbodyList);
        int count = DependencyManager.getDependencyCount(CalleeType.MATRIX, dataVo.getMatrixUuid());
        returnObj.put("referenceCount", count);
        return returnObj;
    }
}

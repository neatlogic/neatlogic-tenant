/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.integration.table;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.crossover.CrossoverServiceFactory;
import codedriver.framework.exception.integration.*;
import codedriver.framework.exception.type.ParamIrregularException;
import codedriver.framework.integration.core.IIntegrationHandler;
import codedriver.framework.integration.core.IntegrationHandlerFactory;
import codedriver.framework.integration.crossover.IntegrationCrossoverService;
import codedriver.framework.integration.dao.mapper.IntegrationMapper;
import codedriver.framework.integration.dto.IntegrationResultVo;
import codedriver.framework.integration.dto.IntegrationVo;
import codedriver.framework.integration.dto.table.ColumnVo;
import codedriver.framework.integration.dto.table.SourceColumnVo;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.framework.integration.handler.FrameworkRequestFrom;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class TableDataSearchApi extends PrivateApiComponentBase {

    private final static Logger logger = LoggerFactory.getLogger(TableDataSearchApi.class);

    @Resource
    private IntegrationMapper integrationMapper;

    @Override
    public String getToken() {
        return "integration/table/data/search";
    }

    @Override
    public String getName() {
        return "集成属性数据查询-table接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "integrationUuid", desc = "集成Uuid", type = ApiParamType.STRING, isRequired = true),
            @Param(name = "columnList", desc = "目标属性集合，数据按这个字段顺序返回", type = ApiParamType.JSONARRAY, isRequired = true),
            @Param(name = "defaultValue", desc = "需要回显的数据uuid集合", type = ApiParamType.JSONARRAY),
            @Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否需要分页，默认true"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页条目"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页"),
            @Param(name = "searchColumnList ", desc = "搜索属性集合", type = ApiParamType.JSONARRAY),
            @Param(name = "sourceColumnList ", desc = "搜索过滤值集合", type = ApiParamType.JSONARRAY),
    })
    @Description(desc = "集成属性数据查询-table接口")
    @Output({
            @Param(name = "tbodyList", type = ApiParamType.JSONARRAY, desc = "属性数据集合"),
            @Param(name = "theadList", type = ApiParamType.JSONARRAY, desc = "属性列名集合"),
            @Param(name = "searchColumnDetailList", type = ApiParamType.JSONARRAY, desc = "搜索属性详情集合"),
            @Param(explode = BasePageVo.class)
    })
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject returnObj = new JSONObject();
        JSONArray columnArray = jsonObj.getJSONArray("columnList");
        if (CollectionUtils.isEmpty(columnArray)) {
            throw new ParamIrregularException("columnList");
        }
        String integrationUuid = jsonObj.getString("integrationUuid");
        IntegrationVo integrationVo = integrationMapper.getIntegrationByUuid(integrationUuid);
        if (integrationVo == null) {
            throw new IntegrationNotFoundException(integrationUuid);
        }
        IIntegrationHandler handler = IntegrationHandlerFactory.getHandler(integrationVo.getHandler());
        if (handler == null) {
            throw new IntegrationHandlerNotFoundException(integrationVo.getHandler());
        }
        IntegrationCrossoverService integrationCrossoverService = CrossoverServiceFactory.getApi(IntegrationCrossoverService.class);
        List<ColumnVo> columnVoList = integrationCrossoverService.getColumnList(integrationVo);
        if (CollectionUtils.isNotEmpty(columnVoList)) {

            String uuidColumn = null;
            for (ColumnVo columnVo : columnVoList) {
                Integer primaryKey = columnVo.getPrimaryKey();
                if (Objects.equals(primaryKey, 1)) {
                    uuidColumn = columnVo.getUuid();
                    break;
                }
            }
            if (uuidColumn == null) {
                throw new IntegrationTablePrimaryKeyColumnNotFoundException(integrationVo.getName());
            }
            List<String> columnList = columnArray.toJavaList(String.class);
            JSONArray theadList = integrationCrossoverService.getTheadList(integrationVo, columnList);
            returnObj.put("theadList", theadList);
            if (!columnList.contains(uuidColumn)) {
                columnList.add(uuidColumn);
            }
            JSONArray defaultValue = jsonObj.getJSONArray("defaultValue");
            if (CollectionUtils.isNotEmpty(defaultValue)) {
                List<SourceColumnVo> sourceColumnList = new ArrayList<>();
                SourceColumnVo sourceColumnVo = new SourceColumnVo();
                sourceColumnVo.setColumn(uuidColumn);
                List<Map<String, Object>> tbodyArray = new ArrayList<>();
                for (Object uuidValue : defaultValue) {
                    sourceColumnVo.setValue(uuidValue);
                    sourceColumnList.clear();
                    sourceColumnList.add(sourceColumnVo);
                    integrationVo.getParamObj().put("sourceColumnList", sourceColumnList);
                    IntegrationResultVo resultVo = handler.sendRequest(integrationVo, FrameworkRequestFrom.FORM);
                    if (StringUtils.isNotBlank(resultVo.getError())) {
                        logger.error(resultVo.getError());
                        throw new IntegrationSendRequestException(integrationVo.getName());
                    }
                    handler.validate(resultVo);
                    List<Map<String, Object>> tbodyList = integrationCrossoverService.getTbodyList(resultVo, columnList);
                    for (Map<String, Object> tbodyObj : tbodyList) {
                        if (Objects.equals(uuidValue, tbodyObj.get(uuidColumn))) {
                            tbodyArray.add(tbodyObj);
                            break;
                        }
                    }
                }
                returnObj.put("tbodyList", tbodyArray);
            } else {
                List<SourceColumnVo> sourceColumnList = new ArrayList<>();
                JSONArray sourceColumnArray = jsonObj.getJSONArray("sourceColumnList");
                if (CollectionUtils.isNotEmpty(sourceColumnArray)) {
                    sourceColumnList = sourceColumnArray.toJavaList(SourceColumnVo.class);
                    Iterator<SourceColumnVo> iterator = sourceColumnList.iterator();
                    while (iterator.hasNext()) {
                        SourceColumnVo sourceColumnVo = iterator.next();
                        if (StringUtils.isBlank(sourceColumnVo.getColumn())) {
                            iterator.remove();
                        } else if (CollectionUtils.isEmpty(sourceColumnVo.getValueList())) {
                            iterator.remove();
                        }
                    }
                }
                JSONArray filterList = jsonObj.getJSONArray("filterList");
                if (CollectionUtils.isNotEmpty(filterList)) {
                    if (!integrationCrossoverService.mergeFilterListAndSourceColumnList(filterList, sourceColumnList)) {
                        return returnObj;
                    }
                }
                JSONObject paramObj = new JSONObject();
                paramObj.put("currentPage", jsonObj.getInteger("currentPage"));
                paramObj.put("pageSize", jsonObj.getInteger("pageSize"));
                paramObj.put("needPage", jsonObj.getBoolean("needPage"));
                paramObj.put("sourceColumnList", sourceColumnList);
                integrationVo.setParamObj(paramObj);
//                integrationVo.getParamObj().putAll(jsonObj);
                IntegrationResultVo resultVo = handler.sendRequest(integrationVo, FrameworkRequestFrom.FORM);
                if (StringUtils.isNotBlank(resultVo.getError())) {
                    logger.error(resultVo.getError());
                    throw new IntegrationSendRequestException(integrationVo.getName());
                }
                handler.validate(resultVo);
                JSONObject transformedResult = JSONObject.parseObject(resultVo.getTransformedResult());
                returnObj.put("currentPage", transformedResult.get("currentPage"));
                returnObj.put("pageSize", transformedResult.get("pageSize"));
                returnObj.put("pageCount", transformedResult.get("pageCount"));
                returnObj.put("rowNum", transformedResult.get("rowNum"));
                List<Map<String, Object>> tbodyList = integrationCrossoverService.getTbodyList(resultVo, columnList);
                returnObj.put("tbodyList", tbodyList);
                JSONArray searchColumnArray = jsonObj.getJSONArray("searchColumnList");
                if (CollectionUtils.isNotEmpty(searchColumnArray)) {
                    returnObj.put("searchColumnDetailList", getSearchColumnDetailList(integrationVo.getName(), columnVoList, searchColumnArray));
                }
            }
        }
        return returnObj;
    }

    private List<ColumnVo> getSearchColumnDetailList(String integrationUuid, List<ColumnVo> columnVoList, JSONArray searchColumnArray) {
        Map<String, ColumnVo> columnVoMap = new HashMap<>();
        for (ColumnVo columnVo : columnVoList) {
            columnVoMap.put(columnVo.getUuid(), columnVo);
        }
        List<ColumnVo> searchColumnDetailList = new ArrayList<>();
        List<String> searchColumnList = searchColumnArray.toJavaList(String.class);
        for (String column : searchColumnList) {
            ColumnVo columnVo = columnVoMap.get(column);
            if (columnVo == null) {
                throw new IntegrationTableColumnNotFoundException(integrationUuid, column);
            }
            searchColumnDetailList.add(columnVo);
        }
        return searchColumnDetailList;
    }

}

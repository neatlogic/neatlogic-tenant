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

package neatlogic.module.tenant.api.integration.table;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.integration.dao.mapper.IntegrationMapper;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;

//@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class TableColumnDataSearchApi extends PrivateApiComponentBase {

    private final static Logger logger = LoggerFactory.getLogger(TableColumnDataSearchApi.class);

    @Resource
    private IntegrationMapper integrationMapper;

    @Override
    public String getToken() {
        return "integration/column/data/search";
    }

    @Override
    public String getName() {
        return "集成属性数据查询-下拉级联接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "keyword", desc = "关键字", type = ApiParamType.STRING, xss = true),
            @Param(name = "keywordColumn", desc = "关键字属性uuid", type = ApiParamType.STRING),
            @Param(name = "integrationUuid", desc = "集成Uuid", type = ApiParamType.STRING, isRequired = true),
            @Param(name = "columnList", desc = "属性uuid列表", type = ApiParamType.JSONARRAY, isRequired = true),
            @Param(name = "sourceColumnList", desc = "搜索过滤值集合", type = ApiParamType.JSONARRAY),
            @Param(name = "pageSize", desc = "显示条目数", type = ApiParamType.INTEGER),
            @Param(name = "defaultValue", desc = "精确匹配回显数据参数", type = ApiParamType.JSONARRAY),
            @Param(name = "filterList", desc = "联动过滤数据集合", type = ApiParamType.JSONARRAY)
    })
    @Output({
            @Param(name = "tbodyList", type = ApiParamType.JSONARRAY, desc = "属性数据集合")
    })
    @Description(desc = "矩阵属性数据查询-下拉级联接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject returnObj = new JSONObject();
//        List<Map<String, Object>> resultList = new ArrayList<>();
//
//        JSONArray columnArray = jsonObj.getJSONArray("columnList");
//        if (CollectionUtils.isEmpty(columnArray)) {
//            throw new ParamIrregularException("columnList");
//        }
//        String integrationUuid = jsonObj.getString("integrationUuid");
//        IntegrationVo integrationVo = integrationMapper.getIntegrationByUuid(integrationUuid);
//        if (integrationVo == null) {
//            throw new IntegrationNotFoundException(integrationUuid);
//        }
//        IIntegrationHandler handler = IntegrationHandlerFactory.getHandler(integrationVo.getHandler());
//        if (handler == null) {
//            throw new IntegrationHandlerNotFoundException(integrationVo.getHandler());
//        }
//        IntegrationCrossoverService integrationCrossoverService = CrossoverServiceFactory.getApi(IntegrationCrossoverService.class);
//        List<ColumnVo> columnVoList = integrationCrossoverService.getColumnList(integrationVo);
//        if (CollectionUtils.isEmpty(columnVoList)) {
//            return returnObj;
//        }
//        /** 属性集合去重 **/
//        List<String> distinctColumList = new ArrayList<>();
//        List<String> columnList = columnArray.toJavaList(String.class);
//        for (String column : columnList) {
//            if (!distinctColumList.contains(column)) {
//                distinctColumList.add(column);
//            }
//        }
//        columnList = distinctColumList;
////        dataVo.setColumnList(distinctColumList);
//
//        List<String> columnUuidList = columnVoList.stream().map(ColumnVo::getUuid).collect(Collectors.toList());
//        for (String column : columnList) {
//            if (!columnUuidList.contains(column)) {
//                throw new IntegrationTableColumnNotFoundException(integrationVo.getName(), column);
//            }
//        }
//        JSONArray defaultValue = jsonObj.getJSONArray("defaultValue");
//        if (CollectionUtils.isNotEmpty(defaultValue)) {
//            for (String value : defaultValue.toJavaList(String.class)) {
//                if (value.contains(IFormAttributeHandler.SELECT_COMPOSE_JOINER)) {
//                    String[] split = value.split(IFormAttributeHandler.SELECT_COMPOSE_JOINER);
//                    //当下拉框配置的值和显示文字列为同一列时，value值是这样的20210101&=&20210101，split数组第一和第二个元素相同，这时需要去重
//                    List<String> splitList = new ArrayList<>();
//                    for (String str : split) {
//                        if (!splitList.contains(str)) {
//                            splitList.add(str);
//                        }
//                    }
//                    List<SourceColumnVo> sourceColumnList = new ArrayList<>();
//                    int min = Math.min(splitList.size(), columnList.size());
//                    for (int i = 0; i < min; i++) {
//                        String column = columnList.get(i);
//                        if (StringUtils.isNotBlank(column)) {
//                            SourceColumnVo sourceColumnVo = new SourceColumnVo();
//                            sourceColumnVo.setColumn(column);
//                            List<String> valueList = new ArrayList<>();
//                            valueList.add(splitList.get(i));
//                            sourceColumnVo.setValueList(valueList);
//                            sourceColumnList.add(sourceColumnVo);
//                        }
//                    }
//                    integrationVo.getParamObj().put("sourceColumnList", sourceColumnList);
////                    integrationVo.getParamObj().putAll(jsonObj);
//                    IntegrationResultVo resultVo = handler.sendRequest(integrationVo, FrameworkRequestFrom.FORM);
//                    if (StringUtils.isNotBlank(resultVo.getError())) {
//                        logger.error(resultVo.getError());
//                        throw new IntegrationSendRequestException(integrationVo.getName());
//                    }
//                    resultList.addAll(integrationCrossoverService.getTbodyList(resultVo, columnList));
//                } else {
//                    List<SourceColumnVo> sourceColumnList = new ArrayList<>();
//                    String column = columnList.get(0);
//                    if (StringUtils.isNotBlank(column)) {
//                        SourceColumnVo sourceColumnVo = new SourceColumnVo();
//                        sourceColumnVo.setColumn(column);
//                        List<String> valueList = new ArrayList<>();
//                        valueList.add(value);
//                        sourceColumnVo.setValueList(valueList);
//                        sourceColumnList.add(sourceColumnVo);
//                    }
//                    integrationVo.getParamObj().put("sourceColumnList", sourceColumnList);
////                    integrationVo.getParamObj().putAll(jsonObj);
//                    IntegrationResultVo resultVo = handler.sendRequest(integrationVo, FrameworkRequestFrom.FORM);
//                    if (StringUtils.isNotBlank(resultVo.getError())) {
//                        logger.error(resultVo.getError());
//                        throw new IntegrationSendRequestException(integrationVo.getName());
//                    }
//                    resultList.addAll(integrationCrossoverService.getTbodyList(resultVo, columnList));
//                }
//            }
//        } else {
//            List<SourceColumnVo> sourceColumnList = new ArrayList<>();
//            JSONArray sourceColumnArray = jsonObj.getJSONArray("sourceColumnList");
//            if (CollectionUtils.isNotEmpty(sourceColumnArray)) {
//                sourceColumnList = sourceColumnArray.toJavaList(SourceColumnVo.class);
//                Iterator<SourceColumnVo> iterator = sourceColumnList.iterator();
//                while (iterator.hasNext()) {
//                    SourceColumnVo sourceColumnVo = iterator.next();
//                    if (StringUtils.isBlank(sourceColumnVo.getColumn())) {
//                        iterator.remove();
//                    } else if (CollectionUtils.isEmpty(sourceColumnVo.getValueList())) {
//                        iterator.remove();
//                    }
//                }
//            }
//            String keywordColumn = jsonObj.getString("keywordColumn");
//            String keyword = jsonObj.getString("keyword");
//            if (StringUtils.isNotBlank(keywordColumn) && StringUtils.isNotBlank(keyword)) {
//                if (!columnUuidList.contains(keywordColumn)) {
//                    throw new IntegrationTableColumnNotFoundException(integrationVo.getName(), keywordColumn);
//                }
//                SourceColumnVo sourceColumnVo = new SourceColumnVo();
//                sourceColumnVo.setColumn(keywordColumn);
//                List<String> valueList = new ArrayList<>();
//                valueList.add(keyword);
//                sourceColumnVo.setValueList(valueList);
//                sourceColumnList.add(sourceColumnVo);
//            }
//            JSONArray filterList = jsonObj.getJSONArray("filterList");
//            if (CollectionUtils.isNotEmpty(filterList)) {
//                if (!integrationCrossoverService.mergeFilterListAndSourceColumnList(filterList, sourceColumnList)) {
//                    return returnObj;
//                }
//            }
//            JSONObject paramObj = new JSONObject();
//            paramObj.put("currentPage", jsonObj.getInteger("currentPage"));
//            paramObj.put("pageSize", jsonObj.getInteger("pageSize"));
//            paramObj.put("needPage", jsonObj.getBoolean("needPage"));
//            paramObj.put("sourceColumnList", sourceColumnList);
//            integrationVo.setParamObj(paramObj);
////            integrationVo.getParamObj().putAll(jsonObj);
//            IntegrationResultVo resultVo = handler.sendRequest(integrationVo, FrameworkRequestFrom.FORM);
//            if (StringUtils.isNotBlank(resultVo.getError())) {
//                logger.error(resultVo.getError());
//                throw new IntegrationSendRequestException(integrationVo.getName());
//            }
//            resultList = integrationCrossoverService.getTbodyList(resultVo, columnList);
//        }
//
//        //去重
//        List<String> exsited = new ArrayList<>();
//        Iterator<Map<String, Object>> iterator = resultList.iterator();
//        while (iterator.hasNext()) {
//            Map<String, Object> resultObj = iterator.next();
//            String compose = JSONObject.toJSONString(resultObj);
//            if (exsited.contains(compose)) {
//                iterator.remove();
//            } else {
//                exsited.add(compose);
//            }
//        }
//        returnObj.put("tbodyList", resultList);
        return returnObj;
    }
}

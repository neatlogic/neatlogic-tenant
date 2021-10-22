/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.integration.table;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.exception.integration.IntegrationHandlerNotFoundException;
import codedriver.framework.exception.integration.IntegrationNotFoundException;
import codedriver.framework.exception.integration.IntegrationSendRequestException;
import codedriver.framework.exception.integration.IntegrationTableColumnNotFoundException;
import codedriver.framework.exception.type.ParamIrregularException;
import codedriver.framework.integration.core.IIntegrationHandler;
import codedriver.framework.integration.core.IntegrationHandlerFactory;
import codedriver.framework.integration.dao.mapper.IntegrationMapper;
import codedriver.framework.integration.dto.IntegrationResultVo;
import codedriver.framework.integration.dto.IntegrationVo;
import codedriver.framework.integration.dto.table.ColumnVo;
import codedriver.framework.integration.dto.table.DataSearchVo;
import codedriver.framework.integration.dto.table.SourceColumnVo;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.tenant.integration.handler.FrameworkRequestFrom;
import codedriver.module.tenant.service.integration.IntegrationService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.*;

//@Service
@Deprecated
@OperationType(type = OperationTypeEnum.SEARCH)
public class TableDataInitApi extends PrivateApiComponentBase {

    private final static Logger logger = LoggerFactory.getLogger(TableDataInitApi.class);

    @Resource
    private IntegrationService integrationService;

    @Resource
    private IntegrationMapper integrationMapper;

    @Override
    public String getToken() {
        return "integration/table/data/init";
    }

    @Override
    public String getName() {
        return "集成属性数据回显-table接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "integrationUuid", desc = "集成Uuid", type = ApiParamType.STRING, isRequired = true),
            @Param(name = "columnList", desc = "目标属性集合，数据按这个字段顺序返回", type = ApiParamType.JSONARRAY, isRequired = true),
            @Param(name = "defaulValue", desc = "需要回显的数据uuid集合", type = ApiParamType.JSONARRAY),
            @Param(name = "uuidColumn", desc = "uuid对应的属性", type = ApiParamType.STRING, isRequired = true),
            @Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否需要分页，默认true"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页条目"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页"),
//            @Param(name = "arrayColumnList", desc = "需要将值转化成数组的属性集合", type = ApiParamType.JSONARRAY)
    })
    @Description(desc = "集成属性数据回显-table接口")
    @Output({
            @Param(name = "theadList", type = ApiParamType.JSONARRAY, desc = "表格头"),
            @Param(name = "tbodyList", type = ApiParamType.JSONARRAY, desc = "表格数据"),
//            @Param(explode = BasePageVo.class)
    })
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject returnObj = new JSONObject();
        DataSearchVo dataVo = JSONObject.toJavaObject(jsonObj, DataSearchVo.class);
        List<String> columnList = dataVo.getColumnList();
        if (CollectionUtils.isEmpty(columnList)) {
            throw new ParamIrregularException("columnList");
        }
        IntegrationVo integrationVo = integrationMapper.getIntegrationByUuid(dataVo.getIntegrationUuid());
        if (integrationVo == null) {
            throw new IntegrationNotFoundException(dataVo.getIntegrationUuid());
        }
        IIntegrationHandler handler = IntegrationHandlerFactory.getHandler(integrationVo.getHandler());
        if (handler == null) {
            throw new IntegrationHandlerNotFoundException(integrationVo.getHandler());
        }

        List<ColumnVo> columnVoList = integrationService.getColumnList(integrationVo);
        if (CollectionUtils.isNotEmpty(columnVoList)) {
            JSONArray theadList = integrationService.getTheadList(integrationVo, columnList);
            returnObj.put("theadList", theadList);
            List<Map<String, Object>> tbodyArray = new ArrayList<>();
            returnObj.put("tbodyList", tbodyArray);
            JSONArray defaultValue = dataVo.getDefaultValue();
            if (CollectionUtils.isNotEmpty(defaultValue)) {
                String uuidColumn = jsonObj.getString("uuidColumn");
                boolean uuidColumnExist = false;
                for (ColumnVo columnVo : columnVoList) {
                    if (Objects.equals(columnVo.getUuid(), uuidColumn)) {
                        uuidColumnExist = true;
                    }
                }
                if (!uuidColumnExist) {
                    throw new IntegrationTableColumnNotFoundException(integrationVo.getName(), uuidColumn);
                }
                List<SourceColumnVo> sourceColumnList = new ArrayList<>();
                SourceColumnVo sourceColumnVo = new SourceColumnVo();
                sourceColumnVo.setColumn(uuidColumn);
//                List<String> uuidValueList = defaultValue.toJavaList(String.class);
                for (Object uuidValue : defaultValue) {
                    sourceColumnVo.setValue(uuidValue);
                    sourceColumnList.clear();
                    sourceColumnList.add(sourceColumnVo);
                    jsonObj.put("sourceColumnList", sourceColumnList);
                    integrationVo.getParamObj().putAll(jsonObj);
                    IntegrationResultVo resultVo = handler.sendRequest(integrationVo, FrameworkRequestFrom.FORM);
                    if (StringUtils.isNotBlank(resultVo.getError())) {
                        logger.error(resultVo.getError());
                        throw new IntegrationSendRequestException(integrationVo.getName());
                    }
                    handler.validate(resultVo);
                    List<Map<String, Object>> tbodyList = integrationService.getTbodyList(resultVo, dataVo.getColumnList());
                    for (Map<String, Object> tbodyObj : tbodyList) {
                        if (Objects.equals(uuidValue, tbodyObj.get(uuidColumn))) {
                            tbodyArray.add(tbodyObj);
                            break;
                        }
                    }
                }
            }
//            else {
//                IntegrationResultVo resultVo = handler.sendRequest(integrationVo, RequestFrom.MATRIX);
//                if (StringUtils.isNotBlank(resultVo.getError())) {
//                    logger.error(resultVo.getError());
//                    throw new MatrixExternalAccessException();
//                }
//                handler.validate(resultVo);
//                JSONObject transformedResult = JSONObject.parseObject(resultVo.getTransformedResult());
//                returnObj.put("currentPage", transformedResult.get("currentPage"));
//                returnObj.put("pageSize", transformedResult.get("pageSize"));
//                returnObj.put("pageCount", transformedResult.get("pageCount"));
//                returnObj.put("rowNum", transformedResult.get("rowNum"));
//                tbodyList = integrationService.getTbodyList(resultVo, dataVo.getColumnList());
//            }
            /** 将arrayColumnList包含的属性值转成数组 **/
//            JSONArray arrayColumnArray = jsonObj.getJSONArray("arrayColumnList");
//            if (CollectionUtils.isNotEmpty(arrayColumnArray)) {
//                List<String> arrayColumnList = arrayColumnArray.toJavaList(String.class);
//                if (CollectionUtils.isNotEmpty(tbodyArray)) {
//                    integrationService.arrayColumnDataConversion(arrayColumnList, tbodyArray);
//                }
//            }
        }
        return returnObj;
    }
}

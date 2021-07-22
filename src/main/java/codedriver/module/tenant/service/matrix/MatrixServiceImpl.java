package codedriver.module.tenant.service.matrix;

import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.dao.mapper.RoleMapper;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.RoleVo;
import codedriver.framework.dto.TeamVo;
import codedriver.framework.dto.UserVo;
import codedriver.framework.exception.core.ApiRuntimeException;
import codedriver.framework.exception.integration.IntegrationHandlerNotFoundException;
import codedriver.framework.exception.integration.IntegrationNotFoundException;
import codedriver.framework.exception.util.FreemarkerTransformException;
import codedriver.framework.integration.core.IIntegrationHandler;
import codedriver.framework.integration.core.IntegrationHandlerFactory;
import codedriver.framework.integration.dao.mapper.IntegrationMapper;
import codedriver.framework.integration.dto.IntegrationResultVo;
import codedriver.framework.integration.dto.IntegrationVo;
import codedriver.framework.matrix.constvalue.MatrixAttributeType;
import codedriver.framework.matrix.dao.mapper.MatrixDataMapper;
import codedriver.framework.matrix.dto.MatrixAttributeVo;
import codedriver.framework.matrix.dto.MatrixDataVo;
import codedriver.framework.matrix.exception.MatrixExternalAccessException;
import codedriver.framework.util.JavascriptUtil;
import codedriver.framework.util.TimeUtil;
import codedriver.module.tenant.integration.handler.FrameworkRequestFrom;
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
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

/**
 *
 * @description:
 * @since 2020-03-27 11:35
 **/
@Service
@Transactional
public class MatrixServiceImpl implements MatrixService {

    private static final Logger logger = LoggerFactory.getLogger(MatrixServiceImpl.class);
    @Resource
    private MatrixDataMapper matrixDataMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private TeamMapper teamMapper;

    @Resource
    private RoleMapper roleMapper;

    @Resource
    private IntegrationMapper integrationMapper;

    @Override
    public List<MatrixAttributeVo> getExternalMatrixAttributeList(String matrixUuid, IntegrationVo integrationVo) throws FreemarkerTransformException {
        List<MatrixAttributeVo> processMatrixAttributeList = new ArrayList<>();
        JSONObject config = integrationVo.getConfig();
        if (MapUtils.isNotEmpty(config)) {
            JSONObject output = config.getJSONObject("output");
            if (MapUtils.isNotEmpty(output)) {
                String content = output.getString("content");
                if (StringUtils.isNotBlank(content)) {
                    try {
                        content = JavascriptUtil.transform(new JSONObject(), content);
                        JSONObject contentObj = JSON.parseObject(content);
                        if (MapUtils.isNotEmpty(contentObj)) {
                            JSONArray theadList = contentObj.getJSONArray("theadList");
                            if (CollectionUtils.isNotEmpty(theadList)) {
                                for (int i = 0; i < theadList.size(); i++) {
                                    JSONObject theadObj = theadList.getJSONObject(i);
                                    MatrixAttributeVo processMatrixAttributeVo = new MatrixAttributeVo();
                                    processMatrixAttributeVo.setMatrixUuid(matrixUuid);
                                    processMatrixAttributeVo.setUuid(theadObj.getString("key"));
                                    processMatrixAttributeVo.setName(theadObj.getString("title"));
                                    processMatrixAttributeVo.setType(MatrixAttributeType.INPUT.getValue());
                                    processMatrixAttributeVo.setIsDeletable(0);
                                    processMatrixAttributeVo.setSort(i);
                                    processMatrixAttributeVo.setIsRequired(0);
                                    Integer isSearchable = theadObj.getInteger("isSearchable");
                                    processMatrixAttributeVo.setIsSearchable((isSearchable == null || isSearchable.intValue() != 1) ? 0 : 1);
                                    processMatrixAttributeList.add(processMatrixAttributeVo);
                                }
                            }
                        }
                    } catch (Exception ex) {
                        logger.error(ex.getMessage(), ex);
                    }
                }
            }
        }
        return processMatrixAttributeList;
    }

    @Override
    public List<Map<String, Object>> matrixTableDataValueHandle(List<MatrixAttributeVo> ProcessMatrixAttributeList, List<Map<String, String>> valueList) {
        if (CollectionUtils.isNotEmpty(ProcessMatrixAttributeList)) {
            Map<String, MatrixAttributeVo> processMatrixAttributeMap = new HashMap<>();
            for (MatrixAttributeVo processMatrixAttributeVo : ProcessMatrixAttributeList) {
                processMatrixAttributeMap.put(processMatrixAttributeVo.getUuid(), processMatrixAttributeVo);
            }
            if (CollectionUtils.isNotEmpty(valueList)) {
                List<Map<String, Object>> resultList = new ArrayList<>(valueList.size());
                for (Map<String, String> valueMap : valueList) {
                    Map<String, Object> resultMap = new HashMap<>();
                    for (Entry<String, String> entry : valueMap.entrySet()) {
                        String attributeUuid = entry.getKey();
                        resultMap.put(attributeUuid, matrixAttributeValueHandle(processMatrixAttributeMap.get(attributeUuid), entry.getValue()));
                    }
                    resultList.add(resultMap);
                }
                return resultList;
            }
        }
        return null;
    }

    @Override
    public JSONObject matrixAttributeValueHandle(MatrixAttributeVo processMatrixAttribute, Object valueObj) {
        JSONObject resultObj = new JSONObject();
        String type = MatrixAttributeType.INPUT.getValue();
        if (processMatrixAttribute != null) {
            type = processMatrixAttribute.getType();
        }
        resultObj.put("type", type);
        if (valueObj == null) {
            resultObj.put("value", null);
            resultObj.put("text", null);
            return resultObj;
        }
        String value = valueObj.toString();
        resultObj.put("value", value);
        resultObj.put("text", value);
        if (MatrixAttributeType.SELECT.getValue().equals(type)) {
            if (processMatrixAttribute != null) {
                String config = processMatrixAttribute.getConfig();
                if (StringUtils.isNotBlank(config)) {
                    JSONObject configObj = JSON.parseObject(config);
                    JSONArray dataList = configObj.getJSONArray("dataList");
                    if (CollectionUtils.isNotEmpty(dataList)) {
                        for (int i = 0; i < dataList.size(); i++) {
                            JSONObject data = dataList.getJSONObject(i);
                            if (Objects.equals(value, data.getString("value"))) {
                                resultObj.put("text", data.getString("text"));
                            }
                        }
                    }
                }
            }
        } else if (MatrixAttributeType.USER.getValue().equals(type)) {
            UserVo userVo = userMapper.getUserBaseInfoByUuid(value);
            if (userVo != null) {
                resultObj.put("text", userVo.getUserName());
                resultObj.put("avatar", userVo.getAvatar());
                resultObj.put("pinyin", userVo.getPinyin());
                resultObj.put("vipLevel", userVo.getVipLevel());
            }
        } else if (MatrixAttributeType.TEAM.getValue().equals(type)) {
            TeamVo teamVo = teamMapper.getTeamByUuid(value);
            if (teamVo != null) {
                resultObj.put("text", teamVo.getName());
            }
        } else if (MatrixAttributeType.ROLE.getValue().equals(type)) {
            RoleVo roleVo = roleMapper.getRoleByUuid(value);
            if (roleVo != null) {
                resultObj.put("text", roleVo.getName());
            }
        }
        return resultObj;
    }

    @Override
    public JSONObject matrixAttributeValueHandle(Object value) {
        return matrixAttributeValueHandle(null, value);
    }

    @Override
    public List<Map<String, String>> matrixAttributeValueKeyWordSearch(MatrixAttributeVo processMatrixAttribute, MatrixDataVo dataVo) {
        dataVo.setPageSize(dataVo.getPageSize() * 10);
        if (processMatrixAttribute != null) {
            dataVo.setAttrType(processMatrixAttribute.getType());
            dataVo.setAttributeUuid(processMatrixAttribute.getUuid());
        }
        List<Map<String, String>> dataMapList = matrixDataMapper.getDynamicTableDataByColumnList2(dataVo, TenantContext.get().getTenantUuid());
        return dataMapList;
    }

    @Override
    public List<Map<String, JSONObject>> getExternalDataTbodyList(IntegrationResultVo resultVo, List<String> columnList, int pageSize, JSONObject resultObj) {
        List<Map<String, JSONObject>> resultList = new ArrayList<>();
        if (resultVo != null && StringUtils.isNotBlank(resultVo.getTransformedResult())) {
            JSONObject transformedResult = JSONObject.parseObject(resultVo.getTransformedResult());
            if (MapUtils.isNotEmpty(transformedResult)) {
                if (resultObj != null) {
                    resultObj.putAll(transformedResult);
                }
                JSONArray tbodyList = transformedResult.getJSONArray("tbodyList");
                if (CollectionUtils.isNotEmpty(tbodyList)) {
                    for (int i = 0; i < tbodyList.size(); i++) {
                        JSONObject rowData = tbodyList.getJSONObject(i);
                        Map<String, JSONObject> resultMap = new HashMap<>(columnList.size());
                        for (String column : columnList) {
                            String columnValue = rowData.getString(column);
                            resultMap.put(column, matrixAttributeValueHandle(columnValue));
                        }
                        resultList.add(resultMap);
                        if (resultList.size() >= pageSize) {
                            break;
                        }
                    }
                    if (resultObj != null) {
                        resultObj.put("tbodyList", resultList);
                    }
                }
            }
        }
        return resultList;
    }

    @Override
    public void arrayColumnDataConversion(List<String> arrayColumnList, JSONArray tbodyList) {
        for (int i = 0; i < tbodyList.size(); i++) {
            JSONObject rowData = tbodyList.getJSONObject(i);
            for (Entry<String, Object> entry : rowData.entrySet()) {
                if (arrayColumnList.contains(entry.getKey())) {
                    List<ValueTextVo> valueObjList = new ArrayList<>();
                    JSONObject valueObj = (JSONObject) entry.getValue();
                    String value = valueObj.getString("value");
                    if (StringUtils.isNotBlank(value)) {
                        if (value.startsWith("[") && value.endsWith("]")) {
                            List<String> valueList = valueObj.getJSONArray("value").toJavaList(String.class);
                            for (String valueStr : valueList) {
                                valueObjList.add(new ValueTextVo(valueStr, valueStr));
                            }
                        } else {
                            valueObjList.add(new ValueTextVo(value, value));
                        }
                    }
                    valueObj.put("value", valueObjList);
                }
            }
        }
    }

    @Override
    public boolean matrixAttributeValueVerify(MatrixAttributeVo matrixAttributeVo, String value) {
        String type = matrixAttributeVo.getType();
        if (type.equals(MatrixAttributeType.INPUT.getValue())) {
            return true;
        } else if (type.equals(MatrixAttributeType.SELECT.getValue())) {
            String config = matrixAttributeVo.getConfig();
            JSONArray dataList = (JSONArray) JSONPath.read(config, "dataList");
            for (int i = 0; i < dataList.size(); i++) {
                JSONObject dataObj = dataList.getJSONObject(i);
                if (value.equals(dataObj.getString("value"))) {
                    return true;
                }
            }
        } else if (type.equals(MatrixAttributeType.DATE.getValue())) {
            SimpleDateFormat sdf = new SimpleDateFormat(TimeUtil.YYYY_MM_DD_HH_MM_SS);
            try {
                sdf.parse(value);
            } catch (ParseException e) {
                return false;
            }
            return true;
        } else if (type.equals(MatrixAttributeType.USER.getValue())) {
            return userMapper.checkUserIsExists(value) > 0;
        } else if (type.equals(MatrixAttributeType.TEAM.getValue())) {
            return teamMapper.checkTeamIsExists(value) > 0;
        } else if (type.equals(MatrixAttributeType.ROLE.getValue())) {
            return roleMapper.checkRoleIsExists(value) > 0;
        }
        return false;
    }

    /**
     * 校验集成接口数据是否符合矩阵格式
     * @param integrationUuid 集成配置uuid
     * @throws ApiRuntimeException
     */
    @Override
    public void validateMatrixExternalData(String integrationUuid) throws ApiRuntimeException {
        IntegrationVo integrationVo = integrationMapper.getIntegrationByUuid(integrationUuid);
        if(integrationVo == null){
            throw new IntegrationNotFoundException(integrationUuid);
        }
        IIntegrationHandler handler = IntegrationHandlerFactory.getHandler(integrationVo.getHandler());
        if (handler == null) {
            throw  new IntegrationHandlerNotFoundException(integrationVo.getHandler());
        }
        IntegrationResultVo resultVo = handler.sendRequest(integrationVo, FrameworkRequestFrom.TEST);
        if(StringUtils.isNotBlank(resultVo.getError())){
            throw new MatrixExternalAccessException();
        }
        handler.validate(resultVo);
    }
}

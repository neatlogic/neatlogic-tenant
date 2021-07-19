/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.matrix;

import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.constvalue.GroupSearch;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.dao.mapper.RoleMapper;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.RoleVo;
import codedriver.framework.dto.TeamVo;
import codedriver.framework.dto.UserVo;
import codedriver.framework.exception.integration.IntegrationHandlerNotFoundException;
import codedriver.framework.integration.core.IIntegrationHandler;
import codedriver.framework.integration.core.IntegrationHandlerFactory;
import codedriver.framework.integration.core.RequestFrom;
import codedriver.framework.integration.dao.mapper.IntegrationMapper;
import codedriver.framework.integration.dto.IntegrationResultVo;
import codedriver.framework.integration.dto.IntegrationVo;
import codedriver.framework.matrix.constvalue.MatrixType;
import codedriver.framework.matrix.dao.mapper.MatrixAttributeMapper;
import codedriver.framework.matrix.dao.mapper.MatrixDataMapper;
import codedriver.framework.matrix.dao.mapper.MatrixExternalMapper;
import codedriver.framework.matrix.dao.mapper.MatrixMapper;
import codedriver.framework.matrix.dto.MatrixAttributeVo;
import codedriver.framework.matrix.dto.MatrixDataVo;
import codedriver.framework.matrix.dto.MatrixExternalVo;
import codedriver.framework.matrix.dto.MatrixVo;
import codedriver.framework.matrix.exception.MatrixExternalAccessException;
import codedriver.framework.matrix.exception.MatrixExternalException;
import codedriver.framework.matrix.exception.MatrixExternalNotFoundException;
import codedriver.framework.matrix.exception.MatrixNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateBinaryStreamApiComponentBase;
import codedriver.framework.util.ExcelUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @program: codedriver
 * @description:
 * @create: 2020-03-26 19:04
 **/
@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class MatrixExportApi extends PrivateBinaryStreamApiComponentBase {

    private final static Logger logger = LoggerFactory.getLogger(MatrixExportApi.class);

    @Resource
    private MatrixAttributeMapper attributeMapper;

    @Resource
    private MatrixMapper matrixMapper;

    @Resource
    private MatrixDataMapper matrixDataMapper;

    @Resource
    private IntegrationMapper integrationMapper;

    @Resource
    private MatrixExternalMapper externalMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private TeamMapper teamMapper;

    @Resource
    private RoleMapper roleMapper;

    @Override
    public String getToken() {
        return "matrix/export";
    }

    @Override
    public String getName() {
        return "矩阵导出接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @SuppressWarnings({"unchecked"})
    @Input({@Param(name = "matrixUuid", desc = "矩阵uuid", type = ApiParamType.STRING, isRequired = true)})
    @Description(desc = "矩阵导出接口")
    @Override
    public Object myDoService(JSONObject paramObj, HttpServletRequest request, HttpServletResponse response) throws Exception {
        String matrixUuid = paramObj.getString("matrixUuid");
        MatrixVo matrixVo = matrixMapper.getMatrixByUuid(matrixUuid);
        if (matrixVo == null) {
            throw new MatrixNotFoundException(matrixUuid);
        }

        HSSFWorkbook workbook = null;
        if (MatrixType.CUSTOM.getValue().equals(matrixVo.getType())) {
            List<MatrixAttributeVo> attributeVoList = attributeMapper.getMatrixAttributeByMatrixUuid(matrixUuid);
            if (CollectionUtils.isNotEmpty(attributeVoList)) {
                List<String> headerList = new ArrayList<>();
                List<String> columnList = new ArrayList<>();
                List<List<String>> columnSelectValueList = new ArrayList<>();
                headerList.add("uuid");
                columnList.add("uuid");
                columnSelectValueList.add(new ArrayList<>());
                for (MatrixAttributeVo attributeVo : attributeVoList) {
                    headerList.add(attributeVo.getName());
                    columnList.add(attributeVo.getUuid());
                    List<String> selectValueList = new ArrayList<>();
                    decodeDataConfig(attributeVo, selectValueList);
                    columnSelectValueList.add(selectValueList);
                }
                MatrixDataVo dataVo = new MatrixDataVo();
                dataVo.setMatrixUuid(paramObj.getString("matrixUuid"));
                dataVo.setColumnList(columnList);

                int currentPage = 1;
                dataVo.setPageSize(1000);
                int rowNum = matrixDataMapper.getDynamicTableDataCount(dataVo, TenantContext.get().getTenantUuid());
                int pageCount = PageUtil.getPageCount(rowNum, dataVo.getPageSize());
                while (currentPage <= pageCount) {
                    dataVo.setCurrentPage(currentPage);
                    dataVo.setStartNum(null);
                    List<Map<String, String>> dataMapList = matrixDataMapper.searchDynamicTableData(dataVo, TenantContext.get().getTenantUuid());
                    /** 转换用户、分组、角色字段值为用户名、分组名、角色名 **/
                    if (CollectionUtils.isNotEmpty(dataMapList)) {
                        for (Map<String, String> map : dataMapList) {
                            for (MatrixAttributeVo attributeVo : attributeVoList) {
                                String value = map.get(attributeVo.getUuid());
                                if (StringUtils.isNotBlank(value)) {
                                    if (GroupSearch.USER.getValue().equals(attributeVo.getType())) {
                                        UserVo user = userMapper.getUserBaseInfoByUuid(value);
                                        if (user != null) {
                                            map.put(attributeVo.getUuid(), user.getUserName());
                                        }
                                    } else if (GroupSearch.TEAM.getValue().equals(attributeVo.getType())) {
                                        TeamVo team = teamMapper.getTeamByUuid(value);
                                        if (team != null) {
                                            map.put(attributeVo.getUuid(), team.getName());
                                        }
                                    } else if (GroupSearch.ROLE.getValue().equals(attributeVo.getType())) {
                                        RoleVo role = roleMapper.getRoleByUuid(value);
                                        if (role != null) {
                                            map.put(attributeVo.getUuid(), role.getName());
                                        }
                                    }
                                }
                            }
                        }
                    }
                    workbook = ExcelUtil.createExcel(workbook, headerList, columnList, columnSelectValueList, dataMapList);
                    currentPage++;
                }
            }
        } else {
            MatrixExternalVo externalVo = externalMapper.getMatrixExternalByMatrixUuid(matrixUuid);
            if (externalVo == null) {
                throw new MatrixExternalNotFoundException(matrixVo.getName());
            }
            IntegrationVo integrationVo = integrationMapper.getIntegrationByUuid(externalVo.getIntegrationUuid());
            IIntegrationHandler handler = IntegrationHandlerFactory.getHandler(integrationVo.getHandler());
            if (handler == null) {
                throw new IntegrationHandlerNotFoundException(integrationVo.getHandler());
            }

            IntegrationResultVo resultVo = handler.sendRequest(integrationVo, RequestFrom.MATRIX);
            if (StringUtils.isNotBlank(resultVo.getError())) {
                logger.error(resultVo.getError());
                throw new MatrixExternalAccessException();
            } else if (StringUtils.isNotBlank(resultVo.getTransformedResult())) {
                JSONObject transformedResult = JSONObject.parseObject(resultVo.getTransformedResult());
                if (MapUtils.isNotEmpty(transformedResult)) {
                    List<String> headerList = new ArrayList<>();
                    List<String> columnList = new ArrayList<>();
                    JSONArray theadList = transformedResult.getJSONArray("theadList");
                    if (CollectionUtils.isNotEmpty(theadList)) {
                        for (int i = 0; i < theadList.size(); i++) {
                            JSONObject obj = theadList.getJSONObject(i);
                            headerList.add(obj.getString("title"));
                            columnList.add(obj.getString("key"));
                        }
                    }
                    List<Map<String, String>> dataMapList = (List<Map<String, String>>) transformedResult.get("tbodyList");
                    workbook = ExcelUtil.createExcel(workbook, headerList, columnList, null, dataMapList);
                }
            }
        }

        if (workbook == null) {
            workbook = new HSSFWorkbook();
        }
        String fileNameEncode = matrixVo.getName() + ".xls";
        Boolean flag = request.getHeader("User-Agent").indexOf("Gecko") > 0;
        if (request.getHeader("User-Agent").toLowerCase().indexOf("msie") > 0 || flag) {
            fileNameEncode = URLEncoder.encode(fileNameEncode, "UTF-8");// IE浏览器
        } else {
            fileNameEncode = new String(fileNameEncode.replace(" ", "").getBytes(StandardCharsets.UTF_8), "ISO8859-1");
        }
        response.setContentType("application/vnd.ms-excel;charset=utf-8");
        response.setHeader("Content-Disposition", " attachment; filename=\"" + fileNameEncode + "\"");

        try (OutputStream os = response.getOutputStream();) {
            workbook.write(os);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    //解析config，抽取属性下拉框值
    private void decodeDataConfig(MatrixAttributeVo attributeVo, List<String> selectValueList) {
        if (StringUtils.isNotBlank(attributeVo.getConfig())) {
            String config = attributeVo.getConfig();
            JSONObject configObj = JSONObject.parseObject(config);
            JSONArray dataList = configObj.getJSONArray("dataList");
            if (CollectionUtils.isNotEmpty(dataList)) {
                for (int i = 0; i < dataList.size(); i++) {
                    JSONObject dataObj = dataList.getJSONObject(i);
                    if (MapUtils.isNotEmpty(dataObj)) {
                        String value = dataObj.getString("value");
                        if (StringUtils.isNotBlank(value)) {
                            selectValueList.add(value);
                        }
                    }
                }
            }
//            if (AttributeHandler.SELECT.getValue().equals(configObj.getString("handler"))){
//                if (configObj.containsKey("config")){
//                    JSONArray configArray = configObj.getJSONArray("config");
//                    for (int i = 0; i < configArray.size(); i++){
//                        JSONObject param = configArray.getJSONObject(i);
//                        selectValueList.add(param.getString("value"));
//                    }
//                }
//            }
        }
    }
}

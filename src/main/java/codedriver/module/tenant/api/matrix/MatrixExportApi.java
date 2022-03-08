/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.matrix;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.constvalue.ExportFileType;
import codedriver.framework.matrix.core.IMatrixDataSourceHandler;
import codedriver.framework.matrix.core.MatrixDataSourceHandlerFactory;
import codedriver.framework.matrix.dao.mapper.MatrixMapper;
import codedriver.framework.matrix.dto.MatrixAttributeVo;
import codedriver.framework.matrix.dto.MatrixVo;
import codedriver.framework.matrix.exception.MatrixDataSourceHandlerNotFoundException;
import codedriver.framework.matrix.exception.MatrixNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateBinaryStreamApiComponentBase;
import codedriver.framework.util.FileUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * @program: codedriver
 * @description:
 * @create: 2020-03-26 19:04
 **/
@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class MatrixExportApi extends PrivateBinaryStreamApiComponentBase {

    @Resource
    private MatrixMapper matrixMapper;

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
        IMatrixDataSourceHandler matrixDataSourceHandler = MatrixDataSourceHandlerFactory.getHandler(matrixVo.getType());
        if (matrixDataSourceHandler == null) {
            throw new MatrixDataSourceHandlerNotFoundException(matrixVo.getType());
        }
        String exportFileType = matrixDataSourceHandler.getExportFileType();
        try (OutputStream os = response.getOutputStream()) {
            if (ExportFileType.CSV.getValue().equals(exportFileType)) {
                String fileName = FileUtil.getEncodedFileName(request.getHeader("User-Agent"), matrixVo.getName() + ".csv");
                response.setContentType("application/text;charset=GBK");
                response.setHeader("Content-Disposition", " attachment; filename=\"" + fileName + "\"");
                matrixDataSourceHandler.exportMatrix2CSV(matrixVo, os);
            } else if (ExportFileType.EXCEL.getValue().equals(exportFileType)) {
                Workbook workbook = matrixDataSourceHandler.exportMatrix2Excel(matrixVo);
                if (workbook == null) {
                    workbook = new HSSFWorkbook();
                }
                String fileName = FileUtil.getEncodedFileName(request.getHeader("User-Agent"), matrixVo.getName() + ".xls");
                response.setContentType("application/vnd.ms-excel;charset=utf-8");
                response.setHeader("Content-Disposition", " attachment; filename=\"" + fileName + "\"");
                workbook.write(os);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

//        HSSFWorkbook workbook = null;
//        if (MatrixType.CUSTOM.getValue().equals(matrixVo.getType())) {
//            List<MatrixAttributeVo> attributeVoList = attributeMapper.getMatrixAttributeByMatrixUuid(matrixUuid);
//            if (CollectionUtils.isNotEmpty(attributeVoList)) {
//                List<String> headerList = new ArrayList<>();
//                List<String> columnList = new ArrayList<>();
//                List<List<String>> columnSelectValueList = new ArrayList<>();
//                headerList.add("uuid");
//                columnList.add("uuid");
//                columnSelectValueList.add(new ArrayList<>());
//                for (MatrixAttributeVo attributeVo : attributeVoList) {
//                    headerList.add(attributeVo.getName());
//                    columnList.add(attributeVo.getUuid());
//                    List<String> selectValueList = new ArrayList<>();
//                    decodeDataConfig(attributeVo, selectValueList);
//                    columnSelectValueList.add(selectValueList);
//                }
//                MatrixDataVo dataVo = new MatrixDataVo();
//                dataVo.setMatrixUuid(paramObj.getString("matrixUuid"));
//                dataVo.setColumnList(columnList);
//
//                int currentPage = 1;
//                dataVo.setPageSize(1000);
//                int rowNum = matrixDataMapper.getDynamicTableDataCount(dataVo);
//                int pageCount = PageUtil.getPageCount(rowNum, dataVo.getPageSize());
//                while (currentPage <= pageCount) {
//                    dataVo.setCurrentPage(currentPage);
//                    dataVo.setStartNum(null);
//                    List<Map<String, String>> dataMapList = matrixDataMapper.searchDynamicTableData(dataVo);
//                    /* 转换用户、分组、角色字段值为用户名、分组名、角色名 **/
//                    if (CollectionUtils.isNotEmpty(dataMapList)) {
//                        for (Map<String, String> map : dataMapList) {
//                            for (MatrixAttributeVo attributeVo : attributeVoList) {
//                                String value = map.get(attributeVo.getUuid());
//                                if (StringUtils.isNotBlank(value)) {
//                                    if (GroupSearch.USER.getValue().equals(attributeVo.getType())) {
//                                        UserVo user = userMapper.getUserBaseInfoByUuid(value);
//                                        if (user != null) {
//                                            map.put(attributeVo.getUuid(), user.getUserName());
//                                        }
//                                    } else if (GroupSearch.TEAM.getValue().equals(attributeVo.getType())) {
//                                        TeamVo team = teamMapper.getTeamByUuid(value);
//                                        if (team != null) {
//                                            map.put(attributeVo.getUuid(), team.getName());
//                                        }
//                                    } else if (GroupSearch.ROLE.getValue().equals(attributeVo.getType())) {
//                                        RoleVo role = roleMapper.getRoleByUuid(value);
//                                        if (role != null) {
//                                            map.put(attributeVo.getUuid(), role.getName());
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//                    workbook = ExcelUtil.createExcel(workbook, headerList, columnList, columnSelectValueList, dataMapList);
//                    currentPage++;
//                }
//            }
//        } else {
//            MatrixExternalVo externalVo = externalMapper.getMatrixExternalByMatrixUuid(matrixUuid);
//            if (externalVo == null) {
//                throw new MatrixExternalNotFoundException(matrixVo.getName());
//            }
//            IntegrationVo integrationVo = integrationMapper.getIntegrationByUuid(externalVo.getIntegrationUuid());
//            IIntegrationHandler handler = IntegrationHandlerFactory.getHandler(integrationVo.getHandler());
//            if (handler == null) {
//                throw new IntegrationHandlerNotFoundException(integrationVo.getHandler());
//            }
//
//            IntegrationResultVo resultVo = handler.sendRequest(integrationVo, RequestFrom.MATRIX);
//            if (StringUtils.isNotBlank(resultVo.getError())) {
//                logger.error(resultVo.getError());
//                throw new MatrixExternalAccessException();
//            } else if (StringUtils.isNotBlank(resultVo.getTransformedResult())) {
//                JSONObject transformedResult = JSONObject.parseObject(resultVo.getTransformedResult());
//                if (MapUtils.isNotEmpty(transformedResult)) {
//                    List<String> headerList = new ArrayList<>();
//                    List<String> columnList = new ArrayList<>();
//                    JSONArray theadList = transformedResult.getJSONArray("theadList");
//                    if (CollectionUtils.isNotEmpty(theadList)) {
//                        for (int i = 0; i < theadList.size(); i++) {
//                            JSONObject obj = theadList.getJSONObject(i);
//                            headerList.add(obj.getString("title"));
//                            columnList.add(obj.getString("key"));
//                        }
//                    }
//                    List<Map<String, String>> dataMapList = (List<Map<String, String>>) transformedResult.get("tbodyList");
//                    workbook = ExcelUtil.createExcel(workbook, headerList, columnList, null, dataMapList);
//                }
//            }
//        }
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

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

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.matrix.core.IMatrixDataSourceHandler;
import neatlogic.framework.matrix.core.MatrixDataSourceHandlerFactory;
import neatlogic.framework.matrix.dao.mapper.MatrixMapper;
import neatlogic.framework.matrix.dto.MatrixVo;
import neatlogic.framework.matrix.exception.*;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateBinaryStreamApiComponentBase;
import neatlogic.framework.auth.label.MATRIX_MODIFY;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Service
@Transactional
@AuthAction(action = MATRIX_MODIFY.class)
@OperationType(type = OperationTypeEnum.CREATE)
public class MatrixImportApi extends PrivateBinaryStreamApiComponentBase {

    @Resource
    private MatrixMapper matrixMapper;

    @Override
    public String getToken() {
        return "matrix/data/import";
    }

    @Override
    public String getName() {
        return "nmtam.matriximportapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "matrixUuid", desc = "term.framework.matrixuuid", type = ApiParamType.STRING, isRequired = true)
    })
    @Description(desc = "nmtam.matriximportapi.getname")
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
        JSONObject returnObj = new JSONObject();
        int update = 0, insert = 0, unExist = 0;
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        //获取所有导入文件
        Map<String, MultipartFile> multipartFileMap = multipartRequest.getFileMap();
        if (multipartFileMap.isEmpty()) {
            throw new MatrixFileNotFoundException();
        }
        for (Map.Entry<String, MultipartFile> entry : multipartFileMap.entrySet()) {
            JSONObject resultObj = matrixDataSourceHandler.importMatrix(matrixVo, entry.getValue());
            Integer insertCount = resultObj.getInteger("insert");
            if (insertCount != null) {
                insert += insertCount;
            }
            Integer updateCount = resultObj.getInteger("update");
            if (updateCount != null) {
                update += updateCount;
            }
            Integer unExistCount = resultObj.getInteger("unExist");
            if (unExistCount != null) {
                unExist += unExistCount;
            }
        }
        returnObj.put("insert", insert);
        returnObj.put("update", update);
        returnObj.put("unExist", unExist);
        return returnObj;
//        if (MatrixType.CUSTOM.getValue().equals(matrixVo.getType())) {
//            JSONObject returnObj = new JSONObject();
//            int update = 0, insert = 0, unExist = 0;
//            MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
//            //获取所有导入文件
//            Map<String, MultipartFile> multipartFileMap = multipartRequest.getFileMap();
//            if (multipartFileMap.isEmpty()) {
//                throw new MatrixFileNotFoundException();
//            }
//            MultipartFile multipartFile;
//            InputStream is;
//            for (Map.Entry<String, MultipartFile> entry : multipartFileMap.entrySet()) {
//                multipartFile = entry.getValue();
//                is = multipartFile.getInputStream();
//                String originalFilename = multipartFile.getOriginalFilename();
//                if (StringUtils.isNotBlank(originalFilename) && originalFilename.contains(".")) {
//                    originalFilename = originalFilename.substring(0, originalFilename.indexOf("."));
//                }
//                if (StringUtils.isNotBlank(originalFilename) && !originalFilename.equals(matrixVo.getName())) {
//                    throw new MatrixNameDifferentImportFileNameException();
//                }
//
//                List<MatrixAttributeVo> attributeVoList = attributeMapper.getMatrixAttributeByMatrixUuid(matrixVo.getUuid());
//                if (CollectionUtils.isNotEmpty(attributeVoList)) {
//                    Map<String, MatrixAttributeVo> headerMap = new HashMap<>();
//                    for (MatrixAttributeVo attributeVo : attributeVoList) {
//                        headerMap.put(attributeVo.getName(), attributeVo);
//                    }
//                    Workbook wb = WorkbookFactory.create(is);
//                    Sheet sheet = wb.getSheetAt(0);
//                    int rowNum = sheet.getLastRowNum();
//                    //获取头栏位
//                    Row headerRow = sheet.getRow(0);
//                    int colNum = headerRow.getLastCellNum();
//                    //attributeList 缺少uuid
//                    if (colNum != attributeVoList.size() + 1) {
//                        throw new MatrixHeaderMisMatchException(originalFilename);
//                    }
//                    //解析数据
//                    for (int i = 1; i <= rowNum; i++) {
//                        Row row = sheet.getRow(i);
//                        boolean isNew = false;
//                        MatrixColumnVo uuidColumn = null;
//                        List<MatrixColumnVo> rowData = new ArrayList<>();
//                        for (int j = 0; j < colNum; j++) {
//                            Cell tbodycell = row.getCell(j);
//                            String value = getCellValue(tbodycell);
//                            String attributeUuid;
//                            Cell theadCell = headerRow.getCell(j);
//                            String columnName = theadCell.getStringCellValue();
//                            if (("uuid").equals(columnName)) {
//                                attributeUuid = "uuid";
//                                if (StringUtils.isBlank(value) || dataMapper.getDynamicTableDataCountByUuid(value, matrixVo.getUuid(), TenantContext.get().getDataDbName()) == 0) {
//                                    value = UuidUtil.randomUuid();
//                                    isNew = true;
//                                    rowData.add(new MatrixColumnVo(attributeUuid, value));
//                                } else {
//                                    uuidColumn = new MatrixColumnVo(attributeUuid, value);
//                                }
//                            } else {
//                                MatrixAttributeVo attributeVo = headerMap.get(columnName);
//                                if (attributeVo != null) {
//                                    attributeUuid = attributeVo.getUuid();
//                                    if (StringUtils.isNotBlank(attributeUuid)) {
//                                        if (matrixService.matrixAttributeValueVerify(attributeVo, value)) {
//                                            rowData.add(new MatrixColumnVo(attributeUuid, value));
//                                        } else {
//                                            throw new MatrixImportDataIllegalException(i + 1, j + 1, value);
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                        if (isNew) {
//                            dataMapper.insertDynamicTableData(rowData, matrixUuid, TenantContext.get().getDataDbName());
//                            insert++;
//                            update++;
//                        } else {
//                            dataMapper.updateDynamicTableDataByUuid(rowData, uuidColumn, matrixUuid, TenantContext.get().getDataDbName());
//                        }
//                    }
//                } else {
//                    throw new MatrixDataNotFoundException(originalFilename);
//                }
//            }
//            returnObj.put("insert", insert);
//            returnObj.put("update", update);
//            returnObj.put("unExist", unExist);
//            return returnObj;
//        } else if (MatrixType.EXTERNAL.getValue().equals(matrixVo.getType())) {
//            throw new MatrixExternalImportException();
//        } else if (MatrixType.VIEW.getValue().equals(matrixVo.getType())) {
//            throw new MatrixViewImportException();
//        }
//        return null;
    }

//    private String getCellValue(Cell cell) {
//        String value = "";
//        if (cell != null) {
//            if (cell.getCellType() != CellType.BLANK) {
//                switch (cell.getCellType()) {
//                    case NUMERIC:
//                        if (DateUtil.isCellDateFormatted(cell)) {
//                            value = formatter.format(cell.getDateCellValue());
//                        } else {
//                            value = String.valueOf(cell.getNumericCellValue());
//                        }
//                        break;
//                    case BOOLEAN:
//                        value = String.valueOf(cell.getBooleanCellValue());
//                        break;
//                    case FORMULA:
//                        value = cell.getCellFormula();
//                        break;
//                    default:
//                        value = cell.getStringCellValue();
//                        break;
//                }
//            }
//        }
//        return value;
//    }
}

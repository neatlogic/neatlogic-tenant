/*Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

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

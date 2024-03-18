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
import neatlogic.framework.exception.type.ParamNotExistsException;
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
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.auth.label.MATRIX_MODIFY;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * @program: neatlogic
 * @description:
 * @create: 2020-03-30 15:26
 **/
@Service
@Transactional
@AuthAction(action = MATRIX_MODIFY.class)
@OperationType(type = OperationTypeEnum.CREATE)
public class MatrixDataSaveApi extends PrivateApiComponentBase {

    @Resource
    private MatrixMapper matrixMapper;

    @Override
    public String getToken() {
        return "matrix/data/save";
    }

    @Override
    public String getName() {
        return "矩阵数据保存接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "matrixUuid", desc = "矩阵uuid", type = ApiParamType.STRING, isRequired = true),
            @Param(name = "rowData", desc = "矩阵数据中的一行数据", type = ApiParamType.JSONOBJECT, isRequired = true)

    })
    @Description(desc = "矩阵数据保存接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String matrixUuid = jsonObj.getString("matrixUuid");
        MatrixVo matrixVo = matrixMapper.getMatrixByUuid(matrixUuid);
        if (matrixVo == null) {
            throw new MatrixNotFoundException(matrixUuid);
        }
        JSONObject rowDataObj = jsonObj.getJSONObject("rowData");
        if (MapUtils.isEmpty(rowDataObj)) {
            throw new ParamNotExistsException("rowData");
        }
        IMatrixDataSourceHandler matrixDataSourceHandler = MatrixDataSourceHandlerFactory.getHandler(matrixVo.getType());
        if (matrixDataSourceHandler == null) {
            throw new MatrixDataSourceHandlerNotFoundException(matrixVo.getType());
        }
        matrixDataSourceHandler.saveTableRowData(matrixUuid, rowDataObj);
//        if (MatrixType.CUSTOM.getValue().equals(matrixVo.getType())) {
//            List<MatrixAttributeVo> attributeList = matrixAttributeMapper.getMatrixAttributeByMatrixUuid(matrixUuid);
//            List<String> attributeUuidList = attributeList.stream().map(MatrixAttributeVo::getUuid).collect(Collectors.toList());
////            JSONObject rowDataObj = jsonObj.getJSONObject("rowData");
//            for (String columnUuid : rowDataObj.keySet()) {
//                if (!"uuid".equals(columnUuid) && !"id".equals(columnUuid) && !attributeUuidList.contains(columnUuid)) {
//                    throw new MatrixAttributeNotFoundException(matrixUuid, columnUuid);
//                }
//            }
//
//            boolean hasData = false;
//            List<MatrixColumnVo> rowData = new ArrayList<>();
//            for (MatrixAttributeVo matrixAttributeVo : attributeList) {
//                String value = rowDataObj.getString(matrixAttributeVo.getUuid());
//                if (StringUtils.isNotBlank(value)) {
//                    hasData = true;
//                    if (MatrixAttributeType.USER.getValue().equals(matrixAttributeVo.getType())) {
//                        value = value.split("#")[1];
//                    } else if (MatrixAttributeType.TEAM.getValue().equals(matrixAttributeVo.getType())) {
//                        value = value.split("#")[1];
//                    } else if (MatrixAttributeType.ROLE.getValue().equals(matrixAttributeVo.getType())) {
//                        value = value.split("#")[1];
//                    }
//                }
//                rowData.add(new MatrixColumnVo(matrixAttributeVo.getUuid(), value));
//            }
//            String schemaName = TenantContext.get().getDataDbName();
//            String uuidValue = rowDataObj.getString("uuid");
//            if (uuidValue == null) {
//                if (hasData) {
//                    rowData.add(new MatrixColumnVo("uuid", UuidUtil.randomUuid()));
//                    matrixDataMapper.insertDynamicTableData(rowData, matrixUuid, schemaName);
//                }
//            } else {
//                if (hasData) {
//                    MatrixColumnVo uuidColumn = new MatrixColumnVo("uuid", uuidValue);
//                    matrixDataMapper.updateDynamicTableDataByUuid(rowData, uuidColumn, matrixUuid, schemaName);
//                } else {
//                    matrixDataMapper.deleteDynamicTableDataByUuid(matrixUuid, uuidValue, schemaName);
//                }
//            }
//        } else if (MatrixType.EXTERNAL.getValue().equals(matrixVo.getType())) {
//            throw new MatrixExternalEditRowDataException();
//        } else if (MatrixType.VIEW.getValue().equals(matrixVo.getType())) {
//            throw new MatrixViewEditRowDataException();
//        }

        return null;
    }
}

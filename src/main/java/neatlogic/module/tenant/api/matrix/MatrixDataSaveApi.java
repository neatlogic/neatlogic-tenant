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

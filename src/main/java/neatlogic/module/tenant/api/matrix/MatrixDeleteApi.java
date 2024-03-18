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
import neatlogic.framework.matrix.exception.MatrixDataSourceHandlerNotFoundException;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.auth.label.MATRIX_MODIFY;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * @program: neatlogic
 * @description:
 * @create: 2020-03-26 19:03
 **/
@Service
@Transactional
@AuthAction(action = MATRIX_MODIFY.class)
@OperationType(type = OperationTypeEnum.DELETE)
public class MatrixDeleteApi extends PrivateApiComponentBase {

    @Resource
    private MatrixMapper matrixMapper;

    @Override
    public String getToken() {
        return "matrix/delete";
    }

    @Override
    public String getName() {
        return "矩阵删除接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "uuid", desc = "矩阵uuid", type = ApiParamType.STRING, isRequired = true)})
    @Description(desc = "矩阵删除接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String uuid = jsonObj.getString("uuid");
        MatrixVo matrixVo = matrixMapper.getMatrixByUuid(uuid);
        if (matrixVo != null) {
            IMatrixDataSourceHandler matrixDataSourceHandler = MatrixDataSourceHandlerFactory.getHandler(matrixVo.getType());
            if (matrixDataSourceHandler == null) {
                throw new MatrixDataSourceHandlerNotFoundException(matrixVo.getType());
            }
            matrixDataSourceHandler.deleteMatrix(uuid);
//            if (DependencyManager.getDependencyCount(CalleeType.MATRIX, uuid) > 0) {
//                throw new MatrixReferencedCannotBeDeletedException(uuid);
//            }
//            matrixMapper.deleteMatrixByUuid(uuid);
//            if (MatrixType.CUSTOM.getValue().equals(matrixVo.getType())) {
//                matrixAttributeMapper.deleteAttributeByMatrixUuid(uuid);
//                matrixAttributeMapper.dropMatrixDynamicTable(uuid, TenantContext.get().getDataDbName());
//            } else if (MatrixType.EXTERNAL.getValue().equals(matrixVo.getType())) {
//                matrixExternalMapper.deleteMatrixExternalByMatrixUuid(uuid);
//            } else if (MatrixType.VIEW.getValue().equals(matrixVo.getType())) {
//                matrixViewMapper.deleteMatrixViewByMatrixUuid(uuid);
//                schemaMapper.deleteView(TenantContext.get().getDataDbName() + ".matrix_" + uuid);
//            }
        }
        return null;
    }
}

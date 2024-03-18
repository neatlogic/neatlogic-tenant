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
import neatlogic.framework.matrix.exception.MatrixDataSourceHandlerNotFoundException;
import neatlogic.framework.matrix.exception.MatrixNotFoundException;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.auth.label.MATRIX_MODIFY;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * @program: neatlogic
 * @description:
 * @create: 2020-03-30 16:29
 **/
@Service
@Transactional
@AuthAction(action = MATRIX_MODIFY.class)
@OperationType(type = OperationTypeEnum.DELETE)
public class MatrixDataDeleteApi extends PrivateApiComponentBase {

    @Resource
    private MatrixMapper matrixMapper;

    @Override
    public String getToken() {
        return "matrix/data/delete";
    }

    @Override
    public String getName() {
        return "矩阵数据删除接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "matrixUuid", desc = "矩阵uuid", type = ApiParamType.STRING, isRequired = true),
            @Param(name = "uuidList", desc = "矩阵数据uuid列表", type = ApiParamType.JSONARRAY, isRequired = true)})
    @Description(desc = "矩阵数据删除接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String matrixUuid = jsonObj.getString("matrixUuid");
        MatrixVo matrixVo = matrixMapper.getMatrixByUuid(matrixUuid);
        if (matrixVo == null) {
            throw new MatrixNotFoundException(matrixUuid);
        }
        JSONArray uuidArray = jsonObj.getJSONArray("uuidList");
        if (CollectionUtils.isEmpty(uuidArray)) {
            throw new ParamNotExistsException("uuidList");
        }
        List<String> uuidList = uuidArray.toJavaList(String.class);
        IMatrixDataSourceHandler matrixDataSourceHandler = MatrixDataSourceHandlerFactory.getHandler(matrixVo.getType());
        if (matrixDataSourceHandler == null) {
            throw new MatrixDataSourceHandlerNotFoundException(matrixVo.getType());
        }
        matrixDataSourceHandler.deleteTableRowData(matrixUuid, uuidList);
//        if (MatrixType.CUSTOM.getValue().equals(matrixVo.getType())) {
//            List<String> uuidList = jsonObj.getJSONArray("uuidList").toJavaList(String.class);
//            for (String uuid : uuidList) {
//                dataMapper.deleteDynamicTableDataByUuid(matrixUuid, uuid, TenantContext.get().getDataDbName());
//            }
//        } else if (MatrixType.EXTERNAL.getValue().equals(matrixVo.getType())) {
//            throw new MatrixExternalDeleteDataException();
//        } else if (MatrixType.VIEW.getValue().equals(matrixVo.getType())) {
//            throw new MatrixViewDeleteDataException();
//        }

        return null;
    }
}

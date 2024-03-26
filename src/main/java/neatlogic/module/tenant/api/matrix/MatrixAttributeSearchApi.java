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

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dto.FieldValidResultVo;
import neatlogic.framework.exception.type.ParamNotExistsException;
import neatlogic.framework.matrix.constvalue.MatrixType;
import neatlogic.framework.matrix.core.IMatrixDataSourceHandler;
import neatlogic.framework.matrix.core.MatrixDataSourceHandlerFactory;
import neatlogic.framework.matrix.core.MatrixPrivateDataSourceHandlerFactory;
import neatlogic.framework.matrix.dao.mapper.*;
import neatlogic.framework.matrix.dto.MatrixAttributeVo;
import neatlogic.framework.matrix.dto.MatrixExternalVo;
import neatlogic.framework.matrix.dto.MatrixVo;
import neatlogic.framework.matrix.exception.MatrixDataSourceHandlerNotFoundException;
import neatlogic.framework.matrix.exception.MatrixExternalNotFoundException;
import neatlogic.framework.matrix.exception.MatrixNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.IValid;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @program: neatlogic
 * @description:
 * @create: 2020-03-26 19:06
 **/
@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class MatrixAttributeSearchApi extends PrivateApiComponentBase {

    @Resource
    private MatrixMapper matrixMapper;

    @Override
    public String getToken() {
        return "matrix/attribute/search";
    }

    @Override
    public String getName() {
        return "nmtam.matrixattributesearchapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Override
    public boolean disableReturnCircularReferenceDetect() {
        return true;
    }
    @Input({
            @Param(name = "matrixUuid", desc = "term.framework.matrixuuid", type = ApiParamType.STRING),
            @Param(name = "type", desc = "common.type", type = ApiParamType.ENUM, rule = "custom,external,view,cmdbci,private,cmdbcustomview"),
            @Param(name = "ciId", type = ApiParamType.LONG, desc = "term.cmdb.ciid")
    })
    @Output({
            @Param(name = "tbodyList", desc = "common.tbodylist", explode = MatrixAttributeVo[].class),
            @Param(name = "type", desc = "common.type", type = ApiParamType.ENUM, rule = "custom,external,view,cmdbci,private,cmdbcustomview")
    })
    @Description(desc = "nmtam.matrixattributesearchapi.getname")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject resultObj = new JSONObject();
        MatrixVo matrixVo = null;
        String matrixUuid = jsonObj.getString("matrixUuid");
        if (StringUtils.isNotBlank(matrixUuid)) {
            matrixVo = MatrixPrivateDataSourceHandlerFactory.getMatrixVo(matrixUuid);
            if (matrixVo == null) {
                matrixVo = matrixMapper.getMatrixByUuid(matrixUuid);
                if (matrixVo == null) {
                    throw new MatrixNotFoundException(matrixUuid);
                }
            }
        } else {
            String type = jsonObj.getString("type");
            if (StringUtils.isBlank(type)) {
                throw new ParamNotExistsException("matrixUuid", "type");
            }
            matrixVo = jsonObj.toJavaObject(MatrixVo.class);
        }
        String type = matrixVo.getType();
        resultObj.put("type", type);
        IMatrixDataSourceHandler matrixDataSourceHandler = MatrixDataSourceHandlerFactory.getHandler(type);
        if (matrixDataSourceHandler == null) {
            throw new MatrixDataSourceHandlerNotFoundException(type);
        }
        List<MatrixAttributeVo> matrixAttributeList = matrixDataSourceHandler.getAttributeList(matrixVo);
        resultObj.put("tbodyList", matrixAttributeList);
        return resultObj;
    }

    /**
     * 校验矩阵的外部数据源是否存在
     **/
    public IValid matrixUuid() {
        return value -> {
            String matrixUuid = value.getString("matrixUuid");
            MatrixVo matrixVo = MatrixPrivateDataSourceHandlerFactory.getMatrixVo(matrixUuid);
            if (matrixVo == null) {
                matrixVo = matrixMapper.getMatrixByUuid(matrixUuid);
            }
            if (MatrixType.EXTERNAL.getValue().equals(matrixVo.getType())) {
                MatrixExternalVo externalVo = matrixMapper.getMatrixExternalByMatrixUuid(matrixUuid);
                if (externalVo == null) {
                    return new FieldValidResultVo(new MatrixExternalNotFoundException(matrixVo.getName()));
                }
            }
            return new FieldValidResultVo();
        };
    }
}

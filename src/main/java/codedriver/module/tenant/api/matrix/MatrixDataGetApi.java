/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.matrix;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.matrix.core.IMatrixDataSourceHandler;
import codedriver.framework.matrix.core.MatrixDataSourceHandlerFactory;
import codedriver.framework.matrix.dao.mapper.MatrixMapper;
import codedriver.framework.matrix.dto.MatrixDataVo;
import codedriver.framework.matrix.dto.MatrixVo;
import codedriver.framework.matrix.exception.MatrixDataSourceHandlerNotFoundException;
import codedriver.framework.matrix.exception.MatrixNotFoundException;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class MatrixDataGetApi extends PrivateApiComponentBase {

    @Resource
    private MatrixMapper matrixMapper;

    @Override
    public String getToken() {
        return "matrix/data/get";
    }

    @Override
    public String getName() {
        return "矩阵一行数据获取接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "matrixUuid", type = ApiParamType.STRING, isRequired = true, desc = "矩阵uuid"),
            @Param(name = "uuid", type = ApiParamType.STRING, isRequired = true, desc = "行uuid")
    })
    @Output({
            @Param(name = "Return", type = ApiParamType.JSONOBJECT, desc = "一行数据")
    })
    @Description(desc = "矩阵一行数据获取接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        MatrixDataVo dataVo = JSONObject.toJavaObject(jsonObj, MatrixDataVo.class);
        MatrixVo matrixVo = matrixMapper.getMatrixByUuid(dataVo.getMatrixUuid());
        if (matrixVo == null) {
            throw new MatrixNotFoundException(dataVo.getMatrixUuid());
        }
        IMatrixDataSourceHandler matrixDataSourceHandler = MatrixDataSourceHandlerFactory.getHandler(matrixVo.getType());
        if (matrixDataSourceHandler == null) {
            throw new MatrixDataSourceHandlerNotFoundException(matrixVo.getType());
        }
        return matrixDataSourceHandler.getTableRowData(dataVo);
//        if (MatrixType.CUSTOM.getValue().equals(matrixVo.getType())) {
//            List<MatrixAttributeVo> attributeVoList = attributeMapper.getMatrixAttributeByMatrixUuid(dataVo.getMatrixUuid());
//            if (CollectionUtils.isNotEmpty(attributeVoList)) {
//                List<String> columnList = attributeVoList.stream().map(MatrixAttributeVo::getUuid).collect(Collectors.toList());
//                dataVo.setColumnList(columnList);
//                Map<String, String> rowData = matrixDataMapper.getDynamicRowDataByUuid(dataVo);
//                for (MatrixAttributeVo attributeVo : attributeVoList) {
//                    if (MatrixAttributeType.USER.getValue().equals(attributeVo.getType())) {
//                        String value = rowData.get(attributeVo.getUuid());
//                        if (value != null) {
//                            value = GroupSearch.USER.getValuePlugin() + value;
//                            rowData.put(attributeVo.getUuid(), value);
//                        }
//                    } else if (MatrixAttributeType.TEAM.getValue().equals(attributeVo.getType())) {
//                        String value = rowData.get(attributeVo.getUuid());
//                        if (value != null) {
//                            value = GroupSearch.TEAM.getValuePlugin() + value;
//                            rowData.put(attributeVo.getUuid(), value);
//                        }
//                    } else if (MatrixAttributeType.ROLE.getValue().equals(attributeVo.getType())) {
//                        String value = rowData.get(attributeVo.getUuid());
//                        if (value != null) {
//                            value = GroupSearch.ROLE.getValuePlugin() + value;
//                            rowData.put(attributeVo.getUuid(), value);
//                        }
//                    }
//                }
//                return rowData;
//            }
//        } else if (MatrixType.EXTERNAL.getValue().equals(matrixVo.getType())) {
//            throw new MatrixExternalEditRowDataException();
//        } else if (MatrixType.VIEW.getValue().equals(matrixVo.getType())) {
//            throw new MatrixViewEditRowDataException();
//        }


//        return null;
    }

}

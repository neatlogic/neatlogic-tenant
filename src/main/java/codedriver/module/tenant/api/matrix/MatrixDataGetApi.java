package codedriver.module.tenant.api.matrix;

import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.constvalue.GroupSearch;
import codedriver.framework.matrix.constvalue.MatrixAttributeType;
import codedriver.framework.matrix.constvalue.MatrixType;
import codedriver.framework.matrix.dao.mapper.MatrixAttributeMapper;
import codedriver.framework.matrix.dao.mapper.MatrixDataMapper;
import codedriver.framework.matrix.dao.mapper.MatrixMapper;
import codedriver.framework.matrix.dto.MatrixAttributeVo;
import codedriver.framework.matrix.dto.MatrixDataVo;
import codedriver.framework.matrix.dto.MatrixVo;
import codedriver.framework.matrix.exception.MatrixExternalException;
import codedriver.framework.matrix.exception.MatrixNotFoundException;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class MatrixDataGetApi extends PrivateApiComponentBase {

    @Resource
    private MatrixMapper matrixMapper;

    @Resource
    private MatrixDataMapper matrixDataMapper;

    @Resource
    private MatrixAttributeMapper attributeMapper;

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
        MatrixDataVo dataVo = JSON.toJavaObject(jsonObj, MatrixDataVo.class);
        MatrixVo matrixVo = matrixMapper.getMatrixByUuid(dataVo.getMatrixUuid());
        if (matrixVo == null) {
            throw new MatrixNotFoundException(dataVo.getMatrixUuid());
        }
        if (MatrixType.CUSTOM.getValue().equals(matrixVo.getType())) {
            List<MatrixAttributeVo> attributeVoList = attributeMapper.getMatrixAttributeByMatrixUuid(dataVo.getMatrixUuid());
            if (CollectionUtils.isNotEmpty(attributeVoList)) {
                List<String> columnList = new ArrayList<>();
                for (MatrixAttributeVo attributeVo : attributeVoList) {
                    columnList.add(attributeVo.getUuid());
                }
                dataVo.setColumnList(columnList);
                Map<String, String> rowData = matrixDataMapper.getDynamicRowDataByUuid(dataVo, TenantContext.get().getTenantUuid());
                for (MatrixAttributeVo attributeVo : attributeVoList) {
                    if (MatrixAttributeType.USER.getValue().equals(attributeVo.getType())) {
                        String value = rowData.get(attributeVo.getUuid());
                        if (value != null) {
                            value = GroupSearch.USER.getValuePlugin() + value;
                            rowData.put(attributeVo.getUuid(), value);
                        }
                    } else if (MatrixAttributeType.TEAM.getValue().equals(attributeVo.getType())) {
                        String value = rowData.get(attributeVo.getUuid());
                        if (value != null) {
                            value = GroupSearch.TEAM.getValuePlugin() + value;
                            rowData.put(attributeVo.getUuid(), value);
                        }
                    } else if (MatrixAttributeType.ROLE.getValue().equals(attributeVo.getType())) {
                        String value = rowData.get(attributeVo.getUuid());
                        if (value != null) {
                            value = GroupSearch.ROLE.getValuePlugin() + value;
                            rowData.put(attributeVo.getUuid(), value);
                        }
                    }
                }
                return rowData;
            }
        } else {
            throw new MatrixExternalException("矩阵外部数据源没有获取一行数据操作");
        }


        return null;
    }

}

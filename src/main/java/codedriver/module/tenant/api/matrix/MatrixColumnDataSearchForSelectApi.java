package codedriver.module.tenant.api.matrix;

import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.matrix.constvalue.MatrixType;
import codedriver.framework.matrix.dao.mapper.MatrixDataMapper;
import codedriver.framework.matrix.dao.mapper.MatrixMapper;
import codedriver.framework.matrix.dto.MatrixColumnVo;
import codedriver.framework.matrix.dto.MatrixDataVo;
import codedriver.framework.matrix.dto.MatrixVo;
import codedriver.framework.matrix.exception.MatrixNotFoundException;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Deprecated
@OperationType(type = OperationTypeEnum.SEARCH)
public class MatrixColumnDataSearchForSelectApi extends PrivateApiComponentBase {

    @Resource
    private MatrixMapper matrixMapper;

    @Resource
    private MatrixDataMapper matrixDataMapper;

    @Override
    public String getToken() {
        return "matrix/column/data/search/forselect";
    }

    @Override
    public String getName() {
        return "矩阵属性数据查询-下拉级联接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "matrixUuid", desc = "矩阵Uuid", type = ApiParamType.STRING, isRequired = true),
            @Param(name = "value", desc = "作为值的目标属性", type = ApiParamType.STRING, isRequired = true),
            @Param(name = "text", desc = "作为显示文字的目标属性", type = ApiParamType.STRING, isRequired = true),
            @Param(name = "sourceColumnList", desc = "源属性集合", type = ApiParamType.JSONARRAY)})
    @Description(desc = "矩阵属性数据查询-下拉级联接口")
    @Output({@Param(name = "columnDataList", type = ApiParamType.JSONARRAY, desc = "属性数据集合")})
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject returnObj = new JSONObject();
        String matrixUuid = jsonObj.getString("matrixUuid");
        MatrixVo matrixVo = matrixMapper.getMatrixByUuid(matrixUuid);
        if (matrixVo == null) {
            throw new MatrixNotFoundException(matrixUuid);
        }
        String value = jsonObj.getString("value");
        String text = jsonObj.getString("text");
        List<String> valueList = new ArrayList<>();
        List<MatrixColumnVo> sourceColumnList = JSON.parseArray(jsonObj.getString("sourceColumnList"), MatrixColumnVo.class);
        MatrixDataVo dataVo = new MatrixDataVo();
        dataVo.setSourceColumnList(sourceColumnList);
        List<String> targetColumnList = new ArrayList<>();
        targetColumnList.add(value);
        targetColumnList.add(text);
        dataVo.setColumnList(targetColumnList);
        dataVo.setMatrixUuid(matrixUuid);
        if (matrixVo.getType().equals(MatrixType.CUSTOM.getValue())) {
            List<Map<String, String>> dataMapList = matrixDataMapper.getDynamicTableDataByColumnList(dataVo, TenantContext.get().getTenantUuid());
            for (Map<String, String> dataMap : dataMapList) {
                String valueTmp = dataMap.get(value);
                if (valueList.contains(valueTmp)) {
                    returnObj.put("isRepeat", true);
                    return returnObj;
                } else {
                    valueList.add(valueTmp);
                }
            }
            returnObj.put("isRepeat", false);
            returnObj.put("columnDataList", dataMapList);
        } else {
            //TODO 外部数据源矩阵  暂未实现
            return null;
        }
        return returnObj;
    }
}

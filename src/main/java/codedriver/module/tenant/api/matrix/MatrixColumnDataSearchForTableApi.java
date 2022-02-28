/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.matrix;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.exception.type.ParamIrregularException;
import codedriver.framework.matrix.core.IMatrixDataSourceHandler;
import codedriver.framework.matrix.core.MatrixDataSourceHandlerFactory;
import codedriver.framework.matrix.dao.mapper.MatrixMapper;
import codedriver.framework.matrix.dto.MatrixAttributeVo;
import codedriver.framework.matrix.dto.MatrixDataVo;
import codedriver.framework.matrix.dto.MatrixVo;
import codedriver.framework.matrix.exception.MatrixAttributeNotFoundException;
import codedriver.framework.matrix.exception.MatrixDataSourceHandlerNotFoundException;
import codedriver.framework.matrix.exception.MatrixNotFoundException;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class MatrixColumnDataSearchForTableApi extends PrivateApiComponentBase {

    @Resource
    private MatrixMapper matrixMapper;

    @Override
    public String getToken() {
        return "matrix/column/data/search/fortable";
    }

    @Override
    public String getName() {
        return "矩阵属性数据查询-table接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "matrixUuid", desc = "矩阵Uuid", type = ApiParamType.STRING, isRequired = true),
            @Param(name = "defaultValue", desc = "需要回显的数据uuid集合", type = ApiParamType.JSONARRAY),
            @Param(name = "uuidColumn", desc = "uuid对应的属性", type = ApiParamType.STRING),
            @Param(name = "columnList", desc = "目标属性集合，数据按这个字段顺序返回", type = ApiParamType.JSONARRAY, isRequired = true),
            @Param(name = "searchColumnList ", desc = "搜索属性集合", type = ApiParamType.JSONARRAY),
            @Param(name = "sourceColumnList", desc = "搜索过滤值集合", type = ApiParamType.JSONARRAY),//TODO 前端传入是List<String>，而MatrixDataVo对象中sourceColumnList是List<MatrixColumnVo>
            @Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否需要分页，默认true"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页条目"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页"),
            @Param(name = "arrayColumnList", desc = "需要将值转化成数组的属性集合", type = ApiParamType.JSONARRAY),
            @Param(name = "filterList", desc = "根据列头uuid,搜索具体的列值，支持多个列分别搜索，注意仅支持静态列表  [{uuid:***,valueList:[]},{uuid:***,valueList:[]}]", type = ApiParamType.JSONARRAY)
    })
    @Description(desc = "矩阵属性数据查询-table接口")
    @Output({
            @Param(name = "tbodyList", type = ApiParamType.JSONARRAY, desc = "属性数据集合"),
            @Param(name = "theadList", type = ApiParamType.JSONARRAY, desc = "属性列名集合"),
            @Param(name = "searchColumnDetailList", type = ApiParamType.JSONARRAY, desc = "搜索属性详情集合"),
            @Param(name = "type", type = ApiParamType.STRING, desc = "矩阵类型"),
            @Param(explode = BasePageVo.class)
    })
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        MatrixDataVo dataVo = jsonObj.toJavaObject(MatrixDataVo.class);
        List<String> columnList = dataVo.getColumnList();
        if (CollectionUtils.isEmpty(columnList)) {
            throw new ParamIrregularException("columnList");
        }
        MatrixVo matrixVo = matrixMapper.getMatrixByUuid(dataVo.getMatrixUuid());
        if (matrixVo == null) {
            throw new MatrixNotFoundException(dataVo.getMatrixUuid());
        }
        JSONArray searchColumnArray = jsonObj.getJSONArray("searchColumnList");
        JSONArray dafaultValue = dataVo.getDefaultValue();
        if (CollectionUtils.isEmpty(dafaultValue)) {
            JSONArray uuidList = jsonObj.getJSONArray("uuidList");
            dataVo.setDefaultValue(uuidList);
        }
        String type = matrixVo.getType();
        IMatrixDataSourceHandler matrixDataSourceHandler = MatrixDataSourceHandlerFactory.getHandler(type);
        if (matrixDataSourceHandler == null) {
            throw new MatrixDataSourceHandlerNotFoundException(type);
        }
        JSONObject returnObj = matrixDataSourceHandler.searchTableData(dataVo);
        List<MatrixAttributeVo> matrixAttributeList = matrixDataSourceHandler.getAttributeList(matrixVo);
        returnObj.put("searchColumnDetailList", getSearchColumnDetailList(dataVo.getMatrixUuid(), matrixAttributeList, searchColumnArray));
        returnObj.put("type", type);
        return returnObj;
    }

    private List<MatrixAttributeVo> getSearchColumnDetailList(String matrixUuid, List<MatrixAttributeVo> attributeList, JSONArray searchColumnArray) {
        if (CollectionUtils.isNotEmpty(searchColumnArray)) {
            Map<String, MatrixAttributeVo> attributeMap = new HashMap<>();
            for (MatrixAttributeVo attribute : attributeList) {
                attributeMap.put(attribute.getUuid(), attribute);
            }
            List<MatrixAttributeVo> searchColumnDetailList = new ArrayList<>();
            List<String> searchColumnList = searchColumnArray.toJavaList(String.class);
            for (String column : searchColumnList) {
                MatrixAttributeVo attribute = attributeMap.get(column);
                if (attribute == null) {
                    throw new MatrixAttributeNotFoundException(matrixUuid, column);
                }
                searchColumnDetailList.add(attribute);
            }
            return searchColumnDetailList;
        }
        return null;
    }
}

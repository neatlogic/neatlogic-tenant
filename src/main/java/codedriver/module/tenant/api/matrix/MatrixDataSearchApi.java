/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.matrix;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.dependency.constvalue.FrameworkFromType;
import codedriver.framework.dependency.core.DependencyManager;
import codedriver.framework.matrix.core.IMatrixDataSourceHandler;
import codedriver.framework.matrix.core.MatrixDataSourceHandlerFactory;
import codedriver.framework.matrix.core.MatrixPrivateDataSourceHandlerFactory;
import codedriver.framework.matrix.dao.mapper.*;
import codedriver.framework.matrix.dto.*;
import codedriver.framework.matrix.exception.*;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.util.TableResultUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @program: codedriver
 * @description:
 * @create: 2020-03-30 16:34
 **/
@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class MatrixDataSearchApi extends PrivateApiComponentBase {

    @Resource
    private MatrixMapper matrixMapper;

    @Override
    public String getToken() {
        return "matrix/data/search";
    }

    @Override
    public String getName() {
        return "矩阵数据检索接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
//            @Param(name = "keyword", desc = "关键字", type = ApiParamType.STRING), 页面上没有搜索框，所以后端不需要支持关键字搜索
            @Param(name = "matrixUuid", desc = "矩阵uuid", type = ApiParamType.STRING, isRequired = true),
            @Param(name = "needPage", desc = "是否分页", type = ApiParamType.BOOLEAN),
            @Param(name = "pageSize", desc = "显示条目数", type = ApiParamType.INTEGER),
            @Param(name = "currentPage", desc = "当前页", type = ApiParamType.INTEGER)
    })
    @Output({
            @Param(name = "tbodyList", type = ApiParamType.JSONARRAY, desc = "矩阵数据集合"),
            @Param(name = "theadList", type = ApiParamType.JSONARRAY, desc = "矩阵属性集合"),
            @Param(name = "referenceCount", type = ApiParamType.INTEGER, desc = "被引用次数"),
            @Param(explode = BasePageVo.class)
    })
    @Description(desc = "矩阵数据检索接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        MatrixDataVo dataVo = JSONObject.toJavaObject(jsonObj, MatrixDataVo.class);
        MatrixVo matrixVo = MatrixPrivateDataSourceHandlerFactory.getMatrixVo(dataVo.getMatrixUuid());
        if (matrixVo == null) {
            matrixVo = matrixMapper.getMatrixByUuid(dataVo.getMatrixUuid());
            if (matrixVo == null) {
                throw new MatrixNotFoundException(dataVo.getMatrixUuid());
            }
        }
        IMatrixDataSourceHandler matrixDataSourceHandler = MatrixDataSourceHandlerFactory.getHandler(matrixVo.getType());
        if (matrixDataSourceHandler == null) {
            throw new MatrixDataSourceHandlerNotFoundException(matrixVo.getType());
        }
        List<MatrixAttributeVo> matrixAttributeList = matrixDataSourceHandler.getAttributeList(matrixVo);
        List<String> columnList = matrixAttributeList.stream().map(MatrixAttributeVo::getUuid).collect(Collectors.toList());
        dataVo.setColumnList(columnList);
        List<Map<String, JSONObject>> tbodyList = matrixDataSourceHandler.searchTableDataNew(dataVo);
        JSONArray theadList = getTheadList(matrixAttributeList);
        JSONObject returnObj = TableResultUtil.getResult(theadList, tbodyList, dataVo);
        int count = DependencyManager.getDependencyCount(FrameworkFromType.MATRIX, dataVo.getMatrixUuid());
        returnObj.put("referenceCount", count);
        return returnObj;
    }

    private JSONArray getTheadList(List<MatrixAttributeVo> attributeList) {
        JSONArray theadList = new JSONArray();
        JSONObject selectionObj = new JSONObject();
        selectionObj.put("key", "selection");
        selectionObj.put("width", 60);
        theadList.add(selectionObj);
        for (MatrixAttributeVo attributeVo : attributeList) {
            JSONObject columnObj = new JSONObject();
            columnObj.put("title", attributeVo.getName());
            columnObj.put("key", attributeVo.getUuid());
            theadList.add(columnObj);
        }
        JSONObject actionObj = new JSONObject();
        actionObj.put("title", "");
        actionObj.put("key", "action");
        actionObj.put("align", "right");
        actionObj.put("width", 10);
        theadList.add(actionObj);
        return theadList;
    }
}

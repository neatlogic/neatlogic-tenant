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

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.dependency.constvalue.FrameworkFromType;
import neatlogic.framework.dependency.core.DependencyManager;
import neatlogic.framework.matrix.core.IMatrixDataSourceHandler;
import neatlogic.framework.matrix.core.MatrixDataSourceHandlerFactory;
import neatlogic.framework.matrix.core.MatrixPrivateDataSourceHandlerFactory;
import neatlogic.framework.matrix.dao.mapper.*;
import neatlogic.framework.matrix.dto.*;
import neatlogic.framework.matrix.exception.*;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.TableResultUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @program: neatlogic
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

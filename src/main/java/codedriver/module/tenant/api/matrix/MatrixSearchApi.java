/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.matrix;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.dependency.constvalue.CalleeType;
import codedriver.framework.dependency.core.DependencyManager;
import codedriver.framework.matrix.dao.mapper.MatrixMapper;
import codedriver.framework.matrix.dto.MatrixVo;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.util.TableResultUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @program: codedriver
 * @description:
 * @create: 2020-03-26 19:06
 **/
@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class MatrixSearchApi extends PrivateApiComponentBase {

    @Resource
    private MatrixMapper matrixMapper;

    @Override
    public String getToken() {
        return "matrix/search";
    }

    @Override
    public String getName() {
        return "数据源矩阵检索";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "keyword", desc = "关键字", type = ApiParamType.STRING),
            @Param(name = "type", desc = "类型", type = ApiParamType.ENUM, rule = "custom,external,view,cmdbci"),
            @Param(name = "currentPage", desc = "当前页码", type = ApiParamType.INTEGER),
            @Param(name = "needPage", desc = "是否分页", type = ApiParamType.BOOLEAN),
            @Param(name = "pageSize", desc = "页面展示数", type = ApiParamType.INTEGER),
            @Param(name = "defaultValue", desc = "精确匹配回显数据参数", type = ApiParamType.JSONARRAY)
    })
    @Output({
            @Param(name = "tbodyList", desc = "矩阵数据源列表", explode = MatrixVo[].class),
            @Param(explode = BasePageVo.class)
    })
    @Description(desc = "数据源矩阵检索")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        MatrixVo matrix = JSONObject.toJavaObject(jsonObj, MatrixVo.class);
        JSONArray defaultValue = matrix.getDefaultValue();
        if (CollectionUtils.isNotEmpty(defaultValue)) {
            List<String> uuidList = defaultValue.toJavaList(String.class);
            List<MatrixVo> tbodyList = matrixMapper.getMatrixListByUuidList(uuidList);
            JSONObject returnObj = new JSONObject();
            returnObj.put("tbodyList", tbodyList);
            return returnObj;
        }
        if (matrix.getNeedPage()) {
            int rowNum = matrixMapper.searchMatrixCount(matrix);
            matrix.setRowNum(rowNum);
        }
        List<MatrixVo> tbodyList = matrixMapper.searchMatrix(matrix);
        for (MatrixVo matrixVo : tbodyList) {
            int count = DependencyManager.getDependencyCount(CalleeType.MATRIX, matrixVo.getUuid());
            matrixVo.setReferenceCount(count);
        }
        return TableResultUtil.getResult(tbodyList, matrix);
    }
}

/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.matrix;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.matrix.dao.mapper.MatrixMapper;
import codedriver.framework.matrix.dto.MatrixVo;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @program: codedriver
 * @description:
 * @create: 2020-03-26 19:06
 **/
@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class MatrixSearchForSelectApi extends PrivateApiComponentBase {

    @Resource
    private MatrixMapper matrixMapper;

    @Override
    public String getToken() {
        return "matrix/search/forselect";
    }

    @Override
    public String getName() {
        return "查询数据源矩阵_下拉框";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "keyword", desc = "关键字", type = ApiParamType.STRING),
            @Param(name = "type", desc = "类型", type = ApiParamType.ENUM, rule = "custom,external"),
            @Param(name = "currentPage", desc = "当前页码", type = ApiParamType.INTEGER),
            @Param(name = "needPage", desc = "是否分页", type = ApiParamType.BOOLEAN),
            @Param(name = "pageSize", desc = "页面展示数", type = ApiParamType.INTEGER),
            @Param(name = "defaultValue", desc = "精确匹配回显数据参数", type = ApiParamType.JSONARRAY)
    })
    @Output({
            @Param(name = "list", desc = "矩阵数据源列表", explode = ValueTextVo[].class)
    })
    @Description(desc = "查询数据源矩阵_下拉框")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject returnObj = new JSONObject();
        MatrixVo matrix = JSON.toJavaObject(jsonObj, MatrixVo.class);
        JSONArray defaultValue = matrix.getDefaultValue();
        if (CollectionUtils.isNotEmpty(defaultValue)) {
            List<ValueTextVo> tbodyList = new ArrayList<>();
            for (int i = 0; i < defaultValue.size(); i++) {
                String uuid = defaultValue.getString(i);
                ValueTextVo processMatrixVo = matrixMapper.getMatrixByUuidForSelect(uuid);
                if (processMatrixVo != null) {
                    tbodyList.add(processMatrixVo);
                }
            }
            returnObj.put("list", tbodyList);
        } else {
            if (matrix.getNeedPage()) {
                int rowNum = matrixMapper.searchMatrixCount(matrix);
                matrix.setPageCount(PageUtil.getPageCount(rowNum, matrix.getPageSize()));
                returnObj.put("pageCount", matrix.getPageCount());
                returnObj.put("rowNum", rowNum);
                returnObj.put("pageSize", matrix.getPageSize());
                returnObj.put("currentPage", matrix.getCurrentPage());
            }
            returnObj.put("list", matrixMapper.searchMatrixForSelect(matrix));
        }
        return returnObj;
    }
}

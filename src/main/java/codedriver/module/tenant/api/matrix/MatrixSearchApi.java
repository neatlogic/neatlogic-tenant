/*
 * Copyright(c) 2022 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.matrix;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.dependency.constvalue.FrameworkFromType;
import codedriver.framework.dependency.core.DependencyManager;
import codedriver.framework.matrix.core.MatrixPrivateDataSourceHandlerFactory;
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
import java.util.ArrayList;
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
            @Param(name = "type", desc = "类型", type = ApiParamType.ENUM, rule = "custom,external,view,cmdbci,private"),
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
        MatrixVo searchVo = JSONObject.toJavaObject(jsonObj, MatrixVo.class);
        JSONArray defaultValue = searchVo.getDefaultValue();
        if (CollectionUtils.isNotEmpty(defaultValue)) {
            List<MatrixVo> resultList = new ArrayList<>();
            List<String> uuidList = new ArrayList<>();
            List<String> defaultValueList = defaultValue.toJavaList(String.class);
            for (String uuid : defaultValueList) {
                MatrixVo matrixVo = MatrixPrivateDataSourceHandlerFactory.getMatrixVo(uuid);
                if (matrixVo != null) {
                    resultList.add(matrixVo);
                } else {
                    uuidList.add(uuid);
                }
            }
            if (CollectionUtils.isNotEmpty(uuidList)) {
                List<MatrixVo> matrixList = matrixMapper.getMatrixListByUuidList(uuidList);
                resultList.addAll(matrixList);
            }
            //排序
            List<MatrixVo> tbodyList = new ArrayList<>();
            for (String uuid : defaultValueList) {
                for (MatrixVo matrixVo : resultList) {
                    if (uuid.equals(matrixVo.getUuid())) {
                        tbodyList.add(matrixVo);
                    }
                }
            }
            JSONObject returnObj = new JSONObject();
            returnObj.put("tbodyList", tbodyList);
            return returnObj;
        }

        int privateCount = MatrixPrivateDataSourceHandlerFactory.getCount(searchVo);
        int dynamicCount = matrixMapper.searchMatrixCount(searchVo);
        int rowNum = dynamicCount + privateCount;
        searchVo.setRowNum(rowNum);
        int currentPage = searchVo.getCurrentPage();
        int pageCount = searchVo.getPageCount();
        if (currentPage > pageCount) {
            return TableResultUtil.getResult(new ArrayList<>(), searchVo);
        }
        int pageSize = searchVo.getPageSize();
        List<MatrixVo> tbodyList = new ArrayList<>();
        int fromIndex = searchVo.getStartNum();
        int toIndex = fromIndex + searchVo.getPageSize();
        toIndex = Math.min(toIndex, rowNum);
        if (fromIndex < privateCount && toIndex <= privateCount) {
            //当前页数据全部在内存中
            tbodyList = MatrixPrivateDataSourceHandlerFactory.getList(searchVo);
        } else if (fromIndex < privateCount && toIndex > privateCount) {
            //当前页数据有部分在内存中，有部分在数据库中
            List<MatrixVo> privateList = MatrixPrivateDataSourceHandlerFactory.getList(searchVo);
            tbodyList.addAll(privateList);
            List<MatrixVo> dynamicList = matrixMapper.searchMatrix(searchVo.getKeyword(), searchVo.getType(), 0, pageSize - privateList.size());
            tbodyList.addAll(dynamicList);
        } else {
            //当前页数据全部在数据库中
            tbodyList = matrixMapper.searchMatrix(searchVo.getKeyword(), searchVo.getType(), fromIndex - privateCount, pageSize);
        }
        for (MatrixVo matrixVo : tbodyList) {
            int referenceCount = DependencyManager.getDependencyCount(FrameworkFromType.MATRIX, matrixVo.getUuid());
            matrixVo.setReferenceCount(referenceCount);
        }
        return TableResultUtil.getResult(tbodyList, searchVo);
    }
}

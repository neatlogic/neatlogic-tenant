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
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.dependency.constvalue.FrameworkFromType;
import neatlogic.framework.dependency.core.DependencyManager;
import neatlogic.framework.matrix.core.MatrixPrivateDataSourceHandlerFactory;
import neatlogic.framework.matrix.dao.mapper.MatrixMapper;
import neatlogic.framework.matrix.dto.MatrixVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.TableResultUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @program: neatlogic
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
        return "nmtam.matrixsearchapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "keyword", desc = "common.keyword", type = ApiParamType.STRING),
            @Param(name = "type", desc = "common.type", type = ApiParamType.ENUM, rule = "custom,external,view,cmdbci,private,cmdbcustomview"),
            @Param(name = "currentPage", desc = "common.currentpage", type = ApiParamType.INTEGER),
            @Param(name = "needPage", desc = "common.needpage", type = ApiParamType.BOOLEAN),
            @Param(name = "pageSize", desc = "common.pagesize", type = ApiParamType.INTEGER),
            @Param(name = "defaultValue", desc = "common.defaultvalue", type = ApiParamType.JSONARRAY)
    })
    @Output({
            @Param(name = "tbodyList", desc = "common.tbodylist", explode = MatrixVo[].class),
            @Param(explode = BasePageVo.class)
    })
    @Description(desc = "nmtam.matrixsearchapi.getname")
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

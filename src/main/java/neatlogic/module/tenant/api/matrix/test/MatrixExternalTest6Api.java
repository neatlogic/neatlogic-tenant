/*Copyright (C) $today.year  深圳极向量科技有限公司 All Rights Reserved.

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

package neatlogic.module.tenant.api.matrix.test;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.units.qual.A;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author linbq
 * @since 2021/8/5 16:44
 **/
@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class MatrixExternalTest6Api extends PrivateApiComponentBase {

    private static List<TbodyVo> staticTbodyList = new ArrayList<>();

    {
        for (int i = 0; i < 50; i++) {
            TbodyVo tbodyVo = new TbodyVo();
            tbodyVo.setColumn1("第一列" + i);
            tbodyVo.setColumn2("第二列" + i/2);
            tbodyVo.setColumn3("第三列" + i/3);
            tbodyVo.setColumn4("第四列" + i/4);
            List<String> columnList = new ArrayList<>();
            int columnIndex = i/5;
            columnList.add("第五列" + columnIndex++);
            columnList.add("第五列" + columnIndex++);
            columnList.add("第五列" + columnIndex++);
            tbodyVo.setColumn5(JSONObject.toJSONString(columnList));
            staticTbodyList.add(tbodyVo);
        }
    }
    @Override
    public String getToken() {
        return "matrix/external/test6";
    }

    @Override
    public String getName() {
        return "测试外部数据源矩阵查询";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "column1", type = ApiParamType.STRING, desc = "第一列"),
            @Param(name = "column2", type = ApiParamType.STRING, desc = "第二列"),
            @Param(name = "column3", type = ApiParamType.STRING, desc = "第三列"),
            @Param(name = "column4", type = ApiParamType.STRING, desc = "第四列"),
            @Param(name = "column5", type = ApiParamType.STRING, desc = "第五列"),
            @Param(name = "keyword", desc = "关键字", type = ApiParamType.STRING),
            @Param(name = "currentPage", desc = "当前页码", type = ApiParamType.INTEGER),
            @Param(name = "pageSize", desc = "页面展示数", type = ApiParamType.INTEGER),
            @Param(name = "needPage", desc = "是否分页", type = ApiParamType.BOOLEAN)
    })
    @Output({
            @Param(name = "tbodyList", explode = TbodyVo[].class, desc = "数据列表"),
            @Param(explode = BasePageVo.class)
    })
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        JSONObject resultObj = new JSONObject();
        List<TbodyVo> tbodyVoList = new ArrayList<>();
        String column1 = paramObj.getString("column1");
        String column2 = paramObj.getString("column2");
        String column3 = paramObj.getString("column3");
        String column4 = paramObj.getString("column4");
        String column5 = paramObj.getString("column5");
        BasePageVo basePageVo = JSONObject.toJavaObject(paramObj, BasePageVo.class);
        for (TbodyVo tbodyVo : staticTbodyList) {
            if (StringUtils.isNotBlank(column1)) {
                if (!Objects.equals(tbodyVo.getColumn1(), column1)) {
                    continue;
                }
            }
            if (StringUtils.isNotBlank(column2)) {
                if (!Objects.equals(tbodyVo.getColumn2(), column2)) {
                    continue;
                }
            }
            if (StringUtils.isNotBlank(column3)) {
                if (!Objects.equals(tbodyVo.getColumn3(), column3)) {
                    continue;
                }
            }
            if (StringUtils.isNotBlank(column4)) {
                if (!Objects.equals(tbodyVo.getColumn4(), column4)) {
                    continue;
                }
            }
            if (StringUtils.isNotBlank(column5)) {
                if (!Objects.equals(tbodyVo.getColumn5(), column5)) {
                    continue;
                }
            }
            tbodyVoList.add(tbodyVo);
        }
        Boolean needPage = paramObj.getBoolean("needPage");
        if (needPage == null || needPage){
            int rowNum = tbodyVoList.size();
            basePageVo.setRowNum(rowNum);
            int fromIndex = basePageVo.getStartNum();
            if(fromIndex < rowNum) {
                int toIndex = fromIndex + basePageVo.getPageSize();
                toIndex = toIndex > rowNum ? rowNum : toIndex;
                resultObj.put("tbodyList", tbodyVoList.subList(fromIndex, toIndex));
            }else{
                resultObj.put("tbodyList", new ArrayList<>());
            }
            resultObj.put("currentPage", basePageVo.getCurrentPage());
            resultObj.put("pageSize", basePageVo.getPageSize());
            resultObj.put("pageCount", basePageVo.getPageCount());
            resultObj.put("rowNum", basePageVo.getRowNum());
            resultObj.put("needPage", true);
        } else {
            resultObj.put("tbodyList", tbodyVoList);
            resultObj.put("needPage", false);
        }
        return resultObj;
    }
}

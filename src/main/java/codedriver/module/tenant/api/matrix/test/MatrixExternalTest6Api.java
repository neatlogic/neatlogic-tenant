/*
 * Copyright(c) 2021 TechSureCo.,Ltd.AllRightsReserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.matrix.test;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
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
        for (int i = 0; i < 1000; i++) {
            TbodyVo tbodyVo = new TbodyVo();
            tbodyVo.setColumn1("第一列" + i);
            tbodyVo.setColumn2("第二列" + i);
            tbodyVo.setColumn3("第三列" + i);
            tbodyVo.setColumn4("第四列" + i);
            tbodyVo.setColumn5("第五列" + i);
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
        return resultObj;
    }
}

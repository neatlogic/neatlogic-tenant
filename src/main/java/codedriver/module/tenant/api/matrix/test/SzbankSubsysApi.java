/*
 * Copyright(c) 2021 TechSureCo.,Ltd.AllRightsReserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.matrix.test;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.util.TableResultUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author linbq
 * @since 2021/9/16 11:22
 **/
@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class SzbankSubsysApi extends PrivateApiComponentBase {

    private static List<SubSysVo> staticSubSysList = new ArrayList<>();

    static {
        for (int i = 0; i < 100; i++) {
            SubSysVo subSysVo = new SubSysVo();
            subSysVo.setSubSysId(Long.valueOf(i));
            subSysVo.setSubSysName("子系统名_" + i);
            subSysVo.setSubSysDesc("子系统描述_" + i);
            subSysVo.setSysName("系统名_" + i/10);
            subSysVo.setRadio1(getDataObj("单选", i, 3, false));
            subSysVo.setCheckbox2(getDataObj("多选", i, 3, true));
            subSysVo.setSelect3(getDataObj("下拉单选", i, 3, false));
            subSysVo.setSelect4(getDataObj("下拉多选", i, 3, true));
            subSysVo.setText5(getDataObj("可编辑文本框", i));
            staticSubSysList.add(subSysVo);
        }
    }
    @Override
    public String getToken() {
        return "szbank/subsys";
    }

    @Override
    public String getName() {
        return "查询子系统数据接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "sysName", type = ApiParamType.STRING, desc = "系统名"),
            @Param(name = "subSysId", type = ApiParamType.LONG, desc = "子系统id"),
            @Param(name = "subSysName", type = ApiParamType.STRING, desc = "子系统名"),
            @Param(name = "subSysDesc", type = ApiParamType.STRING, desc = "子系统描述"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "页大小"),
            @Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否分页")
    })
    @Output({
            @Param(explode = SubSysVo[].class, desc = "子系统列表")
    })
    @Description(desc = "查询子系统数据接口")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        List<SubSysVo> resultList = new ArrayList<>();
        SubSysVo searchVo = JSONObject.toJavaObject(paramObj, SubSysVo.class);
        for (SubSysVo subSysVo : staticSubSysList) {
            if (searchVo.getSubSysId() != null) {
                if (!Objects.equals(searchVo.getSubSysId(), subSysVo.getSubSysId())) {
                    continue;
                }
            }
            if (StringUtils.isNotBlank(searchVo.getSubSysName())) {
                if (!Objects.equals(searchVo.getSubSysName(), subSysVo.getSubSysName())) {
                    continue;
                }
            }
            if (StringUtils.isNotBlank(searchVo.getSubSysDesc())) {
                if (!Objects.equals(searchVo.getSubSysDesc(), subSysVo.getSubSysDesc())) {
                    continue;
                }
            }
            if (StringUtils.isNotBlank(searchVo.getSysName())) {
                if (!Objects.equals(searchVo.getSysName(), subSysVo.getSysName())) {
                    continue;
                }
            }
            resultList.add(subSysVo);
        }
        int rowNum = resultList.size();
        if (rowNum > 0) {
            searchVo.setRowNum(rowNum);
            if (searchVo.getCurrentPage() <= searchVo.getPageCount()) {
                int fromIndex = searchVo.getStartNum();
                int toIndex = fromIndex + searchVo.getPageSize();
                toIndex = toIndex >  rowNum ? rowNum : toIndex;
                resultList =  resultList.subList(fromIndex, toIndex);
                return TableResultUtil.getResult(resultList, searchVo);
            }
        }
        return TableResultUtil.getResult(resultList, searchVo);
    }

    private static JSONObject getDataObj(String prefix, int startIndex) {
        JSONObject resultObj = new JSONObject();
        resultObj.put("value",prefix + startIndex);
        return resultObj;
    }
    private static JSONObject getDataObj(String prefix, int startIndex, int count, boolean multiple) {
        JSONObject resultObj = new JSONObject();
        List<ValueTextVo> dataList = new ArrayList<>();
        int rowNum = startIndex + count;
        for (; startIndex < rowNum; startIndex++) {
            dataList.add(new ValueTextVo(startIndex, prefix + startIndex));
        }
        resultObj.put("dataList", dataList);
        if (multiple) {
            JSONArray value = new JSONArray();
            value.add(startIndex);
            resultObj.put("value", value);
        } else {
            resultObj.put("value", startIndex);
        }
        return resultObj;
    }

    private static class SubSysVo extends BasePageVo {
        private Long subSysId;
        private String subSysName;
        private String subSysDesc;
        private String sysName;
        private JSONObject radio1;
        private JSONObject checkbox2;
        private JSONObject select3;
        private JSONObject select4;
        private JSONObject text5;
        public SubSysVo(){}

        public Long getSubSysId() {
            return subSysId;
        }

        public void setSubSysId(Long subSysId) {
            this.subSysId = subSysId;
        }

        public String getSubSysName() {
            return subSysName;
        }

        public void setSubSysName(String subSysName) {
            this.subSysName = subSysName;
        }

        public String getSubSysDesc() {
            return subSysDesc;
        }

        public void setSubSysDesc(String subSysDesc) {
            this.subSysDesc = subSysDesc;
        }

        public String getSysName() {
            return sysName;
        }

        public void setSysName(String sysName) {
            this.sysName = sysName;
        }

        public JSONObject getRadio1() {
            return radio1;
        }

        public void setRadio1(JSONObject radio1) {
            this.radio1 = radio1;
        }

        public JSONObject getCheckbox2() {
            return checkbox2;
        }

        public void setCheckbox2(JSONObject checkbox2) {
            this.checkbox2 = checkbox2;
        }

        public JSONObject getSelect3() {
            return select3;
        }

        public void setSelect3(JSONObject select3) {
            this.select3 = select3;
        }

        public JSONObject getSelect4() {
            return select4;
        }

        public void setSelect4(JSONObject select4) {
            this.select4 = select4;
        }

        public JSONObject getText5() {
            return text5;
        }

        public void setText5(JSONObject text5) {
            this.text5 = text5;
        }
    }
}

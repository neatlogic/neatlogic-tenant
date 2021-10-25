/*
 * Copyright(c) 2021 TechSureCo.,Ltd.AllRightsReserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.matrix.test;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
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
            subSysVo.setSubSysId(i);
            subSysVo.setSubSysName("子系统名_" + i);
            subSysVo.setSubSysDesc("子系统描述_" + i);
            subSysVo.setSysName("系统名_" + i/10);
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
        String sysName = paramObj.getString("sysName");
        if (StringUtils.isNotBlank(sysName)) {
            for (SubSysVo subSysVo : staticSubSysList) {
                if (subSysVo.sysName.contains(sysName)) {
                    resultList.add(subSysVo);
                }
            }
        } else {
            resultList.addAll(staticSubSysList);
        }
        BasePageVo basePageVo = JSONObject.toJavaObject(paramObj, BasePageVo.class);
        int rowNum = resultList.size();
        if (rowNum > 0) {
            basePageVo.setRowNum(rowNum);
            if (basePageVo.getCurrentPage() <= basePageVo.getPageCount()) {
                int fromIndex = basePageVo.getStartNum();
                int toIndex = fromIndex + basePageVo.getPageSize();
                toIndex = toIndex >  rowNum ? rowNum : toIndex;
                resultList =  resultList.subList(fromIndex, toIndex);
                return TableResultUtil.getResult(resultList, basePageVo);
            }
        }
        return TableResultUtil.getResult(resultList, basePageVo);
    }

    private static class SubSysVo extends BasePageVo {
        private long subSysId;
        private String subSysName;
        private String subSysDesc;
        private String sysName;

        public SubSysVo(){}

        public long getSubSysId() {
            return subSysId;
        }

        public void setSubSysId(long subSysId) {
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
    }
}

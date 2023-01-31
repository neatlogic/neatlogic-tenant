/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package neatlogic.module.tenant.api.notify;

import java.util.ArrayList;
import java.util.List;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.common.util.PageUtil;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.notify.core.INotifyHandler;
import neatlogic.framework.notify.core.NotifyHandlerFactory;
import neatlogic.framework.notify.dao.mapper.NotifyMapper;
import neatlogic.framework.notify.dto.NotifyPolicyConfigVo;
import neatlogic.framework.notify.dto.NotifyPolicyVo;
import neatlogic.framework.notify.dto.NotifyTemplateVo;
import neatlogic.framework.notify.exception.NotifyHandlerNotFoundException;
import neatlogic.framework.notify.exception.NotifyPolicyNotFoundException;

@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class NotifyPolicyTemplateListApi extends PrivateApiComponentBase {

    @Autowired
    private NotifyMapper notifyMapper;

    @Override
    public String getToken() {
        return "notify/policy/template/list";
    }

    @Override
    public String getName() {
        return "通知策略模板列表接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "policyId", type = ApiParamType.LONG, isRequired = true, desc = "策略id"),
            @Param(name = "notifyHandler", type = ApiParamType.STRING, desc = "通知处理器"),
            @Param(name = "keyword", type = ApiParamType.STRING, desc = "关键字", xss=true),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页数据条目")
    })
    @Output({
            @Param(explode = BasePageVo.class),
            @Param(name = "templateList", explode = NotifyTemplateVo[].class, desc = "通知模板列表")
    })
    @Description(desc = "通知策略模板列表接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long policyId = jsonObj.getLong("policyId");
        NotifyPolicyVo notifyPolicyVo = notifyMapper.getNotifyPolicyById(policyId);
        if (notifyPolicyVo == null) {
            throw new NotifyPolicyNotFoundException(policyId.toString());
        }

        String notifyHandler = jsonObj.getString("notifyHandler");
        if (StringUtils.isNotBlank(notifyHandler)) {
            INotifyHandler handler = NotifyHandlerFactory.getHandler(notifyHandler);
            if (handler == null) {
                throw new NotifyHandlerNotFoundException(notifyHandler);
            }
        }

        List<NotifyTemplateVo> templateList = new ArrayList<>();
        BasePageVo basePageVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<NotifyTemplateVo>(){});

        NotifyPolicyConfigVo config = notifyPolicyVo.getConfig();
        for (NotifyTemplateVo notifyTemplateVo : config.getTemplateList()) {
            if (StringUtils.isNotBlank(notifyHandler) && !notifyHandler.equals(notifyTemplateVo.getNotifyHandler())) {
                continue;
            }
            if (StringUtils.isNotBlank(basePageVo.getKeyword()) && !notifyTemplateVo.getName().toLowerCase().contains(basePageVo.getKeyword().toLowerCase())) {
                continue;
            }
            templateList.add(notifyTemplateVo);
        }
        JSONObject resultObj = new JSONObject();
        resultObj.put("currentPage", basePageVo.getCurrentPage());
        resultObj.put("pageSize", basePageVo.getPageSize());
        int rowNum = 0;
        int pageCount = 0;
        if(CollectionUtils.isNotEmpty(templateList)){
            templateList.sort((e1, e2) -> e2.getActionTime().compareTo(e1.getActionTime()));
            rowNum = templateList.size();
            pageCount = PageUtil.getPageCount(rowNum, basePageVo.getPageSize());
            resultObj.put("pageCount", pageCount);
            resultObj.put("rowNum", rowNum);
            int fromIndex = basePageVo.getStartNum();
            if(fromIndex < rowNum) {
                int toIndex = fromIndex + basePageVo.getPageSize();
                toIndex = toIndex > rowNum ? rowNum : toIndex;
                templateList = templateList.subList(fromIndex, toIndex);
            }else{
                templateList = new ArrayList<>();
            }
        }

        resultObj.put("pageCount", pageCount);
        resultObj.put("rowNum", rowNum);
        resultObj.put("templateList", templateList);
        return resultObj;
    }

}

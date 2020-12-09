package codedriver.module.tenant.api.notify;

import java.util.ArrayList;
import java.util.List;

import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.notify.core.INotifyHandler;
import codedriver.framework.notify.core.NotifyHandlerFactory;
import codedriver.framework.notify.dao.mapper.NotifyMapper;
import codedriver.framework.notify.dto.NotifyPolicyConfigVo;
import codedriver.framework.notify.dto.NotifyPolicyVo;
import codedriver.framework.notify.dto.NotifyTemplateVo;
import codedriver.framework.notify.exception.NotifyHandlerNotFoundException;
import codedriver.framework.notify.exception.NotifyPolicyNotFoundException;

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
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页数据条目"),
            @Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否需要分页，默认true")
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
        if(basePageVo.getNeedPage()){
            int rowNum = templateList.size();
            int pageCount = PageUtil.getPageCount(rowNum, basePageVo.getPageSize());
            resultObj.put("currentPage", basePageVo.getCurrentPage());
            resultObj.put("pageSize", basePageVo.getPageSize());
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

        resultObj.put("templateList", templateList);
        return resultObj;
    }

}

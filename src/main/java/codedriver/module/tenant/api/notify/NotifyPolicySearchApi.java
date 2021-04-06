package codedriver.module.tenant.api.notify;

import java.util.List;

import codedriver.framework.dependency.constvalue.CalleeType;
import codedriver.framework.dependency.core.DependencyManager;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.notify.dao.mapper.NotifyMapper;
import codedriver.framework.notify.dto.NotifyPolicyVo;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class NotifyPolicySearchApi extends PrivateApiComponentBase {

    @Autowired
    private NotifyMapper notifyMapper;

    @Override
    public String getToken() {
        return "notify/policy/search";
    }

    @Override
    public String getName() {
        return "通知策略管理列表搜索接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "keyword", type = ApiParamType.STRING, desc = "关键字搜索"),
            @Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否分页"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页数"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页条数"),
            @Param(name = "handler", type = ApiParamType.STRING, isRequired = true, desc = "通知策略处理器")
    })
    @Output({
            @Param(explode = BasePageVo.class),
            @Param(name = "tbodyList", explode = NotifyPolicyVo[].class, desc = "通知策略列表")
    })
    @Description(desc = "通知策略管理列表搜索接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject resultObj = new JSONObject();
        NotifyPolicyVo notifyPolicyVo = JSON.toJavaObject(jsonObj, NotifyPolicyVo.class);
        List<NotifyPolicyVo> tbodyList = notifyMapper.getNotifyPolicyList(notifyPolicyVo);

        for (NotifyPolicyVo notifyPolicy : tbodyList) {
            int count = DependencyManager.getDependencyCount(CalleeType.NOTIFY_POLICY, notifyPolicy.getId());
            notifyPolicy.setInvokerCount(count);
        }
        resultObj.put("tbodyList", tbodyList);
        if (notifyPolicyVo.getNeedPage()) {
            int rowNum = notifyMapper.getNotifyPolicyCount(notifyPolicyVo);
            resultObj.put("currentPage", notifyPolicyVo.getCurrentPage());
            resultObj.put("pageSize", notifyPolicyVo.getPageSize());
            resultObj.put("pageCount", PageUtil.getPageCount(rowNum, notifyPolicyVo.getPageSize()));
            resultObj.put("rowNum", rowNum);
        }
        return resultObj;
    }

}

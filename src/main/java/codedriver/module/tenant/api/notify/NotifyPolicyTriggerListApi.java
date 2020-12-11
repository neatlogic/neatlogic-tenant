package codedriver.module.tenant.api.notify;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.dto.ConditionParamVo;
import codedriver.framework.notify.dao.mapper.NotifyMapper;
import codedriver.framework.notify.dto.NotifyPolicyConfigVo;
import codedriver.framework.notify.dto.NotifyPolicyVo;
import codedriver.framework.notify.dto.NotifyTriggerVo;
import codedriver.framework.notify.exception.NotifyPolicyNotFoundException;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class NotifyPolicyTriggerListApi extends PrivateApiComponentBase {

    @Autowired
    private NotifyMapper notifyMapper;

    @Override
    public String getToken() {
        return "notify/policy/trigger/list";
    }

    @Override
    public String getName() {
        return "通知策略触发类型列表接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "policyId", type = ApiParamType.LONG, isRequired = true, desc = "策略id"),
            @Param(name = "hasAction", type = ApiParamType.ENUM, rule = "-1,0,1", desc = "是否有动作,-1:全部;0:无动作;1:有动作"),
            @Param(name = "keyword", type = ApiParamType.STRING, desc = "关键字", xss=true),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页数据条目"),
            @Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否需要分页，默认true")
    })
    @Output({
            @Param(explode = BasePageVo.class),
            @Param(name = "triggerList", explode = ConditionParamVo[].class, desc = "参数列表")
    })
    @Description(desc = "通知策略触发类型列表接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        List<NotifyTriggerVo> resultList = new ArrayList<>();
        Long policyId = jsonObj.getLong("policyId");
        Integer hasAction = jsonObj.getInteger("hasAction") == null ? -1 : jsonObj.getInteger("hasAction");
        BasePageVo basePageVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<BasePageVo>(){});
        NotifyPolicyVo notifyPolicyVo = notifyMapper.getNotifyPolicyById(policyId);
        if (notifyPolicyVo == null) {
            throw new NotifyPolicyNotFoundException(policyId.toString());
        }
        NotifyPolicyConfigVo config = notifyPolicyVo.getConfig();
        List<NotifyTriggerVo> triggerList = config.getTriggerList();
        for (NotifyTriggerVo vo : triggerList) {
            if (StringUtils.isNotBlank(basePageVo.getKeyword())) {
                if (!vo.getTriggerName().toLowerCase().contains(basePageVo.getKeyword().toLowerCase())) {
                    continue;
                }
            }
            if(hasAction == 1 && CollectionUtils.isEmpty(vo.getNotifyList())){
                continue;
            }
            if(hasAction == 0 && CollectionUtils.isNotEmpty(vo.getNotifyList())){
                continue;
            }
            resultList.add(vo);
        }
        JSONObject resultObj = new JSONObject();
        if(basePageVo.getNeedPage()){
            int rowNum = resultList.size();
            int pageCount = PageUtil.getPageCount(rowNum, basePageVo.getPageSize());
            resultObj.put("currentPage", basePageVo.getCurrentPage());
            resultObj.put("pageSize", basePageVo.getPageSize());
            resultObj.put("pageCount", pageCount);
            resultObj.put("rowNum", rowNum);
            int fromIndex = basePageVo.getStartNum();
            if(fromIndex < rowNum) {
                int toIndex = fromIndex + basePageVo.getPageSize();
                toIndex = toIndex > rowNum ? rowNum : toIndex;
                resultList = resultList.subList(fromIndex, toIndex);
            }else{
                resultList = new ArrayList<>();
            }
        }
        resultObj.put("triggerList", resultList);
        return resultObj;
    }

}

package codedriver.module.tenant.api.notify;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dto.ConditionParamVo;
import codedriver.framework.notify.core.INotifyPolicyHandler;
import codedriver.framework.notify.core.NotifyPolicyHandlerFactory;
import codedriver.framework.notify.dao.mapper.NotifyMapper;
import codedriver.framework.notify.dto.NotifyPolicyConfigVo;
import codedriver.framework.notify.dto.NotifyPolicyVo;
import codedriver.framework.notify.exception.NotifyPolicyHandlerNotFoundException;
import codedriver.framework.notify.exception.NotifyPolicyNotFoundException;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class NotifyPolicyParamListApi extends PrivateApiComponentBase {

    @Autowired
    private NotifyMapper notifyMapper;

    @Override
    public String getToken() {
        return "notify/policy/param/list";
    }

    @Override
    public String getName() {
        return "通知策略参数列表接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "policyId", type = ApiParamType.LONG, isRequired = true, desc = "策略id"),
            @Param(name = "isEditable", type = ApiParamType.ENUM, rule = "-1,0,1", desc = "-1:全部;0:系统参数(不可编辑);1:自定义参数(可编辑),用于筛选系统/自定义参数"),
            @Param(name = "keyword", type = ApiParamType.STRING, desc = "关键字", xss=true),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页数据条目"),
            @Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否需要分页，默认true")
    })
    @Output({
            @Param(explode = BasePageVo.class),
            @Param(name = "paramList", explode = ConditionParamVo[].class, desc = "参数列表")
    })
    @Description(desc = "通知策略参数列表接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        List<ConditionParamVo> paramList = new ArrayList<>();
        Long policyId = jsonObj.getLong("policyId");
        Integer isEditable = jsonObj.getInteger("isEditable") == null ? -1 : jsonObj.getInteger("isEditable");
        NotifyPolicyVo notifyPolicyVo = notifyMapper.getNotifyPolicyById(policyId);
        if (notifyPolicyVo == null) {
            throw new NotifyPolicyNotFoundException(policyId.toString());
        }
        INotifyPolicyHandler notifyPolicyHandler = NotifyPolicyHandlerFactory.getHandler(notifyPolicyVo.getHandler());
        if (notifyPolicyHandler == null) {
            throw new NotifyPolicyHandlerNotFoundException(notifyPolicyVo.getHandler());
        }
        List<ConditionParamVo> list = new ArrayList<>();
        NotifyPolicyConfigVo config = notifyPolicyVo.getConfig();
        List<ConditionParamVo> customParamList = config.getParamList();
        if(CollectionUtils.isNotEmpty(customParamList)){
            customParamList.sort(Comparator.comparing(ConditionParamVo::getLcd));
            Collections.reverse(customParamList);
        }
        list.addAll(customParamList);
        List<ConditionParamVo> systemParamList = notifyPolicyHandler.getSystemParamList();
        systemParamList.sort((e1, e2) -> e1.getName().compareToIgnoreCase(e2.getName()));
        list.addAll(systemParamList);
        BasePageVo basePageVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<BasePageVo>() { });
        for (ConditionParamVo notifyPolicyParamVo : list) {
            if (StringUtils.isNotBlank(basePageVo.getKeyword())) {
                if (!notifyPolicyParamVo.getName().toLowerCase().contains(basePageVo.getKeyword().toLowerCase())
                    && !notifyPolicyParamVo.getLabel().toLowerCase().contains(basePageVo.getKeyword().toLowerCase())) {
                    continue;
                }
            }
            if(isEditable != -1 && (isEditable != notifyPolicyParamVo.getIsEditable())){
                continue;
            }
            paramList.add(notifyPolicyParamVo);
        }
        JSONObject resultObj = new JSONObject();
        if(basePageVo.getNeedPage()){
            int rowNum = paramList.size();
            int pageCount = PageUtil.getPageCount(rowNum, basePageVo.getPageSize());
            resultObj.put("currentPage", basePageVo.getCurrentPage());
            resultObj.put("pageSize", basePageVo.getPageSize());
            resultObj.put("pageCount", pageCount);
            resultObj.put("rowNum", rowNum);
            int fromIndex = basePageVo.getStartNum();
            if(fromIndex < rowNum) {
                int toIndex = fromIndex + basePageVo.getPageSize();
                toIndex = toIndex > rowNum ? rowNum : toIndex;
                paramList = paramList.subList(fromIndex, toIndex);
            }else{
                paramList = new ArrayList<>();
            }
        }
        resultObj.put("paramList", paramList);
        return resultObj;
    }

}

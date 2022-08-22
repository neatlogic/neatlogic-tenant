/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.notify;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.constvalue.GroupSearch;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dependency.constvalue.FrameworkFromType;
import codedriver.framework.dependency.core.DependencyManager;
import codedriver.framework.dto.ConditionParamVo;
import codedriver.framework.dto.UserTypeVo;
import codedriver.framework.dto.UserVo;
import codedriver.framework.notify.core.INotifyPolicyHandler;
import codedriver.framework.notify.core.NotifyPolicyHandlerFactory;
import codedriver.framework.notify.dao.mapper.NotifyMapper;
import codedriver.framework.notify.dto.NotifyPolicyConfigVo;
import codedriver.framework.notify.dto.NotifyPolicyVo;
import codedriver.framework.notify.dto.NotifyTriggerVo;
import codedriver.framework.notify.exception.NotifyPolicyHandlerNotFoundException;
import codedriver.framework.notify.exception.NotifyPolicyNotFoundException;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.usertype.UserTypeFactory;
import codedriver.module.tenant.service.notify.NotifyPolicyService;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class NotifyPolicyGetApi extends PrivateApiComponentBase {

    @Autowired
    private NotifyMapper notifyMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private NotifyPolicyService notifyPolicyService;

    @Override
    public String getToken() {
        return "notify/policy/get";
    }

    @Override
    public String getName() {
        return "通知策略信息获取接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "id", type = ApiParamType.LONG, isRequired = true, desc = "策略id")})
    @Output({@Param(explode = NotifyPolicyVo.class, desc = "策略信息")})
    @Description(desc = "通知策略信息获取接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long id = jsonObj.getLong("id");
        NotifyPolicyVo notifyPolicyVo = notifyMapper.getNotifyPolicyById(id);
        if (notifyPolicyVo == null) {
            throw new NotifyPolicyNotFoundException(id.toString());
        }
        INotifyPolicyHandler notifyPolicyHandler = NotifyPolicyHandlerFactory.getHandler(notifyPolicyVo.getHandler());
        if (notifyPolicyHandler == null) {
            throw new NotifyPolicyHandlerNotFoundException(notifyPolicyVo.getHandler());
        }
        /** 获取工单干系人枚举 */
        //TODO 没有兼容多模块
        Map<String, UserTypeVo> userTypeVoMap = UserTypeFactory.getUserTypeMap();
        UserTypeVo UsertypeVo = userTypeVoMap.get("process");
        Map<String, String> processUserType = UsertypeVo.getValues();

        NotifyPolicyConfigVo config = notifyPolicyVo.getConfig();
        List<NotifyTriggerVo> triggerList = config.getTriggerList();
        List<NotifyTriggerVo> notifyTriggerList = notifyPolicyHandler.getNotifyTriggerList();
        /** 矫正旧配置数据中的触发点 */
        /** 多删 -- 删除已经不存在的触发点 */
        Iterator<NotifyTriggerVo> iterator = triggerList.iterator();
        while (iterator.hasNext()){
            NotifyTriggerVo next = iterator.next();
            if(!notifyTriggerList.stream().anyMatch(o -> o.getTrigger().equals(next.getTrigger()))){
                iterator.remove();
            }
        }
        List<NotifyTriggerVo> triggerArray = new ArrayList<>();
        for (NotifyTriggerVo notifyTrigger : notifyTriggerList) {
            boolean existed = false;
            for (NotifyTriggerVo triggerObj : triggerList) {
                if (Objects.equals(notifyTrigger.getTrigger(), triggerObj.getTrigger())) {
                    /** 补充通知对象详细信息 */
                    notifyPolicyService.addReceiverExtraInfo(processUserType, triggerObj);
                    triggerObj.setDescription(notifyTrigger.getDescription());
                    triggerArray.add(triggerObj);
                    existed = true;
                    break;
                }
            }
            /** 少补 -- 新增老数据中没有而现在有的触发点 */
            if (!existed) {
                triggerArray.add(notifyTrigger);
            }
        }
        config.setTriggerList(triggerArray);
        List<ConditionParamVo> systemParamList = notifyPolicyHandler.getSystemParamList();
        List<ConditionParamVo> systemConditionOptionList = notifyPolicyHandler.getSystemConditionOptionList();
        List<ConditionParamVo> paramList = config.getParamList();
        if (CollectionUtils.isNotEmpty(paramList)) {
            for (ConditionParamVo param : paramList) {
                systemParamList.add(param);
                systemConditionOptionList.add(new ConditionParamVo(param));
            }
        }

        systemParamList.sort((e1, e2) -> e1.getName().compareToIgnoreCase(e2.getName()));
        systemConditionOptionList.sort((e1, e2) -> e1.getName().compareToIgnoreCase(e2.getName()));
        config.setParamList(systemParamList);
        config.setConditionOptionList(systemConditionOptionList);

        int count = DependencyManager.getDependencyCount(FrameworkFromType.NOTIFY_POLICY, id);
        notifyPolicyVo.setReferenceCount(count);
        return notifyPolicyVo;
    }

}

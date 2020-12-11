package codedriver.module.tenant.api.notify;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import codedriver.framework.common.constvalue.GroupSearch;
import codedriver.framework.dao.mapper.RoleMapper;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dto.*;
import codedriver.framework.notify.dto.*;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

import codedriver.framework.usertype.UserTypeFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.notify.core.INotifyPolicyHandler;
import codedriver.framework.notify.core.NotifyPolicyHandlerFactory;
import codedriver.framework.notify.dao.mapper.NotifyMapper;
import codedriver.framework.notify.exception.NotifyPolicyHandlerNotFoundException;
import codedriver.framework.notify.exception.NotifyPolicyNotFoundException;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class NotifyPolicyGetApi extends PrivateApiComponentBase {

    @Autowired
    private NotifyMapper notifyMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TeamMapper teamMapper;

    @Autowired
    private RoleMapper roleMapper;

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
        Map<String, UserTypeVo> userTypeVoMap = UserTypeFactory.getUserTypeMap();
        UserTypeVo UsertypeVo = userTypeVoMap.get("process");
        Map<String, String> processUserType = UsertypeVo.getValues();

        NotifyPolicyConfigVo config = notifyPolicyVo.getConfig();
        List<NotifyTriggerVo> triggerList = config.getTriggerList();
        List<ValueTextVo> notifyTriggerList = notifyPolicyHandler.getNotifyTriggerList();
        List<NotifyTriggerVo> triggerArray = new ArrayList<>();
        for (ValueTextVo notifyTrigger : notifyTriggerList) {
            boolean existed = false;
            for (NotifyTriggerVo triggerObj : triggerList) {
                if (Objects.equals(notifyTrigger.getValue(), triggerObj.getTrigger())) {
                    /** 补充通知对象详细信息开始 */
                    List<NotifyTriggerNotifyVo> notifyTriggerNotifyVos = triggerObj.getNotifyList();
                    if(CollectionUtils.isNotEmpty(notifyTriggerNotifyVos)){
                        for(NotifyTriggerNotifyVo triggerNotifyVo : notifyTriggerNotifyVos){
                            List<NotifyActionVo> actionList = triggerNotifyVo.getActionList();
                            if(CollectionUtils.isNotEmpty(actionList)){
                                for(NotifyActionVo actionVo : actionList){
                                    List<String> receiverList = actionVo.getReceiverList();
                                    if(CollectionUtils.isNotEmpty(receiverList)){
                                        JSONArray receiverObjList = new JSONArray();
                                        for(String receiver : receiverList){
                                            JSONObject receiverObj = new JSONObject();
                                            String[] split = receiver.split("#");
                                            receiverObj.put("type",split[0]);
                                            if (GroupSearch.USER.getValue().equals(split[0])) {
                                                UserVo user = userMapper.getUserBaseInfoByUuid(split[1]);
                                                if(user != null){
                                                    receiverObj.put("uuid",user.getUuid());
                                                    receiverObj.put("name",user.getUserName());
                                                    receiverObj.put("pinyin",user.getPinyin());
                                                    receiverObj.put("avatar",user.getAvatar());
                                                    receiverObj.put("vipLevel",user.getVipLevel());
                                                }
                                            }else if(GroupSearch.TEAM.getValue().equals(split[0])){
                                                TeamVo team = teamMapper.getTeamByUuid(split[1]);
                                                if(team != null){
                                                    receiverObj.put("uuid",team.getUuid());
                                                    receiverObj.put("name",team.getName());
                                                }
                                            }else if(GroupSearch.ROLE.getValue().equals(split[0])){
                                                RoleVo role = roleMapper.getRoleByUuid(split[1]);
                                                if(role != null){
                                                    receiverObj.put("uuid",role.getUuid());
                                                    receiverObj.put("name",role.getName());
                                                }
                                            }else{
                                                receiverObj.put("name",processUserType.get(split[1]));
                                            }
                                            receiverObjList.add(receiverObj);
                                        }
                                        actionVo.setReceiverObjList(receiverObjList);
                                    }
                                }
                            }
                        }
                    }
                    /** 补充通知对象详细信息结束 */
                    triggerArray.add(triggerObj);
                    existed = true;
                    break;
                }
            }
            if (!existed) {
                JSONObject triggerObj = new JSONObject();
                triggerObj.put("trigger", notifyTrigger.getValue());
                triggerObj.put("triggerName", notifyTrigger.getText());
                triggerObj.put("notifyList", new JSONArray());
                triggerArray.add(new NotifyTriggerVo((String)notifyTrigger.getValue(), notifyTrigger.getText()));
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
        List<String> adminUserUuidList = config.getAdminUserUuidList();
        if (CollectionUtils.isNotEmpty(adminUserUuidList)) {
            List<UserVo> userList = userMapper.getUserByUserUuidList(adminUserUuidList);
            if(CollectionUtils.isNotEmpty(userList)){
                List<String> list = userList.stream().map(o -> GroupSearch.USER.getValuePlugin() + o.getUuid()).collect(Collectors.toList());
                config.setUserList(list);
            }
        }
        return notifyPolicyVo;
    }

}

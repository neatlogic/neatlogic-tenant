package codedriver.module.tenant.service.notify;

import codedriver.framework.common.constvalue.GroupSearch;
import codedriver.framework.dao.mapper.RoleMapper;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.RoleVo;
import codedriver.framework.dto.TeamVo;
import codedriver.framework.dto.UserVo;
import codedriver.framework.notify.dto.NotifyActionVo;
import codedriver.framework.notify.dto.NotifyTriggerNotifyVo;
import codedriver.framework.notify.dto.NotifyTriggerVo;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class NotifyPolicyServiceImpl implements NotifyPolicyService {

	@Autowired
	private UserMapper userMapper;

	@Autowired
	private TeamMapper teamMapper;

	@Autowired
	private RoleMapper roleMapper;

	@Override
	public void addReceiverExtraInfo(Map<String, String> processUserType, NotifyTriggerVo triggerObj) {
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
	}
}

/*
 * Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package neatlogic.module.tenant.api.notify.job;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.BaseEditorVo;
import neatlogic.framework.common.util.PageUtil;
import neatlogic.framework.dao.mapper.RoleMapper;
import neatlogic.framework.dao.mapper.TeamMapper;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.notify.constvalue.NotifyRecipientType;
import neatlogic.framework.notify.core.INotifyContentHandler;
import neatlogic.framework.notify.core.INotifyHandler;
import neatlogic.framework.notify.core.NotifyContentHandlerFactory;
import neatlogic.framework.notify.core.NotifyHandlerFactory;
import neatlogic.framework.notify.dao.mapper.NotifyJobMapper;
import neatlogic.framework.notify.dto.job.NotifyJobReceiverVo;
import neatlogic.framework.notify.dto.job.NotifyJobVo;
import neatlogic.framework.notify.exception.NotifyContentHandlerNotFoundException;
import neatlogic.framework.notify.exception.NotifyHandlerNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.scheduler.dao.mapper.SchedulerMapper;
import neatlogic.framework.scheduler.dto.JobAuditVo;
import neatlogic.framework.usertype.UserTypeFactory;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class NotifyJobSearchApi extends PrivateApiComponentBase {

	@Autowired
	private NotifyJobMapper notifyJobMapper;

	@Autowired
	private SchedulerMapper schedulerMapper;

	@Autowired
	private UserMapper userMapper;

	@Autowired
	private TeamMapper teamMapper;

	@Autowired
	private RoleMapper roleMapper;

	@Override
	public String getToken() {
		return "notify/job/search";
	}

	@Override
	public String getName() {
		return "????????????????????????";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
			@Param(name = "keyword", type = ApiParamType.STRING,desc = "?????????", xss = true),
			@Param(name = "currentPage",type = ApiParamType.INTEGER,desc = "?????????"),
			@Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "??????????????????"),
			@Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "???????????????????????????true")
	})
	@Output({@Param(name = "tbodyList", type = ApiParamType.JSONARRAY, explode = NotifyJobVo[].class, desc = "??????????????????"),
			@Param(explode = BaseEditorVo.class)
	})
	@Description(desc = "????????????????????????")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JSONObject returnObj = new JSONObject();
		NotifyJobVo vo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<NotifyJobVo>(){});
		if(vo.getNeedPage()){
			int rowNum = notifyJobMapper.searchJobCount(vo);
			returnObj.put("pageSize", vo.getPageSize());
			returnObj.put("currentPage", vo.getCurrentPage());
			returnObj.put("rowNum", rowNum);
			returnObj.put("pageCount", PageUtil.getPageCount(rowNum, vo.getPageSize()));
		}
		List<NotifyJobVo> jobList = notifyJobMapper.searchJob(vo);
		if(CollectionUtils.isNotEmpty(jobList)){
			for(NotifyJobVo job : jobList){
				JobAuditVo jobAuditVo = new JobAuditVo();
				jobAuditVo.setJobUuid(job.getId().toString());
				job.setExecCount(schedulerMapper.searchJobAuditCount(jobAuditVo));

				INotifyContentHandler handler = NotifyContentHandlerFactory.getHandler(job.getHandler());
				if(handler == null){
					throw new NotifyContentHandlerNotFoundException(job.getHandler());
				}
				INotifyHandler notifyHandler = NotifyHandlerFactory.getHandler(job.getNotifyHandler());
				if(notifyHandler == null){
					throw new NotifyHandlerNotFoundException(job.getNotifyHandler());
				}
				JSONArray messageAttrList = handler.getMessageAttrList(job.getNotifyHandler());
				/** ????????????????????????????????????????????????????????????????????????????????? */
				List<NotifyJobReceiverVo> toList = notifyJobMapper.getToListByJobId(job.getId());
				JSONArray toArray = new JSONArray();
				if(CollectionUtils.isNotEmpty(toList)){
					for(NotifyJobReceiverVo receiverVo : toList){
						if(NotifyRecipientType.USER.getValue().equals(receiverVo.getType())){
							toArray.add(userMapper.getUserBaseInfoByUuidWithoutCache(receiverVo.getReceiver()));
						}else if(NotifyRecipientType.TEAM.getValue().equals(receiverVo.getType())){
							toArray.add(teamMapper.getTeamByUuid(receiverVo.getReceiver()));
						}else if(NotifyRecipientType.ROLE.getValue().equals(receiverVo.getType())){
							toArray.add(roleMapper.getRoleByUuid(receiverVo.getReceiver()));
						}else if(NotifyRecipientType.EMAIL.getValue().equals(receiverVo.getType())){
							toArray.add(new JSONObject(){
								{
									this.put("initType",NotifyRecipientType.EMAIL.getValue());
									this.put("name",receiverVo.getReceiver());
								}
							});
						}else if(NotifyRecipientType.PROCESSUSERTYPE.getValue().equals(receiverVo.getType())){
							toArray.add(new JSONObject(){
								{
									//TODO ?????????????????????
									this.put("initType",NotifyRecipientType.PROCESSUSERTYPE.getValue());
									this.put("name",UserTypeFactory.getUserTypeMap().get("process").getValues().get(receiverVo.getReceiver()));
								}
							});
						}
					}
				}else if(CollectionUtils.isNotEmpty(messageAttrList)){
					for(Object obj : messageAttrList){
						JSONObject object = JSONObject.parseObject(obj.toString());
						if("toList".equals(object.getString("name"))){
							toArray.add(new JSONObject(){
								{
									this.put("initType",NotifyRecipientType.CUSTOM.getValue());
									this.put("name",object.getString("placeholder"));
								}
							});
							break;
						}
					}
				}
				job.setToVoList(toArray);

				/** ???????????????????????? */
				job.setNotifyHandler(notifyHandler.getName());
			}
		}
		returnObj.put("tbodyList",jobList);
		return returnObj;
	}
}

/*Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

package neatlogic.module.tenant.api.notify.job;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.util.PageUtil;
import neatlogic.framework.dao.mapper.RoleMapper;
import neatlogic.framework.dao.mapper.TeamMapper;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dto.UserTypeVo;
import neatlogic.framework.notify.constvalue.NotifyRecipientType;
import neatlogic.framework.notify.core.INotifyContentHandler;
import neatlogic.framework.notify.core.INotifyHandler;
import neatlogic.framework.notify.core.NotifyContentHandlerFactory;
import neatlogic.framework.notify.core.NotifyHandlerFactory;
import neatlogic.framework.notify.dao.mapper.NotifyJobMapper;
import neatlogic.framework.notify.dto.job.NotifyJobAuditVo;
import neatlogic.framework.notify.dto.job.NotifyJobReceiverVo;
import neatlogic.framework.notify.dto.job.NotifyJobVo;
import neatlogic.framework.notify.exception.NotifyContentHandlerNotFoundException;
import neatlogic.framework.notify.exception.NotifyHandlerNotFoundException;
import neatlogic.framework.notify.exception.NotifyJobNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.scheduler.dao.mapper.SchedulerMapper;
import neatlogic.framework.scheduler.dto.JobAuditVo;
import neatlogic.framework.usertype.UserTypeFactory;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class NotifyJobAuditSearchApi extends PrivateApiComponentBase {
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
		return "notify/job/audit/search";
	}

	@Override
	public String getName() {
		return "获取通知策略定时任务发送记录列表";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
			@Param( name = "id",
					type = ApiParamType.STRING,
					desc = "定时任务ID",
					isRequired = true),
			@Param(name = "currentPage",
					type = ApiParamType.INTEGER,
					desc = "当前页"),
			@Param(name = "pageSize",
					type = ApiParamType.INTEGER,
					desc = "每页数据条目"),
			@Param(name = "needPage",
					type = ApiParamType.BOOLEAN,
					desc = "是否需要分页，默认true")
	})
	@Output({@Param(name = "tbodyList", type = ApiParamType.JSONARRAY, explode = NotifyJobAuditVo[].class, desc = "发送记录列表")})
	@Description(desc = "获取通知策略定时任务发送记录列表")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JobAuditVo vo = JSONObject.toJavaObject(jsonObj, JobAuditVo.class);
		vo.setJobUuid(jsonObj.getString("id"));
		NotifyJobVo job = notifyJobMapper.getJobBaseInfoById(Long.valueOf(vo.getJobUuid()));
		if(job == null){
			throw new NotifyJobNotFoundException(Long.valueOf(vo.getJobUuid()));
		}
		int rowNum = schedulerMapper.searchJobAuditCount(vo);
		int pageCount = PageUtil.getPageCount(rowNum, vo.getPageSize());
		vo.setPageCount(pageCount);
		vo.setRowNum(rowNum);
		List<NotifyJobAuditVo> jobAuditList = notifyJobMapper.searchJobAudit(vo);

		/** 补充收件人，如果没有，再看是否插件自带了收件人 */
		if(CollectionUtils.isNotEmpty(jobAuditList)){
			INotifyContentHandler handler = NotifyContentHandlerFactory.getHandler(job.getHandler());
			if(handler == null){
				throw new NotifyContentHandlerNotFoundException(job.getHandler());
			}
			INotifyHandler notifyHandler = NotifyHandlerFactory.getHandler(job.getNotifyHandler());
			if(notifyHandler == null){
				throw new NotifyHandlerNotFoundException(job.getNotifyHandler());
			}
			JSONArray messageAttrList = handler.getMessageAttrList(job.getNotifyHandler());

			List<NotifyJobReceiverVo> toList = notifyJobMapper.getToListByJobId(Long.valueOf(vo.getJobUuid()));
			JSONArray toArray = new JSONArray();
			if(CollectionUtils.isNotEmpty(toList)){
				Map<String, UserTypeVo> userTypeVoMap = UserTypeFactory.getUserTypeMap();
				UserTypeVo UsertypeVo = userTypeVoMap.get("process");
				final Map<String, String> processUserType = new HashMap<>();
				if (UsertypeVo != null && UsertypeVo.getValues() != null) {
					processUserType.putAll(UsertypeVo.getValues());
				}
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
					//TODO 没有兼容多模块
					}else if(NotifyRecipientType.PROCESSUSERTYPE.getValue().equals(receiverVo.getType())){
						toArray.add(new JSONObject(){
							{
								this.put("initType",NotifyRecipientType.PROCESSUSERTYPE.getValue());
								this.put("name", processUserType.get(receiverVo.getReceiver()));
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
			for(NotifyJobAuditVo auditVo : jobAuditList){
				/** 为防止相同的JSONArray被替换成$ref，需要做如下转换 */
				auditVo.setToVoList(JSONArray.parseArray(JSONArray.toJSONString(toArray, SerializerFeature.DisableCircularReferenceDetect)));
				auditVo.setStatusVo(JobAuditVo.Status.getStatus(auditVo.getStatus()));
			}
		}

		JSONObject resultObj = new JSONObject();
		resultObj.put("tbodyList", jobAuditList);
		resultObj.put("currentPage", vo.getCurrentPage());
		resultObj.put("pageSize", vo.getPageSize());
		resultObj.put("pageCount", vo.getPageCount());
		resultObj.put("rowNum", vo.getRowNum());
		return resultObj;
	}
}

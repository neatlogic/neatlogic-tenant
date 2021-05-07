/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.notify.job;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.BaseEditorVo;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.dao.mapper.RoleMapper;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.notify.constvalue.NotifyRecipientType;
import codedriver.framework.notify.core.INotifyContentHandler;
import codedriver.framework.notify.core.INotifyHandler;
import codedriver.framework.notify.core.NotifyContentHandlerFactory;
import codedriver.framework.notify.core.NotifyHandlerFactory;
import codedriver.framework.notify.dao.mapper.NotifyJobMapper;
import codedriver.framework.notify.dto.job.NotifyJobReceiverVo;
import codedriver.framework.notify.dto.job.NotifyJobVo;
import codedriver.framework.notify.exception.NotifyContentHandlerNotFoundException;
import codedriver.framework.notify.exception.NotifyHandlerNotFoundException;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.scheduler.dao.mapper.SchedulerMapper;
import codedriver.framework.scheduler.dto.JobAuditVo;
import codedriver.framework.usertype.UserTypeFactory;
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
		return "查询通知定时任务";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
			@Param(name = "keyword", type = ApiParamType.STRING,desc = "关键词", xss = true),
			@Param(name = "currentPage",type = ApiParamType.INTEGER,desc = "当前页"),
			@Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页数据条目"),
			@Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否需要分页，默认true")
	})
	@Output({@Param(name = "tbodyList", type = ApiParamType.JSONARRAY, explode = NotifyJobVo[].class, desc = "定时任务列表"),
			@Param(explode = BaseEditorVo.class)
	})
	@Description(desc = "查询通知定时任务")
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
				/** 补充收件人详细信息，如果没有，再看是否插件自带了收件人 */
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

				/** 转换通知插件名称 */
				job.setNotifyHandler(notifyHandler.getName());
			}
		}
		returnObj.put("tbodyList",jobList);
		return returnObj;
	}
}

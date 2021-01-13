package codedriver.module.tenant.api.notify.job;

import codedriver.framework.common.constvalue.ApiParamType;
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
import codedriver.framework.notify.dto.job.NotifyJobAuditVo;
import codedriver.framework.notify.dto.job.NotifyJobReceiverVo;
import codedriver.framework.notify.dto.job.NotifyJobVo;
import codedriver.framework.notify.exception.NotifyContentHandlerNotFoundException;
import codedriver.framework.notify.exception.NotifyHandlerNotFoundException;
import codedriver.framework.notify.exception.NotifyJobNotFoundException;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.scheduler.dao.mapper.SchedulerMapper;
import codedriver.framework.scheduler.dto.JobAuditVo;
import codedriver.framework.usertype.UserTypeFactory;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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
								this.put("name", UserTypeFactory.getUserTypeMap().get("process").getValues().get(receiverVo.getReceiver()));
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

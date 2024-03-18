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

import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.NOTIFY_JOB_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dto.FieldValidResultVo;
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
import neatlogic.framework.notify.exception.NotifyJobNameRepeatException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.IValid;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.scheduler.core.IJob;
import neatlogic.framework.scheduler.core.SchedulerManager;
import neatlogic.framework.scheduler.dto.JobObject;
import neatlogic.framework.scheduler.exception.ScheduleIllegalParameterException;
import neatlogic.framework.util.RegexUtils;
import neatlogic.module.framework.notify.schedule.handler.NotifyContentJob;
import com.alibaba.fastjson.*;
import org.apache.commons.collections4.CollectionUtils;
import org.quartz.CronExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@AuthAction(action = NOTIFY_JOB_MODIFY.class)
@Transactional
@OperationType(type = OperationTypeEnum.CREATE)
public class NotifyJobSaveApi extends PrivateApiComponentBase {

	@Autowired
	private NotifyJobMapper notifyJobMapper;

	@Autowired
	private SchedulerManager schedulerManager;

	@Override
	public String getToken() {
		return "notify/job/save";
	}

	@Override
	public String getName() {
		return "保存通知定时任务";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
			@Param(name = "id", type = ApiParamType.LONG, desc = "定时任务ID"),
			@Param(name = "name", type = ApiParamType.REGEX, rule = RegexUtils.NAME, isRequired = true, xss = true, desc = "名称"),
			@Param(name = "handler", type = ApiParamType.STRING, isRequired = true,desc = "插件"),
			@Param(name = "config", type = ApiParamType.JSONOBJECT, desc = "插件自带参数"),
			@Param(name = "notifyHandler", type = ApiParamType.STRING, isRequired = true, desc = "通知方式插件"),
			@Param(name = "toList", type = ApiParamType.JSONARRAY, desc = "收件人列表，格式[\"user#userUuid\",\"team#teamUuid\",\"role#roleUuid\",\"processUserType#major\",\"email#123@qq.com\"]"),
			@Param(name = "ccList", type = ApiParamType.JSONARRAY,desc = "抄送人列表，格式同收件人"),
			@Param(name = "cron", type = ApiParamType.STRING, isRequired = true, desc = "corn表达式"),
			@Param(name = "isActive", type = ApiParamType.ENUM, isRequired = true, rule = "0,1", desc = "是否激活(0:禁用，1：激活)"),
	})
	@Output({ @Param(name = "id", type = ApiParamType.LONG, desc = "定时任务ID") })
	@Description(desc = "保存通知定时任务")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		NotifyJobVo job = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<NotifyJobVo>(){});
		if (!CronExpression.isValidExpression(job.getCron())) {
			throw new ScheduleIllegalParameterException(job.getCron());
		}
		INotifyContentHandler handler = NotifyContentHandlerFactory.getHandler(job.getHandler());
		INotifyHandler notifyHandler = NotifyHandlerFactory.getHandler(job.getNotifyHandler());
		if(handler == null){
			throw new NotifyContentHandlerNotFoundException(job.getHandler());
		}
		if(notifyHandler == null){
			throw new NotifyHandlerNotFoundException(job.getNotifyHandler());
		}
		if (notifyJobMapper.checkNameIsRepeat(job) > 0) {
			throw new NotifyJobNameRepeatException(job.getName());
		}

		Object messageConfig = JSONPath.read(jsonObj.toJSONString(), "config.messageConfig");
		List<NotifyJobReceiverVo> toVoList = new ArrayList<>();
		List<NotifyJobReceiverVo> ccVoList = new ArrayList<>();
		if(messageConfig != null){
			JSONObject jsonObject = JSONObject.parseObject(messageConfig.toString());
			/**组装接收人与抄送人*/
			JSONArray toArray = jsonObject.getJSONArray("toList");
			JSONArray ccArray = jsonObject.getJSONArray("ccList");
			getReceiverList(job, toArray,ccArray, toVoList,ccVoList);
		}

		NotifyJobVo oldJob = notifyJobMapper.getJobBaseInfoById(job.getId());
		job.setLcu(UserContext.get().getUserUuid());
		if (oldJob == null) {
			job.setFcu(UserContext.get().getUserUuid());
			notifyJobMapper.insertJob(job);
		} else {
			notifyJobMapper.updateJob(job);
			notifyJobMapper.deleteReceiverByJobId(job.getId());
		}

		if(CollectionUtils.isNotEmpty(toVoList)){
			notifyJobMapper.batchInsertReceiver(toVoList);
		}
		if(CollectionUtils.isNotEmpty(ccVoList)){
			notifyJobMapper.batchInsertReceiver(ccVoList);
		}

		IJob schedulerhandler = SchedulerManager.getHandler(NotifyContentJob.class.getName());
		String tenantUuid = TenantContext.get().getTenantUuid();
		JobObject newJobObject = new JobObject.Builder(job.getId().toString(), schedulerhandler.getGroupName(), schedulerhandler.getClassName(), tenantUuid).withCron(job.getCron()).addData("notifyContentJobId",job.getId()).build();
		if(job.getIsActive().intValue() == 1){
			schedulerManager.loadJob(newJobObject);
		}else{
			schedulerManager.unloadJob(newJobObject);
		}

		return job.getId();
	}

	public IValid name() {
		return value -> {
			NotifyJobVo job = JSON.toJavaObject(value, NotifyJobVo.class);
			if (notifyJobMapper.checkNameIsRepeat(job) > 0) {
				return new FieldValidResultVo(new NotifyJobNameRepeatException(job.getName()));
			}
			return new FieldValidResultVo();
		};
	}

	/***
	 * 拼装收件人
	 * @param job
	 * @param toArray
	 * @param ccArray
	 * @param toVoList
	 * @param ccVoList
	 */
	private void getReceiverList(NotifyJobVo job, JSONArray toArray, JSONArray ccArray,List<NotifyJobReceiverVo> toVoList,List<NotifyJobReceiverVo> ccVoList) {
		if(CollectionUtils.isNotEmpty(toArray)){
			List<String> toList = toArray.toJavaList(String.class);
			for(String to : toList){
				String[] split = to.split("#");
				if(NotifyRecipientType.getNotifyRecipientType(split[0]) != null) {
					NotifyJobReceiverVo receiver = new NotifyJobReceiverVo();
					receiver.setNotifyJobId(job.getId());
					receiver.setReceiver(split[1]);
					receiver.setType(split[0]);
					receiver.setReceiveType(INotifyHandler.RecipientType.TO.getValue());
					toVoList.add(receiver);
				}
			}
		}
		if(CollectionUtils.isNotEmpty(ccArray)){
			List<String> ccList = ccArray.toJavaList(String.class);
			for(String cc : ccList){
				String[] split = cc.split("#");
				if(NotifyRecipientType.getNotifyRecipientType(split[0]) != null) {
					NotifyJobReceiverVo receiver = new NotifyJobReceiverVo();
					receiver.setNotifyJobId(job.getId());
					receiver.setReceiver(split[1]);
					receiver.setType(split[0]);
					receiver.setReceiveType(INotifyHandler.RecipientType.CC.getValue());
					ccVoList.add(receiver);
				}
			}
		}
	}


}

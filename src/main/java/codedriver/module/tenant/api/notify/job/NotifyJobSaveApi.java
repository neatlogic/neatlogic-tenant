package codedriver.module.tenant.api.notify.job;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
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
import codedriver.framework.notify.exception.NotifyJobNameRepeatException;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.scheduler.core.SchedulerManager;
import codedriver.framework.scheduler.exception.ScheduleIllegalParameterException;
import codedriver.module.tenant.auth.label.NOTIFY_JOB_MODIFY;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
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
			@Param(name = "name", type = ApiParamType.REGEX, rule = "^[A-Za-z_\\d\\u4e00-\\u9fa5]+$", isRequired = true, xss = true, desc = "名称"),
			@Param(name = "handler", type = ApiParamType.STRING, isRequired = true,desc = "插件"),
			@Param(name = "config", type = ApiParamType.JSONOBJECT, desc = "插件自带参数"),
			@Param(name = "notifyHandler", type = ApiParamType.STRING, isRequired = true, desc = "通知方式插件"),
			@Param(name = "toList", type = ApiParamType.JSONARRAY, desc = "收件人列表，格式[\"user#userUuid\",\"team#teamUuid\",\"role#roleUuid\",\"processUserType#major\",\"custom#123@qq.com\"]"),
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

		/**
		 * 组装接收人与抄送人
		 */
		JSONArray toArray = jsonObj.getJSONArray("toList");
		JSONArray ccArray = jsonObj.getJSONArray("ccList");
		List<NotifyJobReceiverVo> toVoList = new ArrayList<>();
		List<NotifyJobReceiverVo> ccVoList = new ArrayList<>();
		getReceiverList(job, toArray,ccArray, toVoList,ccVoList);

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

		//TODO 启动或停止定时任务

		return job.getId();
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

package codedriver.framework.tenant.api.scheduler;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Example;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.framework.scheduler.core.SchedulerManager;
import codedriver.framework.scheduler.dto.JobClassVo;
import codedriver.framework.scheduler.dto.JobVo;
import codedriver.framework.scheduler.exception.ScheduleJobClassNotFoundException;
import codedriver.framework.scheduler.service.SchedulerService;
@Service
@AuthAction(name="SYSTEM_JOB_EDIT")
public class JobSearchApi extends ApiComponentBase {

	@Autowired
	private SchedulerService schedulerService;
	
	@Override
	public String getToken() {
		return "job/search";
	}

	@Override
	public String getName() {
		return "查询定时作业列表";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name="currentPage",type=ApiParamType.INTEGER,isRequired=false,desc="当前页码"),
		@Param(name="pageSize",type=ApiParamType.INTEGER,isRequired=false,desc="页大小"),
		@Param(name="keyword",type=ApiParamType.STRING,isRequired=false,desc="定时作业名称(支持模糊查询)"),
		@Param(name="classpath",type=ApiParamType.STRING,isRequired=false,desc="定时作业组件classpath")
		})
	@Description(desc="查询定时作业列表")
	@Example(example="{\"name\":\"自动评分job\", \"classpath\":\"codedriver.framework.scheduler.core.TestJob\"}")
	@Output({
		@Param(name="currentPage",type=ApiParamType.INTEGER,isRequired=true,desc="当前页码"),
		@Param(name="pageSize",type=ApiParamType.INTEGER,isRequired=true,desc="页大小"),
		@Param(name="pageCount",type=ApiParamType.INTEGER,isRequired=true,desc="总页数"),
		@Param(name="rowNum",type=ApiParamType.INTEGER,isRequired=true,desc="总行数"),
		@Param(name="jobList",type=ApiParamType.JSONARRAY,isRequired=true,desc="定时作业列表"),
		@Param(name="jobList[0].uuid",type=ApiParamType.LONG,isRequired=true,desc="定时作业id"),
		@Param(name="jobList[0].name",type=ApiParamType.STRING,isRequired=true,desc="定时作业名称"),
		@Param(name="jobList[0].classpath",type=ApiParamType.STRING,isRequired=true,desc="定时作业组件类路径"),
		@Param(name="jobList[0].jobClassName",type=ApiParamType.STRING,isRequired=true,desc="定时作业组件名称"),
		@Param(name="jobList[0].beginTime",type=ApiParamType.LONG,isRequired=false,desc="开始时间"),
		@Param(name="jobList[0].endTime",type=ApiParamType.LONG,isRequired=false,desc="结束时间"),
		@Param(name="jobList[0].cron",type=ApiParamType.STRING,isRequired=false,desc="cron表达式"),
		@Param(name="jobList[0].isActive",type=ApiParamType.STRING,isRequired=true,desc="是否激活(no:禁用，yes：激活)"),
		@Param(name="jobList[0].needAudit",type=ApiParamType.STRING,isRequired=true,desc="是否保存执行记录(no:不保存，yes:保存)"),
		@Param(name="jobList[0].jobStatus.status",type=ApiParamType.STRING,isRequired=false,desc="running:运行中;stop:停止;not_loaded未加载"),
		@Param(name="jobList[0].jobStatus.nextFireTime",type=ApiParamType.LONG,isRequired=false,desc="下一次被唤醒时间"),
		@Param(name="jobList[0].jobStatus.lastFireTime",type=ApiParamType.LONG,isRequired=false,desc="最后一次被唤醒时间"),
		@Param(name="jobList[0].jobStatus.lastFinishTime",type=ApiParamType.LONG,isRequired=false,desc="最后一次完成时间"),
		@Param(name="jobList[0].jobStatus.execCount",type=ApiParamType.INTEGER,isRequired=false,desc="执行次数"),
		@Param(name="jobList[0].propList",type=ApiParamType.JSONARRAY,isRequired=false,desc="属性列表"),
		@Param(name="jobList[0].propList[0].id",type=ApiParamType.LONG,isRequired=true,desc="属性id"),
		@Param(name="jobList[0].propList[0].name",type=ApiParamType.STRING,isRequired=true,desc="属性名"),
		@Param(name="jobList[0].propList[0].value",type=ApiParamType.STRING,isRequired=true,desc="属性值")
		})
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		//判断定时作业组件是否存在
		if(jsonObj.containsKey("classpath")) {
			String classpath = jsonObj.getString("classpath");
			JobClassVo jobClass = SchedulerManager.getJobClassByClasspath(classpath);
			if(jobClass == null) {
				throw new ScheduleJobClassNotFoundException(classpath);
			}
		}
		
		JobVo jobVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<JobVo>() {});	
		List<JobVo> jobList = schedulerService.searchJobList(jobVo);
		JSONObject resultObj = new JSONObject();
		resultObj.put("jobList", jobList);
		resultObj.put("currentPage",jobVo.getCurrentPage());
		resultObj.put("pageSize",jobVo.getPageSize());
		resultObj.put("pageCount", jobVo.getPageCount());
		resultObj.put("rowNum", jobVo.getRowNum());
		return resultObj;
	}

}

package codedriver.framework.tenant.api.scheduler;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.common.AuthAction;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Example;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.framework.scheduler.dto.JobVo;
import codedriver.framework.scheduler.service.SchedulerService;
@Service
@Transactional
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
		@Param(name="name",type=ApiParamType.STRING,isRequired=false,desc="定时作业名称(支持模糊查询)"),
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
		@Param(name="jobList[0].id",type=ApiParamType.LONG,isRequired=true,desc="定时作业id"),
		@Param(name="jobList[0].name",type=ApiParamType.STRING,isRequired=true,desc="定时作业名称"),
		@Param(name="jobList[0].classpath",type=ApiParamType.STRING,isRequired=true,desc="定时作业组件类路径"),
		@Param(name="jobList[0].beginTime",type=ApiParamType.LONG,isRequired=false,desc="开始时间"),
		@Param(name="jobList[0].endTime",type=ApiParamType.LONG,isRequired=false,desc="结束时间"),
		@Param(name="jobList[0].triggerType",type=ApiParamType.STRING,isRequired=true,desc="触发器类型"),
		@Param(name="jobList[0].repeat",type=ApiParamType.INTEGER,isRequired=false,desc="重复次数"),
		@Param(name="jobList[0].interval",type=ApiParamType.INTEGER,isRequired=false,desc="间隔时间，单位是秒"),
		@Param(name="jobList[0].cron",type=ApiParamType.STRING,isRequired=false,desc="cron表达式"),
		@Param(name="jobList[0].status",type=ApiParamType.STRING,isRequired=true,desc="running:运行中;stop:停止;not_loaded未加载"),
		@Param(name="jobList[0].nextFireTime",type=ApiParamType.LONG,isRequired=false,desc="下一次被唤醒时间"),
		@Param(name="jobList[0].lastFireTime",type=ApiParamType.LONG,isRequired=false,desc="最后一次被唤醒时间"),
		@Param(name="jobList[0].lastFinishTime",type=ApiParamType.LONG,isRequired=false,desc="最后一次完成时间"),
		@Param(name="jobList[0].execCount",type=ApiParamType.INTEGER,isRequired=false,desc="执行次数"),
		@Param(name="jobList[0].isActive",type=ApiParamType.STRING,isRequired=true,desc="是否激活(no:禁用，yes：激活)"),
		@Param(name="jobList[0].needAudit",type=ApiParamType.STRING,isRequired=true,desc="是否保存执行记录(no:不保存，yes:保存)"),
		@Param(name="jobList[0].serverId",type=ApiParamType.INTEGER,isRequired=true,desc="server_id，一台主机一个serverid"),
		@Param(name="jobList[0].propList",type=ApiParamType.JSONARRAY,isRequired=false,desc="属性列表"),
		@Param(name="jobList[0].propList[0].id",type=ApiParamType.LONG,isRequired=true,desc="属性id"),
		@Param(name="jobList[0].propList[0].name",type=ApiParamType.STRING,isRequired=true,desc="属性名"),
		@Param(name="jobList[0].propList[0].value",type=ApiParamType.STRING,isRequired=true,desc="属性值")
		})
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JobVo jobVo = new JobVo();
		if(jsonObj.containsKey("name")) {
			String name = jsonObj.getString("name");
			jobVo.setName(name);
		}
		if(jsonObj.containsKey("classpath")) {
			String classpath = jsonObj.getString("classpath");
			jobVo.setClasspath(classpath);
		}
		if(jsonObj.containsKey("currentPage")) {
			Integer currentPage = jsonObj.getInteger("currentPage");
			jobVo.setCurrentPage(currentPage);
		}
		if(jsonObj.containsKey("pageSize")) {
			Integer pageSize = jsonObj.getInteger("pageSize");
			jobVo.setPageSize(pageSize);
		}
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

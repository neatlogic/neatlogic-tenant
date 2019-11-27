package codedriver.framework.tenant.api.scheduler;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.common.AuthAction;
import codedriver.framework.common.config.Config;
import codedriver.framework.exception.ApiRuntimeException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Example;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.framework.scheduler.dao.mapper.SchedulerMapper;
import codedriver.framework.scheduler.dto.JobVo;
import codedriver.framework.scheduler.dto.ServerNewJobVo;
import codedriver.framework.scheduler.exception.SchedulerExceptionMessage;
import codedriver.framework.scheduler.service.SchedulerService;
import codedriver.framework.server.dao.mapper.ServerMapper;
import codedriver.framework.server.dto.ServerClusterVo;
@Service
@AuthAction(name="SYSTEM_JOB_EDIT")
public class JobResumeApi extends ApiComponentBase {

	Logger logger = LoggerFactory.getLogger(JobResumeApi.class);
	
	@Autowired
	private SchedulerService schedulerService;
	@Autowired 
	private ServerMapper serverMapper;
	@Autowired
	private SchedulerMapper schedulerMapper;
	
	@Override
	public String getToken() {
		return "job/resume";
	}

	@Override
	public String getName() {
		return "恢复定时作业";
	}

	@Override
	public String getConfig() {
		return null;
	}
	
	@Input({@Param(name="jobId",type="Long",isRequired="true",desc="定时作业id")})
	@Description(desc="恢复定时作业")
	@Example(example="{\"jobId\":1}")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {		
		String jobUuid = jsonObj.getString("jobUuid");
		JobVo job = schedulerMapper.getJobByUuid(jobUuid);
		if(job == null) {
			SchedulerExceptionMessage message = new SchedulerExceptionMessage("定时作业："+ jobUuid + " 不存在");
			logger.error(message.toString());
			throw new ApiRuntimeException(message);
		}		
		schedulerService.loadJob(job);
		TenantContext tenant = TenantContext.get();
		String tenantUuid = tenant.getTenantUuid();
		tenant.setUseDefaultDatasource(true);
		List<ServerClusterVo> serverList = serverMapper.getServerByStatus(ServerClusterVo.STARTUP);
		for(ServerClusterVo server : serverList) {
			int serverId = server.getServerId();
			if(Config.SCHEDULE_SERVER_ID == serverId) {
				continue;
			}
			schedulerMapper.insertServerNewJob(new ServerNewJobVo(serverId, job.getUuid(), tenantUuid));
		}
		return "OK";
	}

}

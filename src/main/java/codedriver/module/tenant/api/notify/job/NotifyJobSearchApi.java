package codedriver.module.tenant.api.notify.job;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.BaseEditorVo;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.notify.dao.mapper.NotifyJobMapper;
import codedriver.framework.notify.dto.job.NotifyJobVo;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.scheduler.dao.mapper.SchedulerMapper;
import codedriver.framework.scheduler.dto.JobAuditVo;
import com.alibaba.fastjson.JSON;
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
		//TODO 查询收件人
		if(CollectionUtils.isNotEmpty(jobList)){
			for(NotifyJobVo job : jobList){
				JobAuditVo jobAuditVo = new JobAuditVo();
				jobAuditVo.setJobUuid(job.getId().toString());
				job.setExecCount(schedulerMapper.searchJobAuditCount(jobAuditVo));
			}
		}
		returnObj.put("tbodyList",jobList);
		return returnObj;
	}
}

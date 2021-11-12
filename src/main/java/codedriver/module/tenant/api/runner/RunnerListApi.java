package codedriver.module.tenant.api.runner;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.auth.label.RUNNER_MODIFY;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.dao.mapper.runner.RunnerMapper;
import codedriver.framework.dto.runner.RunnerVo;
import codedriver.framework.exception.runner.RunnerGroupIdNotFoundException;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AuthAction(action = RUNNER_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class RunnerListApi extends PrivateApiComponentBase {

    @Resource
    RunnerMapper runnerMapper;


    @Override
    public String getName() {
        return "查询未关联的runner列表";
    }

    @Override
    public String getToken() {
        return "runner/list";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "groupId", type = ApiParamType.LONG, desc = "runner组id"),
            @Param(name = "keyword", type = ApiParamType.STRING, desc = "关键字（ip或者名称）"),
            @Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否分页"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页数"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页条数")
    })
    @Output({
            @Param(name = "tbodyList", desc = "runner列表")
    })
    @Description(desc = "用于创建和编辑runner组的时候，供选择做关联的runner列表")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {

        Long groupId = paramObj.getLong("groupId");
        RunnerVo runnerVo = JSONObject.toJavaObject(paramObj, RunnerVo.class);
        JSONObject resultObj = new JSONObject();
        List<RunnerVo> allVoList = runnerMapper.searchRunner(new RunnerVo());

        //新增runner组时供选择的全部的runner列表
        RunnerVo runnerVoTmp = new RunnerVo();
        runnerVoTmp.setNeedPage(runnerVo.getNeedPage());
        runnerVoTmp.setStartNum(runnerVo.getStartNum());
        runnerVoTmp.setPageSize(runnerVo.getPageSize());
        runnerVoTmp.setKeyword(runnerVo.getKeyword());
        List<RunnerVo> runnerVoList = runnerMapper.searchRunner(runnerVoTmp);
        int rowNum = runnerMapper.searchRunnerCount(runnerVo);

        //编辑runner组时供选择的过滤的runner列表
        if (groupId != null) {
            if (runnerMapper.checkRunnerGroupIdIsExist(groupId) == 0) {
                throw new RunnerGroupIdNotFoundException(groupId);
            }
            List<Long> runnerIdList = allVoList.stream().map(RunnerVo::getId).collect(Collectors.toList());
            List<RunnerVo> groupRunnerlist = runnerMapper.searchRunner(runnerVo);
            if (CollectionUtils.isNotEmpty(groupRunnerlist)) {
                runnerIdList.removeAll(groupRunnerlist.stream().map(RunnerVo::getId).collect(Collectors.toList()));
            }
            runnerVoList = runnerMapper.searchRunnerVoListByIdList(runnerIdList, runnerVo);
            rowNum = runnerMapper.searchRunner(new RunnerVo()).size() - runnerMapper.searchRunnerCountByGroupId(groupId);

        }

        resultObj.put("runnerVoList", runnerVoList);
        resultObj.put("rowNum", rowNum);
        resultObj.put("pageCount", PageUtil.getPageCount(rowNum, runnerVo.getPageSize()));
        resultObj.put("currentPage", runnerVo.getCurrentPage());
        resultObj.put("pageSize", runnerVo.getPageSize());
        return resultObj;
    }
}

package codedriver.module.tenant.api.runner;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.auth.label.RUNNER_MODIFY;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.dao.mapper.runner.RunnerMapper;
import codedriver.framework.dto.runner.RunnerGroupVo;
import codedriver.framework.dto.runner.RunnerVo;
import codedriver.framework.exception.runner.RunnerGroupIdNotFoundException;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
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
            @Param(name = "id", type = ApiParamType.LONG, desc = "runner组id"),
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

        Long id = paramObj.getLong("id");
        RunnerGroupVo runnerGroupVo = JSONObject.toJavaObject(paramObj, RunnerGroupVo.class);
        JSONObject resultObj = new JSONObject();
        List<RunnerVo> allVoList = runnerMapper.searchRunner(new RunnerVo());

        //新增runner组时供选择的全部的runner列表
        RunnerVo runnerVoTmp = new RunnerVo();
        runnerVoTmp.setNeedPage(runnerGroupVo.getNeedPage());
        runnerVoTmp.setStartNum(runnerGroupVo.getStartNum());
        runnerVoTmp.setPageSize(runnerGroupVo.getPageSize());
        runnerVoTmp.setKeyword(runnerGroupVo.getKeyword());
        List<RunnerVo> runnerVoList = runnerMapper.searchRunner(runnerVoTmp);
        int rowNum = runnerMapper.searchRunnerCount(runnerVoTmp);

        //编辑runner组时供选择的过滤的runner列表
        if (id != null) {
            if (runnerMapper.checkRunnerGroupIdIsExist(id) == 0) {
                throw new RunnerGroupIdNotFoundException(id);
            }
            runnerVoTmp.setGroupId(id);
            //当前组的runnerList
            List<RunnerVo> groupRunnerlist = runnerMapper.searchRunner(runnerVoTmp);
            //过滤后的runnerList
            List<RunnerVo> runnerList = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(groupRunnerlist)) {
                runnerList = allVoList.stream().filter(s -> !groupRunnerlist.stream().map(RunnerVo::getId).collect(Collectors.toList()).contains(s.getId())).collect(Collectors.toList());
            }
            runnerGroupVo.setRunnerList(runnerList);
            runnerVoList = runnerMapper.searchRunnerListByGroupVo(runnerGroupVo);
            rowNum = runnerMapper.searchRunner(new RunnerVo()).size() - runnerMapper.searchRunnerCountByGroupId(id);

        }

        resultObj.put("runnerVoList", runnerVoList);
        resultObj.put("rowNum", rowNum);
        resultObj.put("pageCount", PageUtil.getPageCount(rowNum, runnerVoTmp.getPageSize()));
        resultObj.put("currentPage", runnerVoTmp.getCurrentPage());
        resultObj.put("pageSize", runnerVoTmp.getPageSize());
        return resultObj;
    }
}

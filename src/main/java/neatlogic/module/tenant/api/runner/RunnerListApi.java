package neatlogic.module.tenant.api.runner;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.RUNNER_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dao.mapper.runner.RunnerMapper;
import neatlogic.framework.dto.runner.RunnerVo;
import neatlogic.framework.exception.runner.RunnerGroupIdNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.TableResultUtil;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

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
            @Param(name = "isFilterGroup", type = ApiParamType.INTEGER, desc = "是否过滤runner组"),
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

        RunnerVo runnerVo = JSONObject.toJavaObject(paramObj, RunnerVo.class);
        Long groupId = paramObj.getLong("groupId");

        if (groupId != null) {
            if (runnerMapper.checkRunnerGroupIdIsExist(groupId) == 0) {
                throw new RunnerGroupIdNotFoundException(groupId);
            }
        }
        runnerVo.setIsDelete(0);
        int rowNum = runnerMapper.searchRunnerCount(runnerVo);
        runnerVo.setRowNum(rowNum);
        return TableResultUtil.getResult(runnerMapper.searchRunner(runnerVo), runnerVo);
    }
}

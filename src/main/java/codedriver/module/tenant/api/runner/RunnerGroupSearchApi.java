/*
 * Copyright (c)  2021 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.runner;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.auth.label.RUNNER_MODIFY;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dao.mapper.runner.RunnerMapper;
import codedriver.framework.dto.runner.RunnerGroupVo;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.util.TableResultUtil;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
@AuthAction(action = RUNNER_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class RunnerGroupSearchApi extends PrivateApiComponentBase {

    @Resource
    RunnerMapper runnerMapper;

    @Override
    public String getName() {
        return "查询runner组信息";
    }

    @Override
    public String getToken() {
        return "runnergroup/search";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Description(desc = "获取runner组列表")
    @Input({
            @Param(name = "keyword", type = ApiParamType.STRING, desc = "关键词"),
            @Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否分页"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页数"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页条数")
    })
    @Output({
            @Param(name = "tbodyList", explode = RunnerGroupVo[].class, desc = "所有tagent runner组")
    })
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        RunnerGroupVo groupVo = JSONObject.toJavaObject(paramObj, RunnerGroupVo.class);
        int rowNum = runnerMapper.searchRunnerGroupCount(groupVo);
        List<RunnerGroupVo> runnerGroupVoList = new ArrayList<>();
        groupVo.setRowNum(rowNum);
        if (rowNum > 0) {
            runnerGroupVoList = runnerMapper.searchRunnerGroup(groupVo);
        }
        return TableResultUtil.getResult(runnerGroupVoList, groupVo);
    }


}

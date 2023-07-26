/*
 * Copyright (c)  2021 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.runner;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.auth.label.RUNNER_MODIFY;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dao.mapper.runner.RunnerMapper;
import codedriver.framework.dto.runner.RunnerVo;
import codedriver.framework.exception.runner.RunnerIdNotFoundException;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@Transactional
@AuthAction(action = RUNNER_MODIFY.class)
@OperationType(type = OperationTypeEnum.DELETE)
public class RunnerDeleteApi extends PrivateApiComponentBase {

    @Resource
    RunnerMapper runnerMapper;

    @Override
    public String getName() {
        return "删除runner";
    }

    @Override
    public String getToken() {
        return "runner/delete";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "id", type = ApiParamType.LONG, isRequired = true, desc = "runner id")
    })
    @Output({
    })
    @Description(desc = "用于runner管理页面，runner删除接口")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {

        Long id = paramObj.getLong("id");
        if (runnerMapper.checkRunnerIdIsExist(id) == 0) {
            throw new RunnerIdNotFoundException(id);
        }

        RunnerVo runnerVo = runnerMapper.getRunnerById(id);
        runnerVo.setIsDelete(1);
        runnerMapper.updateRunner(runnerVo);
        runnerMapper.deleteRunnerGroupRunnerByRunnerId(id);
        return null;
    }

}
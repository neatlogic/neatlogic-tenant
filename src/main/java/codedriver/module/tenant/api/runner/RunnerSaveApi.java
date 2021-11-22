/*
 * Copyright (c)  2021 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.runner;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.auth.label.RUNNER_MODIFY;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dao.mapper.runner.RunnerMapper;
import codedriver.framework.dto.runner.RunnerAuthVo;
import codedriver.framework.dto.runner.RunnerVo;
import codedriver.framework.exception.runner.RunnerIdNotFoundException;
import codedriver.framework.exception.runner.RunnerIsExistException;
import codedriver.framework.exception.runner.RunnerNameRepeatsException;
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
@OperationType(type = OperationTypeEnum.OPERATE)
public class RunnerSaveApi extends PrivateApiComponentBase {

    @Resource
    RunnerMapper runnerMapper;

    @Override
    public String getName() {
        return "保存runner";
    }

    @Override
    public String getToken() {
        return "runner/save";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "id", type = ApiParamType.LONG, isRequired = false, desc = "runner id"),
            @Param(name = "name", type = ApiParamType.STRING, isRequired = true, desc = "runner 名"),
            @Param(name = "protocol", type = ApiParamType.ENUM, isRequired = true, rule = "http,https", desc = "协议"),
            @Param(name = "host", type = ApiParamType.STRING, desc = "runner ip"),
            @Param(name = "nettyPort", type = ApiParamType.INTEGER, desc = "心跳端口"),
            @Param(name = "port", type = ApiParamType.INTEGER, desc = "命令端口"),
            @Param(name = "isAuth", type = ApiParamType.INTEGER, desc = "是否认证"),
            @Param(name = "runnerAuthList", explode = RunnerAuthVo.class, type = ApiParamType.JSONARRAY, desc = "runner外部认证信息"),
    })
    @Output({
    })
    @Description(desc = "runner 保存接口,直接由ip确定一个runner")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        RunnerVo runnerVo = JSONObject.toJavaObject(paramObj, RunnerVo.class);
        Long id = paramObj.getLong("id");

        RunnerVo runnerNameTmp = new RunnerVo();
        RunnerVo runnerIpTmp = new RunnerVo();
        if (runnerMapper.checkRunnerNameIsExist(runnerVo) > 0) {
            runnerNameTmp = runnerMapper.getRunnerByName(runnerVo.getName());
            if (runnerNameTmp.getIsDelete() != null && runnerNameTmp != null) {
                if (runnerNameTmp.getIsDelete() == 0) {
                    throw new RunnerNameRepeatsException(runnerVo.getName());
                }
            }
        }

        if (runnerMapper.checkRunnerIsExistByIdAndIp(runnerVo.getId(), runnerVo.getHost()) > 0) {
            runnerIpTmp = runnerMapper.getRunnerByIp(runnerVo.getHost());
            if (runnerIpTmp != null) {
                if (runnerIpTmp.getIsDelete() != null&&runnerIpTmp.getIsDelete() == 0) {
                    throw new RunnerIsExistException(runnerVo.getHost());
                }
            }
        }

        if (id != null) {
            if (runnerMapper.checkRunnerIdIsExist(id) == 0) {
                throw new RunnerIdNotFoundException(id);
            }
            runnerMapper.replaceRunner(runnerVo);
        } else {
            if (runnerIpTmp != null && runnerIpTmp.getIsDelete() != null && runnerIpTmp.getIsDelete() == 1) {
                runnerVo.setId(runnerIpTmp.getId());
                if ((runnerVo.getName()).equals(runnerNameTmp.getName())) {
                    runnerMapper.deleteRuunerByName(runnerVo.getName());
                }
            } else if (runnerNameTmp != null && runnerNameTmp.getIsDelete() != null&& runnerNameTmp.getIsDelete() == 1) {
                runnerVo.setId(runnerNameTmp.getId());
            }

            runnerMapper.replaceRunner(runnerVo);

        }
        return null;
    }
}

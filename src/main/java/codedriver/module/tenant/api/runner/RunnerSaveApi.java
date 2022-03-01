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
import codedriver.framework.exception.runner.RunnerIpIsExistException;
import codedriver.framework.exception.runner.RunnerNameRepeatsException;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Objects;

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
        RunnerVo searchVo = JSONObject.toJavaObject(paramObj, RunnerVo.class);
        Long id = paramObj.getLong("id");
        RunnerVo runnerVo = null;

        RunnerVo oldNameRunner = runnerMapper.getRunnerByName(searchVo.getName());
        //下列情景抛异常：
        //情景一：新增runner时，已存在（使用中）同名runner
        //情景二：修改runner时，已存在（使用中）并且id不相等的同名的runner
        if (oldNameRunner != null && Objects.equals(oldNameRunner.getIsDelete(), 0) && (id == null || !Objects.equals(oldNameRunner.getId(), id))) {
            throw new RunnerNameRepeatsException(searchVo.getName());
        }

        RunnerVo oldIpRunner = runnerMapper.getRunnerByIp(searchVo.getHost());
        //下列情景抛异常：
        //情景一：新增runner时，已存在（使用中）同ip的runner
        //情景二：修改runner时，已存在（使用中）并且id不相等的同ip的runner
        if (oldIpRunner != null && Objects.equals(oldIpRunner.getIsDelete(), 0) && ((id == null || !Objects.equals(oldIpRunner.getId(), id)))) {
            throw new RunnerIpIsExistException(searchVo.getHost());
        }

        //再次编辑
        if (id != null) {
            if (runnerMapper.checkRunnerIdIsExist(id) == 0) {
                throw new RunnerIdNotFoundException(id);
            }
            runnerVo = searchVo;
            if (oldIpRunner != null) {
                //情景：修改runner1的ip改为2.2.2.2
                //      runner1  ip为1.1.1.1 name为 1 （使用中）
                //      runner2  ip为2.2.2.2 name为 2 （已删除）
                //需要删除runner2，并使用原来runner2的id
                runnerVo.setId(oldIpRunner.getId());
                if (Objects.equals(oldIpRunner.getIsDelete(), 1)) {
                    runnerMapper.deleteRunnerById(id);
                }
                //情景：修改runner1的ip改为2.2.2.2 name为2
                //      runner1  ip为1.1.1.1 name为 1 （使用中）
                //      runner2  ip为2.2.2.2 name为 2 （已删除）
                //需要删除runner2，并使用原来runner2的id
                if (oldIpRunner.equals(oldNameRunner) && !Objects.equals(id, oldIpRunner.getId())) {
                    runnerMapper.deleteRunnerById(id);
                }
            }
            if (oldNameRunner != null) {
                //情景：修改runner1的name改为2
                //      runner1  ip为1.1.1.1 name为 1 （使用中）
                //      runner2  ip为2.2.2.2 name为 2 （已删除）
                //需要删除runner2，继续使用原来runner1的id
                if (Objects.equals(oldNameRunner.getIsDelete(), 1)) {
                    runnerMapper.deleteRunnerById(oldNameRunner.getId());
                }
            }
        }
        //新增ip相同，覆盖，id不变
        if (runnerVo == null && oldIpRunner != null) {
            searchVo.setId(oldIpRunner.getId());
            runnerVo = searchVo;
            //情景：新增runner3 ip为1.1.1.1 name为 2
            //      runner1  ip为1.1.1.1 name为 1 （已删除）
            //      runner2  ip为2.2.2.2 name为 2 （已删除）
            //需要删除runner2，重新启用runner1,id不变
            if (oldNameRunner != null && oldNameRunner.getIsDelete() == 1) {
                runnerMapper.deleteRunnerById(oldNameRunner.getId());
            }
        }
        //新增name相同，覆盖，id不变
        if (runnerVo == null && oldNameRunner != null) {
            searchVo.setId(oldNameRunner.getId());
            runnerVo = searchVo;
        }
        //新增runner
        if (runnerVo == null) {
            runnerVo = searchVo;
        }
        runnerMapper.replaceRunner(runnerVo);
        return null;
    }
}

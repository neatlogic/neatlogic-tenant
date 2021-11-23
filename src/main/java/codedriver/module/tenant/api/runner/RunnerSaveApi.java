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
        RunnerVo oldIpRunner = runnerMapper.getRunnerByIp(searchVo.getHost());

        if (oldNameRunner != null && ((id == null && Objects.equals(oldNameRunner.getIsDelete(), 0)) || (id != null && !Objects.equals(oldNameRunner.getId(), id)))) {
            throw new RunnerNameRepeatsException(searchVo.getName());
        }
        if (oldIpRunner != null && ((id == null && Objects.equals(oldIpRunner.getIsDelete(), 0)) || (id != null && !Objects.equals(oldIpRunner.getId(), id)))) {
            throw new RunnerIpIsExistException(searchVo.getHost());
        }

        //再次编辑
        if (id != null) {
            if (runnerMapper.checkRunnerIdIsExist(id) == 0) {
                throw new RunnerIdNotFoundException(id);
            }
            runnerVo = searchVo;
        }
        //ip相同，覆盖ip的runner，id不变，若发现当前runner的name已存在库里且isDelete为1，则删除name相同的runner
        if (runnerVo == null && oldIpRunner != null) {
            searchVo.setId(oldIpRunner.getId());
            runnerVo = searchVo;
            if (oldNameRunner != null && oldNameRunner.getIsDelete() == 1) {
                runnerMapper.deleteRuunerByName(searchVo.getName());
            }
        }
        //name相同，覆盖，id不变
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

/*
 * Copyright (c)  2021 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.runner;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.auth.label.RUNNER_MODIFY;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.util.IpUtil;
import codedriver.framework.dao.mapper.runner.RunnerMapper;
import codedriver.framework.dto.runner.GroupNetworkVo;
import codedriver.framework.dto.runner.RunnerGroupVo;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.exception.runner.*;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;

@Service
@Transactional
@AuthAction(action = RUNNER_MODIFY.class)
@OperationType(type = OperationTypeEnum.OPERATE)
public class RunnerGroupSaveApi extends PrivateApiComponentBase {

    @Resource
    RunnerMapper runnerMapper;

    @Override
    public String getName() {
        return "runner组保存接口";
    }

    @Override
    public String getToken() {
        return "runnergroup/save";
    }

    @Override
    public String getConfig() {
        return null;
    }


    @Input({
            @Param(name = "id", type = ApiParamType.LONG, isRequired = false, desc = "id"),
            @Param(name = "name", type = ApiParamType.STRING, isRequired = true, desc = "runner 分组名"),
            @Param(name = "description", type = ApiParamType.STRING, isRequired = false, desc = "描述"),
            @Param(name = "groupNetworkList", type = ApiParamType.JSONARRAY, isRequired = false, desc = "runner 分组网段列表"),
    })
    @Output({
    })
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        RunnerGroupVo runnerGroupVo = JSONObject.toJavaObject(paramObj, RunnerGroupVo.class);
        Long id = paramObj.getLong("id");
        String name = paramObj.getString("name");
        List<GroupNetworkVo> groupNetworkList = runnerGroupVo.getGroupNetworkList();
        if (runnerMapper.checkGroupNameIsRepeats(runnerGroupVo) > 0) {
            throw new RunnerGroupNetworkNameRepeatsException(name);
        }
        if (id != null) {
            if (runnerMapper.checkRunnerGroupIdIsExist(id) == 0) {
                throw new RunnerGroupIdNotFoundException(id);
            }
            if (!CollectionUtils.isEmpty(groupNetworkList)) {
                String checkIpMask = StringUtils.EMPTY;
                for (int i = 0; i < groupNetworkList.size(); i++) {
                    String ip = groupNetworkList.get(i).getNetworkIp();
                    Integer mask = groupNetworkList.get(i).getMask();
                    if (!IpUtil.checkIp(ip) || StringUtils.isBlank(ip)) {
                        throw new IPIsIncorrectException(ip);
                    }
                    if (mask == null || !IpUtil.checkMask(mask)) {
                        throw new MaskIsIncorrectException(ip);
                    }
                    if (i == 0) {
                        checkIpMask = ip + ":" + mask;
                        continue;
                    }
                    if (checkIpMask.equals(ip + ":" + mask)) {
                        throw new RunnerGroupNetworkSameException(checkIpMask);//TODO 前端提示不准确，192.168.0.0/24和192.168.0.1/24实际上是同一个网段
                    }
                }
            }
            runnerMapper.updateRunnerGroup(runnerGroupVo);
        } else {
            runnerMapper.insertRunnerGroup(runnerGroupVo);
        }
        runnerMapper.deleteGroupNetWork(runnerGroupVo.getId());
        if (groupNetworkList != null && groupNetworkList.size() > 0) {
            for (GroupNetworkVo networkVo : groupNetworkList) {
                networkVo.setGroupId(runnerGroupVo.getId());
                runnerMapper.insertNetwork(networkVo);
            }
        }


        return null;
    }


}

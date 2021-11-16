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
import codedriver.framework.dto.runner.RunnerVo;
import codedriver.framework.exception.runner.*;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@AuthAction(action = RUNNER_MODIFY.class)
@OperationType(type = OperationTypeEnum.OPERATE)
public class RunnerGroupSaveApi extends PrivateApiComponentBase {

    @Resource
    RunnerMapper runnerMapper;

    @Override
    public String getName() {
        return "保存runner组";
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
            @Param(name = "groupNetworkList", type = ApiParamType.JSONARRAY, isRequired = false, desc = "runner组 网段列表"),
            @Param(name = "runnerList", type = ApiParamType.JSONARRAY, isRequired = false, desc = "关联的runner列表"),
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
        if (!CollectionUtils.isEmpty(groupNetworkList)) {

            List<String> iPMaskList = new ArrayList<>();
            for (int i = 0; i < groupNetworkList.size(); i++) {
                String ip = groupNetworkList.get(i).getNetworkIp();
                Integer mask = groupNetworkList.get(i).getMask();
                if (!IpUtil.checkIp(ip) || StringUtils.isBlank(ip)) {
                    throw new IPIsIncorrectException(ip);
                }
                if (mask == null || !IpUtil.checkMask(mask)) {
                    throw new MaskIsIncorrectException(ip);
                }
                iPMaskList.add(ip + ":" + mask);
            }

            Set<String> ipMaskSet = iPMaskList.stream().collect(Collectors.toSet());
            if (iPMaskList.size() != ipMaskSet.size()) {
                throw new RunnerGroupNetworkSameException();//TODO 前端提示不准确，192.168.0.0/24和192.168.0.1/24实际上是同一个网段
            }

        }
        if (id != null) {
            if (runnerMapper.checkRunnerGroupIdIsExist(id) == 0) {
                throw new RunnerGroupIdNotFoundException(id);
            }
            runnerMapper.updateRunnerGroup(runnerGroupVo);
        } else {
            runnerMapper.insertRunnerGroup(runnerGroupVo);
        }

        Long groupId = runnerGroupVo.getId();
        //组网段
        runnerMapper.deleteGroupNetWork(groupId);
        if (groupNetworkList != null && groupNetworkList.size() > 0) {
            for (GroupNetworkVo networkVo : groupNetworkList) {
                networkVo.setGroupId(groupId);
                runnerMapper.insertNetwork(networkVo);
            }
        }

        //关联runner
        JSONArray runnerArray = paramObj.getJSONArray("runnerList");
        List<RunnerVo> runnerVoList = null;
        runnerMapper.deleteRunnerGroupRunnerByGroupId(id);
        if (CollectionUtils.isNotEmpty(runnerArray)) {
            runnerVoList = runnerArray.toJavaList(RunnerVo.class);
            List<Long> runnerIdList = runnerVoList.stream().map(RunnerVo::getId).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(runnerIdList)) {
                runnerMapper.insertRunnerGroupRunnerByRunnerIdListAndGroupId(runnerIdList, groupId);
            }
        }
        return null;
    }
}

/*Copyright (C) $today.year  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

package neatlogic.module.tenant.api.runner;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.RUNNER_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.util.IpUtil;
import neatlogic.framework.dao.mapper.runner.RunnerMapper;
import neatlogic.framework.dto.FieldValidResultVo;
import neatlogic.framework.dto.runner.GroupNetworkVo;
import neatlogic.framework.dto.runner.RunnerGroupVo;
import neatlogic.framework.dto.runner.RunnerVo;
import neatlogic.framework.exception.runner.*;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.IValid;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashSet;
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
        List<GroupNetworkVo> groupNetworkList = runnerGroupVo.getGroupNetworkList();

        if (runnerMapper.checkGroupNameIsRepeats(runnerGroupVo) > 0) {
            throw new RunnerGroupNetworkNameRepeatsException(runnerGroupVo.getName());
        }
            if (!CollectionUtils.isEmpty(groupNetworkList)) {

            Set<String> iPMaskSet = new HashSet<>();
            for (int i = 0; i < groupNetworkList.size(); i++) {
                String ip = groupNetworkList.get(i).getNetworkIp();
                Integer mask = groupNetworkList.get(i).getMask();
                if (!IpUtil.checkIp(ip) || StringUtils.isBlank(ip)) {
                    throw new IPIsIncorrectException(ip);
                }
                if (mask == null || !IpUtil.checkMask(mask)) {
                    throw new MaskIsIncorrectException(ip);
                }
                iPMaskSet.add(ip + ":" + mask);
            }
            if (iPMaskSet.size() != groupNetworkList.size()) {
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

    public IValid name() {
        return value -> {
            RunnerGroupVo vo = JSON.toJavaObject(value, RunnerGroupVo.class);
            if (runnerMapper.checkGroupNameIsRepeats(vo) > 0) {
                return new FieldValidResultVo(new RunnerGroupNetworkNameRepeatsException(vo.getName()));
            }
            return new FieldValidResultVo();
        };
    }
}

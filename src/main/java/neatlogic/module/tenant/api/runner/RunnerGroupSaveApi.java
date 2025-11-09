/*
 *
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 */

package neatlogic.module.tenant.api.runner;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.RUNNER_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.constvalue.TagType;
import neatlogic.framework.common.util.IpUtil;
import neatlogic.framework.dao.mapper.TagMapper;
import neatlogic.framework.dao.mapper.runner.RunnerMapper;
import neatlogic.framework.dto.FieldValidResultVo;
import neatlogic.framework.dto.TagVo;
import neatlogic.framework.dto.runner.GroupNetworkVo;
import neatlogic.framework.dto.runner.GroupTagVo;
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

    @Resource
    TagMapper tagMapper;

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
            @Param(name = "id", type = ApiParamType.LONG, desc = "id"),
            @Param(name = "name", type = ApiParamType.STRING, isRequired = true, desc = "nmtar.runnergroupsaveapi.input.param.desc.name"),
            @Param(name = "rule", type = ApiParamType.STRING, desc = "nmtar.runnergroupsaveapi.input.param.desc.rule"),
            @Param(name = "description", type = ApiParamType.STRING, desc = "common.description"),
            @Param(name = "groupNetworkList", type = ApiParamType.JSONARRAY, desc = "nmtar.runnergroupsaveapi.input.param.desc.groupnetworklist"),
            @Param(name = "runnerList", type = ApiParamType.JSONARRAY, desc = "nmtar.runnergroupsaveapi.input.param.desc.runnerlist"),
    })
    @Output({
    })
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        RunnerGroupVo runnerGroupVo = JSON.toJavaObject(paramObj, RunnerGroupVo.class);
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

        //组标签
        runnerMapper.deleteGroupTag(groupId);
        if (CollectionUtils.isNotEmpty(runnerGroupVo.getTagList())) {
            for (String tag : runnerGroupVo.getTagList()) {
                Long tagId = tagMapper.getTagIdByNameAndType(tag, TagType.RUNNERGROUP.getValue());
                if (tagId == null) {
                    TagVo newTag = new TagVo(tag, TagType.RUNNERGROUP.getValue());
                    tagMapper.insertTag(newTag);
                    tagId = newTag.getId();
                }
                tagMapper.getTagLockById(tagId);
                GroupTagVo groupTagVo = new GroupTagVo(groupId, tagId);
                runnerMapper.insertRunnerTag(groupTagVo);
            }
        }
        //删除没用的标签
        List<TagVo> noUseTagList = tagMapper.searchNoUseTag();
        for (TagVo noUseTag : noUseTagList) {
            tagMapper.getTagLockById(noUseTag.getId());
            tagMapper.deleteTagById(noUseTag.getId());
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

        //
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

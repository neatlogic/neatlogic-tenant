/*
Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package neatlogic.module.tenant.service;

import neatlogic.framework.dao.mapper.runner.RunnerMapper;
import neatlogic.framework.dto.runner.RunnerVo;
import neatlogic.framework.exception.runner.RunnerIdNotFoundException;
import neatlogic.framework.exception.runner.RunnerIpIsExistException;
import neatlogic.framework.exception.runner.RunnerNameRepeatsException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

@Service
public class RunnerServiceImpl implements RunnerService {
    @Resource
    RunnerMapper runnerMapper;

    @Override
    public void SaveRunner(RunnerVo paramRunner, Long id) {
        RunnerVo replaceRunner = null;

        RunnerVo oldNameRunner = runnerMapper.getRunnerByName(paramRunner.getName());
        //下列情景抛异常：
        //情景一：新增runner时，已存在（使用中）同名runner
        //情景二：修改runner时，已存在（使用中）并且id不相等的同名的runner
        if (oldNameRunner != null && Objects.equals(oldNameRunner.getIsDelete(), 0) && (id == null || !Objects.equals(oldNameRunner.getId(), id))) {
            throw new RunnerNameRepeatsException(paramRunner.getName());
        }

        RunnerVo oldIpRunner = runnerMapper.getRunnerByIp(paramRunner.getHost());
        //下列情景抛异常：
        //情景一：新增runner时，已存在（使用中）同ip的runner
        //情景二：修改runner时，已存在（使用中）并且id不相等的同ip的runner
        if (oldIpRunner != null && Objects.equals(oldIpRunner.getIsDelete(), 0) && ((id == null || !Objects.equals(oldIpRunner.getId(), id)))) {
            throw new RunnerIpIsExistException(paramRunner.getHost());
        }

        //再次编辑
        if (id != null) {
            if (runnerMapper.checkRunnerIdIsExist(id) == 0) {
                throw new RunnerIdNotFoundException(id);
            }
            replaceRunner = paramRunner;
            //情景：修改runner1的ip改为2.2.2.2
            //      runner1  ip为1.1.1.1 name为 1 （使用中）
            //      runner2  ip为2.2.2.2 name为 2 （已删除）
            //需要删除runner2，并使用原来runner2的id
            if (oldIpRunner != null && Objects.equals(oldIpRunner.getIsDelete(), 1)) {
                replaceRunner.setId(oldIpRunner.getId());
                runnerMapper.deleteRunnerById(id);
            }
            //情景：修改runner1的name改为2
            //      runner1  ip为1.1.1.1 name为 1 （使用中）
            //      runner2  ip为2.2.2.2 name为 2 （已删除）
            //需要删除runner2，继续使用原来runner1的id（但修改自身name时，无需进行删除操作）
            if (oldNameRunner != null && Objects.equals(oldNameRunner.getIsDelete(), 1)) {
                runnerMapper.deleteRunnerById(oldNameRunner.getId());
            }
        }
        //新增runner的ip相同时，重启之前的runner，id不变
        if (replaceRunner == null && oldIpRunner != null) {
            paramRunner.setId(oldIpRunner.getId());
            replaceRunner = paramRunner;
            //情景：新增runner3 ip为1.1.1.1 name为 2
            //      runner1  ip为1.1.1.1 name为 1 （已删除）
            //      runner2  ip为2.2.2.2 name为 2 （已删除）
            //需要删除runner2，重新启用runner1,id不变
            if (oldNameRunner != null) {
                runnerMapper.deleteRunnerById(oldNameRunner.getId());
            }
        }
        //新增runner的name相同时，重启之前的runner，id不变
        if (replaceRunner == null && oldNameRunner != null) {
            paramRunner.setId(oldNameRunner.getId());
            replaceRunner = paramRunner;
        }
        //新增runner
        if (replaceRunner == null) {
            replaceRunner = paramRunner;
        }
        runnerMapper.replaceRunner(replaceRunner);
    }
}

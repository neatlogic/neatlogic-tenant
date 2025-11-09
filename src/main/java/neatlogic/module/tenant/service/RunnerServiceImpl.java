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
package neatlogic.module.tenant.service;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.common.config.Config;
import neatlogic.framework.common.constvalue.RunnerStatus;
import neatlogic.framework.dao.mapper.runner.RunnerMapper;
import neatlogic.framework.dto.runner.RunnerMapVo;
import neatlogic.framework.dto.runner.RunnerVo;
import neatlogic.framework.exception.runner.RunnerIdNotFoundException;
import neatlogic.framework.exception.runner.RunnerIpIsExistException;
import neatlogic.framework.exception.runner.RunnerNameRepeatsException;
import neatlogic.framework.exception.runner.RunnerNotFoundException;
import neatlogic.framework.integration.authentication.enums.AuthenticateType;
import neatlogic.framework.util.HttpRequestUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Objects;

@Service
public class RunnerServiceImpl implements RunnerService {
    private final Logger logger = LoggerFactory.getLogger(RunnerServiceImpl.class);
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

        runnerMapper.insertRunnerMap(new RunnerMapVo(replaceRunner.getId(), replaceRunner.getId()));
    }

    /**
     * 检查runner联通性
     */
    @Override
    public JSONObject checkRunnerHealth(Long runnerId) {
        RunnerVo runner = runnerMapper.getRunnerById(runnerId);
        if (runner == null) {
            throw new RunnerNotFoundException(runnerId.toString());
        }
        JSONObject statusObj = new JSONObject();
        String url = runner.getUrl() + "api/rest/health/check";
        HttpRequestUtil requestUtil = HttpRequestUtil.post(url).setPayload(new JSONObject().toJSONString()).setAuthType(AuthenticateType.BUILDIN).setConnectTimeout(Config.RUNNER_CONNECT_TIMEOUT()).setReadTimeout(Config.RUNNER_READ_TIMEOUT()).sendRequest();
        long statusLcd = System.currentTimeMillis();
        if (requestUtil.getResponseCode() != 200 || StringUtils.isNotBlank(requestUtil.getError())) {
            logger.error(String.format("Request to %s failed, result: %s, ResponseCode: %s, ErrorMsg: %s, Exception %s",
                    url, requestUtil.getResult(), requestUtil.getResponseCode(), requestUtil.getErrorMsg(), requestUtil.getError()));
            runnerMapper.updateStatusById(runner.getId(), RunnerStatus.DISCONNECTED.getValue(), new Date(statusLcd));
            statusObj.put("status", RunnerStatus.DISCONNECTED.getValue());
            statusObj.put("statusText", RunnerStatus.DISCONNECTED.getText());
        } else {
            runnerMapper.updateStatusById(runner.getId(), RunnerStatus.CONNECTED.getValue(), new Date(statusLcd));
            statusObj.put("status", RunnerStatus.CONNECTED.getValue());
            statusObj.put("statusText", RunnerStatus.CONNECTED.getText());
        }
        statusObj.put("statusLcd", statusLcd);
        return statusObj;
    }
}

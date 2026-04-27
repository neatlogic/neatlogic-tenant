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

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.ADMIN;
import neatlogic.framework.common.config.Config;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dao.mapper.runner.RunnerMapper;
import neatlogic.framework.dto.runner.RunnerVo;
import neatlogic.framework.exception.runner.RunnerHttpRequestException;
import neatlogic.framework.exception.runner.RunnerNotFoundException;
import neatlogic.framework.integration.authentication.enums.AuthenticateType;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.HttpRequestUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@AuthAction(action = ADMIN.class)
public class GetRunnerWaitingDetailApi extends PrivateApiComponentBase {

    @Resource
    RunnerMapper runnerMapper;

    @Override
    public String getName() {
        return "nmtar.getrunnerwaitingdetailapi.getname";
    }

    @Input({
            @Param(name = "id", type = ApiParamType.LONG, isRequired = true, desc = "runner id"),
            @Param(name = "jobId", type = ApiParamType.LONG, desc = "job id")
    })
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        JSONObject params = new  JSONObject();
        JSONObject result = new JSONObject();
        Long runnerId = paramObj.getLong("id");
        Long jobId = paramObj.getLong("jobId");
        if(jobId == null) {
            jobId = 1L;
            params.put("jobId", jobId);
        }
        RunnerVo runner = runnerMapper.getRunnerById(runnerId);
        if (runner == null) {
            throw new RunnerNotFoundException(runnerId.toString());
        }
        String url = runner.getUrl() + "api/rest/job/waiting/detail/get";
        HttpRequestUtil requestUtil = HttpRequestUtil.post(url).setPayload(params.toJSONString()).setAuthType(AuthenticateType.BUILDIN).setConnectTimeout(Config.RUNNER_CONNECT_TIMEOUT()).setReadTimeout(Config.RUNNER_READ_TIMEOUT()).sendRequest();
        if (requestUtil.getResponseCode() != 200 || StringUtils.isNotBlank(requestUtil.getError())) {
            throw new RunnerHttpRequestException("Request failed! " + url + ":" + requestUtil.getError());
        }
        JSONObject resultJson = requestUtil.getResultJson();
        return resultJson.getJSONObject("Return");
    }

    @Override
    public String getToken() {
        return "runner/waiting/detail/get";
    }
}

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
import neatlogic.framework.auth.label.NoAuth;
import neatlogic.framework.common.config.Config;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dao.mapper.runner.RunnerMapper;
import neatlogic.framework.dto.runner.RunnerVo;
import neatlogic.framework.exception.runner.RunnerHttpRequestException;
import neatlogic.framework.exception.runner.RunnerNotFoundException;
import neatlogic.framework.exception.runner.RunnerUrlIsNullException;
import neatlogic.framework.integration.authentication.enums.AuthenticateType;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.HttpRequestUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@AuthAction(action = NoAuth.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class GetRunnerVersionApi extends PrivateApiComponentBase {
    private static final Logger logger = LoggerFactory.getLogger(GetRunnerVersionApi.class);

    @Resource
    private RunnerMapper runnerMapper;

    @Override
    public String getName() {
        return "nmtar.getrunnerversionapi.getname";
    }

    @Input({
            @Param(name = "id", type = ApiParamType.LONG, isRequired = true, desc = "nmtar.getrunnerversionapi.input.param.desc.id")
    })
    @Output({
            @Param(name = "tbodyList", type = ApiParamType.JSONARRAY, desc = "nmtar.getrunnerversionapi.output.param.desc.tbodylist")
    })
    @Description(desc = "nmtar.getrunnerversionapi.description")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        Long runnerId = paramObj.getLong("id");
        RunnerVo runner = runnerMapper.getRunnerById(runnerId);
        if (runner == null) {
            throw new RunnerNotFoundException(runnerId.toString());
        }
        if (StringUtils.isBlank(runner.getUrl())) {
            throw new RunnerUrlIsNullException(runnerId);
        }
        String url = runner.getUrl() + "api/rest/system/version/get";
        HttpRequestUtil requestUtil = HttpRequestUtil.post(url)
                .setPayload(new JSONObject().toJSONString())
                .setAuthType(AuthenticateType.BUILDIN)
                .setConnectTimeout(Config.RUNNER_CONNECT_TIMEOUT())
                .setReadTimeout(Config.RUNNER_READ_TIMEOUT())
                .sendRequest();
        if (requestUtil.getResponseCode() != 200 || StringUtils.isNotBlank(requestUtil.getError())) {
            logger.error("获取执行器版本信息失败,url:{},responseCode:{},error:{}", url, requestUtil.getResponseCode(), requestUtil.getError());
            throw new RunnerHttpRequestException("Request failed! " + url + ":" + requestUtil.getError());
        }
        JSONObject resultJson = requestUtil.getResultJson();
        if (resultJson == null || resultJson.getJSONObject("Return") == null) {
            logger.error("获取执行器版本信息失败,url:{},result:{}", url, resultJson);
            throw new RunnerHttpRequestException("Request failed! " + url);
        }
        return resultJson.getJSONObject("Return");
    }

    @Override
    public String getToken() {
        return "runner/version/get";
    }
}

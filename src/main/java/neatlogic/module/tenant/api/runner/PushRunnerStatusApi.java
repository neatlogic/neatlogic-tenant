/*
 * Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package neatlogic.module.tenant.api.runner;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.RequestContext;
import neatlogic.framework.common.constvalue.RunnerStatus;
import neatlogic.framework.dto.runner.RunnerVo;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.tenant.service.RunnerServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class PushRunnerStatusApi extends PrivateApiComponentBase {

    @Override
    public String getName() {
        return "runner推送更新runner状态";
    }

    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        String host = RequestContext.get().getRequest().getRemoteHost();
        RunnerVo runnerVo = new RunnerVo();
        runnerVo.setHost(host);
        runnerVo.setStatus(RunnerStatus.CONNECTED.getValue());
        runnerVo.setInfo(paramObj.toJSONString());
        runnerVo.setStatusLcd(new Date());
        RunnerServiceImpl.runnerInforMap.put(host,runnerVo);
        return null;
    }

    @Override
    public String getToken() {
        return "runner/status/push";
    }
}

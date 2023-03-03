/*
 * Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package neatlogic.module.tenant.api.notify.job;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.notify.dao.mapper.NotifyJobMapper;
import neatlogic.framework.notify.dto.job.NotifyJobVo;
import neatlogic.framework.notify.exception.NotifyJobNotFoundException;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class NotifyJobGetApi extends PrivateApiComponentBase {

	@Autowired
	private NotifyJobMapper notifyJobMapper;

	@Override
	public String getToken() {
		return "notify/job/get";
	}

	@Override
	public String getName() {
		return "获取通知定时任务";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({@Param(name = "id", type = ApiParamType.LONG, isRequired = true, desc = "定时任务ID")})
	@Output({@Param(name = "job", explode = NotifyJobVo.class, desc = "定时任务")})
	@Description(desc = "获取通知定时任务")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JSONObject result = new JSONObject();
		Long id = jsonObj.getLong("id");
		if(notifyJobMapper.getJobBaseInfoById(id) == null){
			throw new NotifyJobNotFoundException(id);
		}
		result.put("job",notifyJobMapper.getJobDetailById(id));
		return result;
	}
}

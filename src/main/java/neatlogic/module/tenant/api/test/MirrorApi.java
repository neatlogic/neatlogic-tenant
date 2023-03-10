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

package neatlogic.module.tenant.api.test;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Component;

@Deprecated

@Component
public class MirrorApi extends PrivateApiComponentBase {

	@Override
	public String getToken() {
		// TODO Auto-generated method stub
		return "test/mirror";
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "反射测试api";
	}

	@Override
	public String getConfig() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isRaw() {
		return true;
	}

	@Override
	@Description(desc = "反射测试api")
	public Object myDoService(JSONObject jsonObj) throws Exception {
		return jsonObj;
	}

}

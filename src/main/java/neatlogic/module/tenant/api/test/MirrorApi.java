/*Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.

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

package neatlogic.module.tenant.api.test;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;

@Deprecated

//@Component
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

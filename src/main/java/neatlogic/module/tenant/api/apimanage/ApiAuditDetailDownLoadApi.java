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

package neatlogic.module.tenant.api.apimanage;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateBinaryStreamApiComponentBase;
import neatlogic.framework.util.AuditUtil;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class ApiAuditDetailDownLoadApi extends PrivateBinaryStreamApiComponentBase {

	@Override
	public String getToken() {
		return "apimanage/audit/detail/download";
	}

	@Override
	public String getName() {
		return "下载接口调用记录";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({@Param(name = "filePath", type = ApiParamType.STRING, desc = "调用记录文件路径", isRequired = true)})
	@Output({})
	@Description(desc = "下载接口调用记录")
	@Override
	public Object myDoService(JSONObject jsonObj, HttpServletRequest request, HttpServletResponse response) throws Exception {

		String filePath = jsonObj.getString("filePath");

		AuditUtil.downLoadAuditDetail(request, response, filePath);

		return null;
	}

}

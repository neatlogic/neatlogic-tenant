/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.apimanage;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateBinaryStreamApiComponentBase;
import codedriver.framework.util.AuditUtil;
import com.alibaba.fastjson.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Deprecated

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

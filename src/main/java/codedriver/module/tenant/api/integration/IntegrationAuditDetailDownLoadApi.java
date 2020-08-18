package codedriver.module.tenant.api.integration;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.BinaryStreamApiComponentBase;
import codedriver.framework.util.AuditUtil;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class IntegrationAuditDetailDownLoadApi extends BinaryStreamApiComponentBase {

	@Override
	public String getToken() {
		return "integration/audit/detail/download";
	}

	@Override
	public String getName() {
		return "下载集成管理调用记录";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({@Param(name = "filePath", type = ApiParamType.STRING, desc = "调用记录文件路径", isRequired = true)})
	@Output({})
	@Description(desc = "下载集成管理调用记录")
	@Override
	public Object myDoService(JSONObject jsonObj, HttpServletRequest request, HttpServletResponse response) throws Exception {

		String filePath = jsonObj.getString("filePath");

		AuditUtil.downLoadAuditDetail(request, response, filePath);

		return null;
	}

}

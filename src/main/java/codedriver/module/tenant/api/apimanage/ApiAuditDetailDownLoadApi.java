package codedriver.module.tenant.api.apimanage;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.auth.label.FRAMEWORK_BASE;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateBinaryStreamApiComponentBase;
import codedriver.framework.util.AuditUtil;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Service
@AuthAction(action = FRAMEWORK_BASE.class)
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

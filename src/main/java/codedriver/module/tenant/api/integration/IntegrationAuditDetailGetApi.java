package codedriver.module.tenant.api.integration;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.auth.label.FRAMEWORK_BASE;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.exception.file.FilePathIllegalException;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.util.AuditUtil;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

@Service
@AuthAction(action = FRAMEWORK_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class IntegrationAuditDetailGetApi extends PrivateApiComponentBase {

	@Override
	public String getToken() {
		return "integration/audit/detail/get";
	}

	@Override
	public String getName() {
		return "获取集成管理审计内容";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({ @Param(name = "filePath", type = ApiParamType.STRING, desc = "调用记录文件路径", isRequired = true) })
	@Output({ @Param(type = ApiParamType.STRING) })
	@Description(desc = "获取集成管理审计内容")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {

		JSONObject resultJson = new JSONObject();

		String filePath = jsonObj.getString("filePath");

		if(!filePath.contains("?") || !filePath.contains("&") || !filePath.contains("=")){
			throw new FilePathIllegalException("文件路径格式错误");
		}

		long offset = Long.parseLong(filePath.split("\\?")[1].split("&")[1].split("=")[1]);

		String result = null;
		if(offset > AuditUtil.maxFileSize){
			result = AuditUtil.getAuditDetail(filePath);
			resultJson.put("result",result);
			resultJson.put("hasMore",true);
		}else{
			result = AuditUtil.getAuditDetail(filePath);
			resultJson.put("result",result);
			resultJson.put("hasMore",false);
		}
		return resultJson;
	}
}

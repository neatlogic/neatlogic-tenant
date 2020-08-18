package codedriver.module.tenant.api.apimanage;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.exception.file.FilePathIllegalException;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.framework.util.AuditUtil;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class ApiAuditDetailGetApi extends ApiComponentBase {

	@Override
	public String getToken() {
		return "apimanage/audit/detail/get";
	}

	@Override
	public String getName() {
		return "获取接口调用记录";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({ @Param(name = "filePath", type = ApiParamType.STRING, desc = "调用记录文件路径", isRequired = true) })
	@Output({})
	@Description(desc = "获取接口调用记录")
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

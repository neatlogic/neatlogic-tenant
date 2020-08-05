package codedriver.module.tenant.api.apimanage;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.tenant.service.apiaudit.ApiAuditService;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class ApiAuditDetailGetApi extends ApiComponentBase {

	@Autowired
	private ApiAuditService apiAuditService;

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

		String filePath = jsonObj.getString("filePath");
		System.out.println("读取文件前：" + System.currentTimeMillis());
		String result = apiAuditService.getAuditContentOnFile(filePath);
		System.out.println("读取文件后：" + System.currentTimeMillis());

		return result;
	}

}

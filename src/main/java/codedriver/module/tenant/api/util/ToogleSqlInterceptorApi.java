package codedriver.module.tenant.api.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dao.plugin.SqlCostInterceptor;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;

@Service
@OperationType(type = OperationTypeEnum.OPERATE)
public class ToogleSqlInterceptorApi extends ApiComponentBase {

	@Override
	public String getToken() {
		return "/util/togglesqlinterceptor";
	}

	@Override
	public String getName() {
		return "控制系统SQL追踪";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({ @Param(name = "action", type = ApiParamType.ENUM, isRequired = true, rule = "insert,remove,clear", desc = "insert：激活追踪指定SQL，remove：取消追踪指定SQL，clear：取消追踪所有SQL"), @Param(name = "id", type = ApiParamType.STRING, desc = "mapper配置文件中的sql id") })
	@Description(desc = "打开指定SQL日志，在标准输出中能查看最终执行SQL和执行时间")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String action = jsonObj.getString("action");
		String id = jsonObj.getString("id");
		if (StringUtils.isNotBlank(action)) {
			if (action.equalsIgnoreCase("clear")) {
				SqlCostInterceptor.SqlIdMap.clear();
			} else if (action.equalsIgnoreCase("insert") && StringUtils.isNotBlank(id)) {
				SqlCostInterceptor.SqlIdMap.addId(id);
			} else if (action.equalsIgnoreCase("remove") && StringUtils.isNotBlank(id)) {
				SqlCostInterceptor.SqlIdMap.removeId(id);
			}
		}
		return null;
	}
}

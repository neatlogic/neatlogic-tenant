package codedriver.module.tenant.api.notify;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.notify.constvalue.NotifyPolicyParamSource;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.IsActived;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;

@Service
@IsActived
public class NotifyPolicyParamSourceListApi extends ApiComponentBase {

	@Override
	public String getToken() {
		return "notify/policy/paramSource/list";
	}

	@Override
	public String getName() {
		return "通知策略参数来源列表接口";
	}

	@Override
	public String getConfig() {
		return null;
	}
	
	@Output({
		@Param(name = "paramSourceList", explode = ValueTextVo[].class, desc = "参数来源列表") 
	})
	@Description(desc = "通知策略参数来源列表接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JSONObject resultObj = new JSONObject();
		List<ValueTextVo> paramSourceList = new ArrayList<>();
		for(NotifyPolicyParamSource source : NotifyPolicyParamSource.values()) {
			paramSourceList.add(new ValueTextVo(source.getValue(), source.getText()));
		}
		resultObj.put("paramSourceList", paramSourceList);
		return resultObj;
	}

}

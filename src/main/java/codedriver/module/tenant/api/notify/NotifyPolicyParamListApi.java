package codedriver.module.tenant.api.notify;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.notify.core.INotifyPolicyHandler;
import codedriver.framework.notify.core.NotifyPolicyHandlerFactory;
import codedriver.framework.notify.dto.NotifyPolicyParamVo;
import codedriver.framework.notify.dto.NotifyPolicyVo;
import codedriver.framework.notify.exception.NotifyPolicyHandlerNotFoundException;
import codedriver.framework.notify.exception.NotifyPolicyNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
@Service
public class NotifyPolicyParamListApi extends ApiComponentBase {

	@Override
	public String getToken() {
		return "notify/policy/param/list";
	}

	@Override
	public String getName() {
		return "通知策略参数列表接口";
	}

	@Override
	public String getConfig() {
		return null;
	}
	
	@Input({
		@Param(name = "keyword", type = ApiParamType.STRING, desc = "模糊匹配"),
		@Param(name = "policyUuid", type = ApiParamType.STRING, isRequired = true, desc = "策略uuid"),
		@Param(name = "source", type = ApiParamType.ENUM, rule = "system,custom", desc = "参数来源")
	})
	@Output({
		@Param(name = "paramList", explode = NotifyPolicyParamVo[].class, desc = "参数列表")
	})
	@Description(desc = "通知策略参数列表接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object myDoTest(JSONObject jsonObj) {
		List<NotifyPolicyParamVo> paramList = new ArrayList<>();
		String policyUuid = jsonObj.getString("policyUuid");
		NotifyPolicyVo notifyPolicyVo = NotifyPolicyVo.notifyPolicyMap.get(policyUuid);
		if(notifyPolicyVo == null) {
			throw new NotifyPolicyNotFoundException(policyUuid);
		}
		String keyword = jsonObj.getString("keyword");
		String source = jsonObj.getString("source");
		if(!"system".equals(source)) {
			JSONObject configObj = notifyPolicyVo.getConfigObj();
			List<NotifyPolicyParamVo> customParamList = JSON.parseArray(configObj.getJSONArray("paramList").toJSONString(), NotifyPolicyParamVo.class);
			if(CollectionUtils.isNotEmpty(customParamList)) {
				for(NotifyPolicyParamVo notifyPolicyParamVo : customParamList) {
					if(StringUtils.isNotBlank(keyword)) {
						if(!notifyPolicyParamVo.getName().toLowerCase().contains(keyword.toLowerCase()) 
								&& !notifyPolicyParamVo.getDescription().toLowerCase().contains(keyword.toLowerCase())) {
							continue;
						}
					}
					paramList.add(notifyPolicyParamVo);
				}
			}
		}
		if(!"custom".equals(source)) {
			INotifyPolicyHandler notifyPolicyHandler = NotifyPolicyHandlerFactory.getHandler(notifyPolicyVo.getPolicyHandler());
			if(notifyPolicyHandler == null) {
				throw new NotifyPolicyHandlerNotFoundException(notifyPolicyVo.getPolicyHandler());
			}
			List<NotifyPolicyParamVo> systemParamList = notifyPolicyHandler.getSystemParamList();
			if(CollectionUtils.isNotEmpty(systemParamList)) {
				for(NotifyPolicyParamVo notifyPolicyParamVo : systemParamList) {
					if(StringUtils.isNotBlank(keyword)) {
						if(!notifyPolicyParamVo.getName().toLowerCase().contains(keyword.toLowerCase()) 
								&& !notifyPolicyParamVo.getDescription().toLowerCase().contains(keyword.toLowerCase())) {
							continue;
						}
					}
					paramList.add(notifyPolicyParamVo);
				}
			}
		}
		return paramList;
	}
}

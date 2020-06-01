package codedriver.module.tenant.api.notify;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.notify.core.INotifyPolicyHandler;
import codedriver.framework.notify.core.NotifyPolicyFactory;
import codedriver.framework.notify.core.NotifyPolicyHandlerFactory;
import codedriver.framework.notify.dao.mapper.NotifyMapper;
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
	
	@Autowired
	private NotifyMapper notifyMapper;

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
		@Param(name = "policyId", type = ApiParamType.LONG, isRequired = true, desc = "策略id"),
		@Param(name = "source", type = ApiParamType.ENUM, rule = "system,custom", desc = "参数来源")
	})
	@Output({
		@Param(name = "paramList", explode = NotifyPolicyParamVo[].class, desc = "参数列表")
	})
	@Description(desc = "通知策略参数列表接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		List<NotifyPolicyParamVo> paramList = new ArrayList<>();
		Long policyId = jsonObj.getLong("policyId");
		NotifyPolicyVo notifyPolicyVo = notifyMapper.getNotifyPolicyById(policyId);
		if(notifyPolicyVo == null) {
			throw new NotifyPolicyNotFoundException(policyId.toString());
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
			INotifyPolicyHandler notifyPolicyHandler = NotifyPolicyHandlerFactory.getHandler(notifyPolicyVo.getHandler());
			if(notifyPolicyHandler == null) {
				throw new NotifyPolicyHandlerNotFoundException(notifyPolicyVo.getHandler());
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

	@Override
	public Object myDoTest(JSONObject jsonObj) {
		List<NotifyPolicyParamVo> paramList = new ArrayList<>();
		Long policyId = jsonObj.getLong("policyId");
		NotifyPolicyVo notifyPolicyVo = NotifyPolicyFactory.notifyPolicyMap.get(policyId);
		if(notifyPolicyVo == null) {
			throw new NotifyPolicyNotFoundException(policyId.toString());
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
			INotifyPolicyHandler notifyPolicyHandler = NotifyPolicyHandlerFactory.getHandler(notifyPolicyVo.getHandler());
			if(notifyPolicyHandler == null) {
				throw new NotifyPolicyHandlerNotFoundException(notifyPolicyVo.getHandler());
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

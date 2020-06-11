package codedriver.module.tenant.api.notify;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.notify.constvalue.NotifyPolicyActionType;
import codedriver.framework.notify.core.INotifyPolicyHandler;
import codedriver.framework.notify.core.NotifyPolicyHandlerFactory;
import codedriver.framework.notify.dao.mapper.NotifyMapper;
import codedriver.framework.notify.dto.NotifyPolicyVo;
import codedriver.framework.notify.dto.NotifyTemplateVo;
import codedriver.framework.notify.exception.NotifyPolicyHandlerNotFoundException;
import codedriver.framework.notify.exception.NotifyPolicyNotFoundException;
import codedriver.framework.notify.exception.NotifyTemplateNameRepeatException;
import codedriver.framework.notify.exception.NotifyTemplateNotFoundException;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.IsActived;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
@Service
@Transactional
@IsActived
public class NotifyPolicyTemplateSaveApi extends ApiComponentBase {

	@Autowired
	private NotifyMapper notifyMapper;

	@Override
	public String getToken() {
		return "notify/policy/template/save";
	}

	@Override
	public String getName() {
		return "通知模板保存接口";
	}

	@Override
	public String getConfig() {
		return null;
	}
	@Input({
		@Param(name = "policyId", type = ApiParamType.LONG, isRequired = true, desc = "策略id"),
		@Param(name = "id", type = ApiParamType.LONG, desc = "模板id"),
		@Param(name = "name", type = ApiParamType.REGEX, rule = "^[A-Za-z_\\d\\u4e00-\\u9fa5]{1,50}$", isRequired = true, desc = "模板名称"),
		@Param(name = "title", type = ApiParamType.STRING, isRequired = true, desc = "模板标题"),
		@Param(name = "content", type = ApiParamType.STRING, isRequired = true, desc = "模板内容"),
		@Param(name = "notifyHandler", type = ApiParamType.STRING, desc = "通知处理器")
	})
	@Output({
		@Param(explode = NotifyTemplateVo.class, desc = "通知模板信息")
	})
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long policyId = jsonObj.getLong("policyId");
		NotifyPolicyVo notifyPolicyVo = notifyMapper.getNotifyPolicyById(policyId);
		if(notifyPolicyVo == null) {
			throw new NotifyPolicyNotFoundException(policyId.toString());
		}
		INotifyPolicyHandler notifyPolicyHandler = NotifyPolicyHandlerFactory.getHandler(notifyPolicyVo.getHandler());
		if(notifyPolicyHandler == null) {
			throw new NotifyPolicyHandlerNotFoundException(notifyPolicyVo.getHandler());
		}
		
		Long id = jsonObj.getLong("id");
		String name = jsonObj.getString("name");
		String title = jsonObj.getString("title");
		String content = jsonObj.getString("content");
		String notifyHandler = jsonObj.getString("notifyHandler");
		NotifyTemplateVo resultTemplateVo = null;
		JSONObject config = notifyPolicyVo.getConfig();
		List<NotifyTemplateVo> templateList = JSON.parseArray(config.getJSONArray("templateList").toJSONString(), NotifyTemplateVo.class);
		for(NotifyTemplateVo notifyTemplateVo : templateList) {
			if(name.equals(notifyTemplateVo.getName()) && !notifyTemplateVo.getId().equals(id)) {
				throw new NotifyTemplateNameRepeatException(name);
			}
		}
		if(id != null) {
			boolean isExists = false;
			for(NotifyTemplateVo notifyTemplateVo : templateList) {
				if(id.equals(notifyTemplateVo.getId())) {
					notifyTemplateVo.setName(name);
					notifyTemplateVo.setTitle(title);
					notifyTemplateVo.setContent(content);
					notifyTemplateVo.setNotifyHandler(notifyHandler);
					notifyTemplateVo.setAction(NotifyPolicyActionType.UPDATE.getValue());
					notifyTemplateVo.setActionTime(new Date());
					notifyTemplateVo.setActionUser(UserContext.get().getUserName());
					isExists = true;
					resultTemplateVo = notifyTemplateVo;
				}
			}
			if(!isExists) {
				throw new NotifyTemplateNotFoundException(id.toString());
			}
		}else {
			NotifyTemplateVo notifyTemplateVo = new NotifyTemplateVo();
			notifyTemplateVo.setName(name);
			notifyTemplateVo.setTitle(title);
			notifyTemplateVo.setContent(content);
			notifyTemplateVo.setNotifyHandler(notifyHandler);
			notifyTemplateVo.setAction(NotifyPolicyActionType.CREATE.getValue());
			notifyTemplateVo.setActionTime(new Date());
			notifyTemplateVo.setActionUser(UserContext.get().getUserName());
			templateList.add(notifyTemplateVo);
			resultTemplateVo = notifyTemplateVo;
		}
		config.put("templateList", templateList);
		notifyPolicyVo.setConfig(config.toJSONString());
		notifyMapper.updateNotifyPolicyById(notifyPolicyVo);
		return resultTemplateVo;
	}
	
}

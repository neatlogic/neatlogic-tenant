package codedriver.module.tenant.api.notify;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.notify.core.INotifyPolicyHandler;
import codedriver.framework.notify.core.NotifyPolicyFactory;
import codedriver.framework.notify.core.NotifyPolicyHandlerFactory;
import codedriver.framework.notify.dao.mapper.NotifyMapper;
import codedriver.framework.notify.dto.NotifyPolicyVo;
import codedriver.framework.notify.exception.NotifyPolicyHandlerNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
@Service
public class NotifyPolicySearchApi  extends ApiComponentBase {
	
	@Autowired
	private NotifyMapper notifyMapper;

	@Override
	public String getToken() {
		return "notify/policy/search";
	}

	@Override
	public String getName() {
		return "通知策略管理列表搜索接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "keyword", type = ApiParamType.STRING, desc = "关键字搜索"),
		@Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否分页"),
		@Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页数"),
		@Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页条数"),
		@Param(name = "handler", type = ApiParamType.STRING, isRequired = true, desc = "通知策略处理器")
	})
	@Output({
		@Param(explode=BasePageVo.class),
		@Param(name = "tbodyList", explode = NotifyPolicyVo[].class, desc = "通知策略列表")
	})
	@Description(desc = "通知策略管理列表搜索接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JSONObject resultObj = new JSONObject();
		NotifyPolicyVo notifyPolicyVo = JSON.toJavaObject(jsonObj, NotifyPolicyVo.class);
		List<NotifyPolicyVo> tbodyList = notifyMapper.getNotifyPolicyList(notifyPolicyVo);
		if(CollectionUtils.isNotEmpty(tbodyList)) {
			List<Long> policyIdList = tbodyList.stream().map(NotifyPolicyVo::getId).collect(Collectors.toList());
			List<NotifyPolicyVo> notifyPolicyInvokerCountList = notifyMapper.getNotifyPolicyInvokerCountListByPolicyIdList(policyIdList);
			Map<Long, Integer> notifyPolicyInvokerCountMap = new HashMap<>();
			for(NotifyPolicyVo notifyPolicy : notifyPolicyInvokerCountList) {
				notifyPolicyInvokerCountMap.put(notifyPolicy.getId(), notifyPolicy.getInvokerCount());
			}
			for(NotifyPolicyVo notifyPolicy : tbodyList) {
				notifyPolicy.setInvokerCount(notifyPolicyInvokerCountMap.get(notifyPolicy.getId()));
			}
		}
		
		resultObj.put("tbodyList", tbodyList);
		if(notifyPolicyVo.getNeedPage()) {
			int rowNum = notifyMapper.getNotifyPolicyCount(notifyPolicyVo);
			resultObj.put("currentPage", notifyPolicyVo.getCurrentPage());
			resultObj.put("pageSize", notifyPolicyVo.getPageSize());
			resultObj.put("pageCount", PageUtil.getPageCount(rowNum, notifyPolicyVo.getPageSize()));
			resultObj.put("rowNum", rowNum);
		}
		return resultObj;
	}
	
	@Override
	public Object myDoTest(JSONObject jsonObj) {
		String handler = jsonObj.getString("handler");
		INotifyPolicyHandler notifyPolicyHandler = NotifyPolicyHandlerFactory.getHandler(handler);
		if(notifyPolicyHandler == null) {
			throw new NotifyPolicyHandlerNotFoundException(handler);
		}
		JSONObject resultObj = new JSONObject();
		BasePageVo basePageVo = JSON.toJavaObject(jsonObj, BasePageVo.class);
		List<NotifyPolicyVo> tbodyList = new ArrayList<>();

		for(Entry<Long, NotifyPolicyVo> entry : NotifyPolicyFactory.notifyPolicyMap.entrySet()) {
			NotifyPolicyVo notifyPolicy = entry.getValue();
			
			if(handler.equals(notifyPolicy.getHandler())) {
				if(StringUtils.isNoneBlank(basePageVo.getKeyword())) {
					String keyword = basePageVo.getKeyword().toLowerCase();
					String name = notifyPolicy.getName().toLowerCase();
					if(name.contains(keyword)) {
						tbodyList.add(notifyPolicy);
					}
				}else {
					tbodyList.add(notifyPolicy);
				}
			}			
		}
		tbodyList.sort((e1, e2) -> -e1.getActionTime().compareTo(e2.getActionTime()));
		
		if(basePageVo.getNeedPage()) {
			int rowNum = tbodyList.size();
			resultObj.put("currentPage", basePageVo.getCurrentPage());
			resultObj.put("pageSize", basePageVo.getPageSize());
			resultObj.put("pageCount", PageUtil.getPageCount(rowNum, basePageVo.getPageSize()));
			resultObj.put("rowNum", rowNum);
			if(rowNum > 0) {
				int fromIndex = basePageVo.getStartNum();
				fromIndex = fromIndex >= rowNum ? rowNum - 1 : fromIndex;
				int toIndex = fromIndex + basePageVo.getPageSize();
				toIndex = toIndex > rowNum ? rowNum : toIndex;
				tbodyList = tbodyList.subList(fromIndex, toIndex);
			}		
		}
		resultObj.put("tbodyList", tbodyList);
		return resultObj;
	}

}

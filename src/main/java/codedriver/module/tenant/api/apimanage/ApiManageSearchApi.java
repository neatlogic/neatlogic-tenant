package codedriver.module.tenant.api.apimanage;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.exception.type.ComponentNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.framework.restful.core.ApiComponentFactory;
import codedriver.framework.restful.dao.mapper.ApiMapper;
import codedriver.framework.restful.dto.ApiHandlerVo;
import codedriver.framework.restful.dto.ApiVo;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ApiManageSearchApi extends ApiComponentBase {

	@Autowired
	private ApiMapper ApiMapper;
	
	@Override
	public String getToken() {
		return "apimanage/search";
	}

	@Override
	public String getName() {
		return "接口配置信息列表接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "keyword", type = ApiParamType.STRING, xss = true, desc = "关键字，接口名模糊查询"),
		@Param(name = "moduleGroup", type = ApiParamType.STRING, desc = "接口所属模块组"),
		@Param(name = "funcId", type = ApiParamType.STRING, desc = "接口所属功能"),
		@Param(name = "apiType", type = ApiParamType.STRING, desc = "接口类型(system|custom)"),
		@Param(name = "handler", type = ApiParamType.STRING, desc = "处理器"),
		@Param(name = "isActive", type = ApiParamType.ENUM, rule = "0,1", desc = "是否激活"),
		@Param(name = "currentPage", type = ApiParamType.INTEGER, desc="当前页码，默认值1"),
		@Param(name = "pageSize", type = ApiParamType.INTEGER, desc="页大小，默认值10"),
		@Param(name = "needPage", type = ApiParamType.BOOLEAN, desc="是否分页，默认值true")
		
	})
	@Output({
		@Param(name = "currentPage", type = ApiParamType.INTEGER, desc="当前页码"),
		@Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "页大小"),
		@Param(name = "pageCount", type = ApiParamType.INTEGER, desc = "总页数"),
		@Param(name = "rowNum", type = ApiParamType.INTEGER, desc = "总行数"),
		@Param(name = "tbodyList", explode = ApiVo[].class, isRequired = true, desc = "接口配置信息列表")
	})
	@Description(desc = "接口配置信息列表接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		ApiVo apiVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<ApiVo>() {});
		String handler = apiVo.getHandler();
		
		if(handler != null) {
			ApiHandlerVo apiHandlerVo = ApiComponentFactory.getApiHandlerByHandler(handler);
			if(apiHandlerVo == null) {
				throw new ComponentNotFoundException("接口组件:" + handler + "不存在");
			}
		}
		
		List<ApiVo> ramApiList = new ArrayList<>();
		List<String> tokenList = new ArrayList<>();
		List<String> ramTokenList = new ArrayList<>();
		//从内存中取出符合搜索条件的api、token数据
		for(ApiVo api : ApiComponentFactory.getApiList()) {
			if(apiVo.getIsActive() != null && !apiVo.getIsActive().equals(api.getIsActive())) {
				continue;
			}
//			if(StringUtils.isNotBlank(apiVo.getModuleId()) && !apiVo.getModuleId().equals(api.getHandler())) {
//				continue;
//			}
			if(StringUtils.isNotBlank(handler) && !handler.equals(api.getHandler())) {
				continue;
			}
			//根据接口类型筛选接口（用于接口管理页的系统接口/自定义接口的相互切换）
			if(StringUtils.isNotBlank(apiVo.getApiType()) && !apiVo.getApiType().equals(api.getApiType())) {
				continue;
			}
			//根据模块筛选接口（用于接口管理页的目录树筛选）
			if(StringUtils.isNotBlank(apiVo.getModuleGroup()) && !apiVo.getModuleGroup().equals(api.getModuleGroup())) {
				continue;
			}
			//根据功能筛选接口（用于接口管理页的目录树筛选）
			if (StringUtils.isNotBlank(apiVo.getFuncId())) {
				if(apiVo.getFuncId().contains("/")){
					if(!api.getToken().contains("/")){
						continue;
					}else if(api.getToken().contains("/") && !api.getToken().startsWith(apiVo.getFuncId() + "/")){
						continue;
					}
				}else {
					if(!api.getToken().contains("/") && !apiVo.getFuncId().equals(api.getToken())) {
						continue;
					}else if(api.getToken().contains("/") && !api.getToken().startsWith(apiVo.getFuncId() + "/")) {
						continue;
					}
				}

			}
			if(StringUtils.isNotBlank(apiVo.getKeyword())) {
				if(!api.getName().contains(apiVo.getKeyword()) && !api.getToken().contains(apiVo.getKeyword())) {
					continue;
				}
			}
			ramApiList.add(api);
			tokenList.add(api.getToken());
			ramTokenList.add(api.getToken());
		}

		List<ApiVo> dbAllApiList = ApiMapper.getAllApi();
		List<ApiVo> dbApiList = new ArrayList<>();
		List<String> dbTokenList = new ArrayList<>();
		Map<String, ApiVo> ramApiMap = ApiComponentFactory.getApiMap();
		for(ApiVo api : dbAllApiList) {

			if(ramApiMap.get(api.getToken()) != null){
				api.setApiType(ApiVo.ApiType.SYSTEM.getValue());
			}else{
				api.setApiType(ApiVo.ApiType.CUSTOM.getValue());
			}

			//根据接口类型筛选接口（用于接口管理页的系统接口/自定义接口的相互切换）
			if (StringUtils.isNotBlank(apiVo.getApiType()) && !apiVo.getApiType().equals(api.getApiType())) {
				continue;
			}
			//根据模块筛选接口（用于接口管理页的目录树筛选）
			if(StringUtils.isNotBlank(apiVo.getModuleGroup()) && !apiVo.getModuleGroup().equals(api.getModuleGroup())) {
				continue;
			}
			//根据功能筛选接口（用于接口管理页的目录树筛选）
			if(apiVo.getFuncId().contains("/")){
				if(!api.getToken().contains("/")){
					continue;
				}else if(api.getToken().contains("/") && !api.getToken().startsWith(apiVo.getFuncId() + "/")){
					continue;
				}
			}else {
				if(!api.getToken().contains("/") && !apiVo.getFuncId().equals(api.getToken())) {
					continue;
				}else if(api.getToken().contains("/") && !api.getToken().startsWith(apiVo.getFuncId() + "/")) {
					continue;
				}
			}
			if(StringUtils.isNotBlank(apiVo.getKeyword())) {
				if(!api.getName().contains(apiVo.getKeyword()) && !api.getToken().contains(apiVo.getKeyword())) {
					continue;
				}
			}
			dbTokenList.add(api.getToken());
			dbApiList.add(api);
		}
		//从数据库中取出符合搜索条件的token数据
//		List<String> dbTokenList = ApiMapper.getApiTokenList(apiVo);
		//将内存和数据库取出的token合并去重
		for(String token : dbTokenList) {
			if(tokenList.contains(token)) {
				continue;
			}
			tokenList.add(token);
		}
		//token排序
		tokenList.sort(String::compareTo);
		
		JSONObject resultObj = new JSONObject();
		if(apiVo.getNeedPage()) {
			int rowNum = tokenList.size();
			int pageCount = PageUtil.getPageCount(rowNum, apiVo.getPageSize());
			resultObj.put("currentPage", apiVo.getCurrentPage());
			resultObj.put("pageSize", apiVo.getPageSize());
			resultObj.put("pageCount", pageCount);
			resultObj.put("rowNum", rowNum);
			//取出当前页token
			int fromIndex = apiVo.getStartNum();
			if(fromIndex < rowNum) {
				int toIndex = fromIndex + apiVo.getPageSize();
				toIndex = toIndex > rowNum ? rowNum : toIndex;
				tokenList = tokenList.subList(fromIndex, toIndex);
			}else {
				tokenList = new ArrayList<>();
			}			
			ramTokenList.retainAll(tokenList);
			dbTokenList.retainAll(tokenList);
		}
				
		Map<String, ApiVo> apiMap = new HashMap<>();		
		if(!ramTokenList.isEmpty()) {
			//从内存中取出当前页api数据，保存在map中
			for(ApiVo api : ramApiList) {
				if(apiVo.getNeedPage() && !ramTokenList.contains(api.getToken())) {
					continue;
				}
				apiMap.put(api.getToken(), api);
			}
		}		
		for(ApiVo api : dbApiList) {
			if(apiMap.containsKey(api.getToken())) {
				api.setIsDeletable(0);
			}
			apiMap.put(api.getToken(), api);
		}
//		Map<String, Integer> visitTimesMap = new HashMap<>();
//		if(!tokenList.isEmpty()) {
//			List<ApiVo> visitTimesList = ApiMapper.getApiVisitTimesListByTokenList(tokenList);
//			for(ApiVo api : visitTimesList) {
//				visitTimesMap.put(api.getToken(), api.getVisitTimes());
//			}
//		}

		//从map中按顺序取出api数据
		List<ApiVo> apiList = new ArrayList<>();
		for(String token : tokenList) {
			ApiVo api = apiMap.get(token);
//			Integer visitTimes = visitTimesMap.get(token);
//			if(visitTimes != null) {
//				api.setVisitTimes(visitTimes);
//			}
			apiList.add(api);
		}

		/**
		 * 根据token获取每个API的访问次数，并保存在ApiVo的visitTimes字段中
		 */
		List<String> apiTokenList = new ArrayList<>();
		apiList.stream().forEach(vo -> apiTokenList.add(vo.getToken()));
		if(!apiTokenList.isEmpty()){
			List<ApiVo> apiVisitTimesList = ApiMapper.getApiAccessCountByTokenList(apiTokenList);
			if(!apiVisitTimesList.isEmpty()){
				apiList.stream().forEach(api -> {
					for(ApiVo vo : apiVisitTimesList){
						if(api.getToken().equals(vo.getToken())){
							api.setVisitTimes(vo.getVisitTimes());
							break;
						}
					}
				});
			}
		}

		resultObj.put("tbodyList", apiList);
		
		return resultObj;
	}

}

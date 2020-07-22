package codedriver.module.tenant.api.apimanage;

import codedriver.framework.common.util.ModuleUtil;
import codedriver.framework.dto.ModuleGroupVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.framework.restful.core.ApiComponentFactory;
import codedriver.framework.restful.dao.mapper.ApiMapper;
import codedriver.framework.restful.dto.ApiVo;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 接口管理页-左侧目录树接口
 * 整体思路：
 * 1、获取系统所有模块
 * 2、获取所有的API(包括内存中的和数据库中的)
 * 3、遍历模块列表，构造目录树中的每一个目录及其下面的子项
 */

@Service
public class ApiManageTreeSearchApi extends ApiComponentBase {

	@Autowired
	private ApiMapper apiMapper;

	@Override
	public String getToken() {
		return "apimanage/tree/search";
	}

	@Override
	public String getName() {
		return "获取接口管理页树形目录接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({ })
	@Output({})
	@Description(desc = "接口管理-树形目录接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		// TODO 根据apiType判断返回系统接口目录还是自定义接口目录，还要有全部的目录，以提供给操作审计使用
		// TODO 默认展示两层目录，在第二层标识是否有子目录
		// TODO 增加一个接口，点击第二层目录时查询其下所有子目录
		String apiMenuType = jsonObj.getString("apiMenuType");
		//存储最终目录的数组
		JSONArray menuJsonArray = new JSONArray();
		List<Map<String,Object>> menuMapList = new ArrayList<>();
		if(StringUtils.isNotBlank(apiMenuType)){
			List<ApiVo> apiList = null;
			//获取系统中所有的模块
			Map<String, ModuleGroupVo> moduleGroupVoMap = ModuleUtil.getModuleGroupMap();
			if("system".equals(apiMenuType)){
				apiList = ApiComponentFactory.getApiList();
			}else if("custom".equals(apiMenuType)) {
				//获取数据库中所有的API
				List<ApiVo> dbApiList = apiMapper.getAllApi();
				Map<String, ApiVo> ramApiMap = ApiComponentFactory.getApiMap();
				apiList = new ArrayList<>();
				//与系统中的API匹配token，如果匹配不上则表示是自定义API
				for (ApiVo vo : dbApiList) {
					if (ramApiMap.get(vo.getToken()) == null) {
						apiList.add(vo);
					}
				}
			}
			if(CollectionUtils.isNotEmpty(apiList)){
				for(Map.Entry<String, ModuleGroupVo> vo : moduleGroupVoMap.entrySet()){
					Map<String,Object> moduleMap = new JSONObject();
					moduleMap.put("moduleGroup",vo.getKey());
					moduleMap.put("moduleGroupName",vo.getValue().getGroupName());
					//多个token的第一个单词相同，用Set可以去重
					Set<Func> funcSet = new HashSet<>(16);
					for(ApiVo apiVo : apiList){
						String moduleGroup = apiVo.getModuleGroup();
						if(vo.getKey().equals(moduleGroup)){
							String token = apiVo.getToken();
							Func func = new Func();
//						int count = (token.length() - token.replace("/", "").length());
//						System.out.println(count);
							//有些API的token没有“/”，比如登出接口
							if(token.indexOf("/") < 0){
								func.setFuncId(token);
							}else{
								func.setFuncId(token.substring(0,token.indexOf("/")));
							}
							funcSet.add(func);
						}
					}
					moduleMap.put("funcList",funcSet);
					menuMapList.add(moduleMap);
				}
			}
			//TODO 分析出每个模块每个功能下最长的token有几个"/"，以此来判断是否有子目录
			List<Map<String,Object>> menuArray = new ArrayList();
			for(Map<String,Object> map : menuMapList){
				Set<Func> funcSet = (Set<Func>)map.get("funcList");
				Map<String,Object> moduleMap = new HashMap<>();
				moduleMap.put("moduleGroup",map.get("moduleGroup").toString());
				List<Map<String,Object>> funcList = new ArrayList<>();
				for(Func func : funcSet){
					Map<String,Object> funcMap = new HashMap<>();
					funcMap.put("funcId",func.getFuncId());
					int count = 0;
					for(ApiVo apiVo : apiList){
						String moduleGroup = apiVo.getModuleGroup();
						if(map.get("moduleGroup").toString().equals(moduleGroup)){
							String token = apiVo.getToken();
							if(token.startsWith(func.getFuncId())){
								if((token.length() - token.replace("/", "").length()) >= count){
									count = (token.length() - token.replace("/", "").length());
								}
							}
							funcMap.put("count",count);
						}

					}
					funcList.add(funcMap);
				}
				moduleMap.put("funcList",funcList);
				menuArray.add(moduleMap);
			}
			System.out.println(menuArray);

			//TODO 匹配目录集，去给hasChildren赋值
			for(Map<String,Object> moduleMap : menuMapList){
				for(Map<String,Object> map : menuArray){
					if(moduleMap.get("moduleGroup").equals(map.get("moduleGroup"))){
						Set<Func> funcList = (Set<Func>) moduleMap.get("funcList");
						List<Map<String,Object>> countFuncList = (List<Map<String,Object>>)map.get("funcList");
						for(Func func : funcList){
							for(Map<String,Object> countMap : countFuncList){
								if(func.getFuncId().equals(countMap.get("funcId"))){
									if(Integer.parseInt(countMap.get("count").toString()) > 1){
										func.setHasChildren(true);
									}
								}
							}
						}
					}
				}
			}
		}
		return menuMapList;
	}

	class Func{
		private String funcId;

		private Boolean hasChildren = false;

		public String getFuncId() {
			return funcId;
		}

		public void setFuncId(String funcId) {
			this.funcId = funcId;
		}

		public Boolean getHasChildren() {
			return hasChildren;
		}

		public void setHasChildren(Boolean hasChildren) {
			this.hasChildren = hasChildren;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			Func func = (Func) o;
			return Objects.equals(funcId, func.funcId);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((funcId == null) ? 0 : funcId.hashCode());
			return result;
		}
	}
}

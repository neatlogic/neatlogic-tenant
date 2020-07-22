package codedriver.module.tenant.api.apimanage;

import codedriver.framework.common.util.ModuleUtil;
import codedriver.framework.dto.ModuleGroupVo;
import codedriver.framework.dto.ModuleVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.framework.restful.core.ApiComponentFactory;
import codedriver.framework.restful.dao.mapper.ApiMapper;
import codedriver.framework.restful.dto.ApiAuditVo;
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
		// TODO 根据apiType判断返回系统接口目录还是自定义接口目录
		// TODO 默认展示两层目录，在第二层标识是否有子目录
		// TODO 增加一个接口，点击第二层目录时查询其下所有子目录
		String apiMenuType = jsonObj.getString("apiMenuType");
		//存储最终目录的数组
		JSONArray menuJsonArray = new JSONArray();
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
					JSONObject moduleJson = new JSONObject();
					moduleJson.put("moduleGroup",vo.getKey());
					moduleJson.put("moduleGroupName",vo.getValue().getGroupName());
					//多个token的第一个单词相同，用Set可以去重
					Set<String> funcSet = new HashSet<>(16);
					for(ApiVo apiVo : apiList){
						String moduleGroup = apiVo.getModuleGroup();
						if(vo.getKey().equals(moduleGroup)){
							String token = apiVo.getToken();
							String funcId;
//						int count = (token.length() - token.replace("/", "").length());
//						System.out.println(count);
							//有些API的token没有“/”，比如登出接口
							if(token.indexOf("/") < 0){
								funcId = token;
							}else{
								funcId = token.substring(0,token.indexOf("/"));
							}
							funcSet.add(funcId);
						}
					}
					moduleJson.put("funcList",funcSet);
					menuJsonArray.add(moduleJson);
				}
			}
		}
		return menuJsonArray;
	}

//	class Func{
//		private String funcId;
//
//		public String getFuncId() {
//			return funcId;
//		}
//
//		public void setFuncId(String funcId) {
//			this.funcId = funcId;
//		}
//
//		@Override
//		public boolean equals(Object o) {
//			if (this == o) return true;
//			if (o == null || getClass() != o.getClass()) return false;
//			Func func = (Func) o;
//			return Objects.equals(funcId, func.funcId);
//		}
//
//		@Override
//		public int hashCode() {
//			final int prime = 31;
//			int result = 1;
//			result = prime * result + ((funcId == null) ? 0 : funcId.hashCode());
//			return result;
//		}
//	}
}

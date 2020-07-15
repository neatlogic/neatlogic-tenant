package codedriver.module.tenant.api.apimanage;

import codedriver.framework.common.util.ModuleUtil;
import codedriver.framework.dto.ModuleVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.framework.restful.core.ApiComponentFactory;
import codedriver.framework.restful.dao.mapper.ApiMapper;
import codedriver.framework.restful.dto.ApiVo;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
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

		//存储最终目录的数组
		JSONArray menuJsonArray = new JSONArray();
		//获取系统中所有的模块
		List<ModuleVo> moduleList = ModuleUtil.getAllModuleList();
		Set<String> moduleGroupSet = new HashSet<>();
		//获取所有的moduleGroup
		moduleList.stream().forEach(vo -> moduleGroupSet.add(vo.getGroup()));
		List<ModuleVo> moduleVoGroupList = new ArrayList<>();
		for(String module : moduleGroupSet){
			for(ModuleVo vo : moduleList){
				if(vo.getId().equals(module)){
					ModuleVo moduleVo = new ModuleVo();
					moduleVo.setGroup(module);
					moduleVo.setGroupName(vo.getGroupName());
					moduleVoGroupList.add(moduleVo);
					continue;
				}
			}
		}
		//获取所有的内存API
		HashMap<String,ApiVo> apiMap = new HashMap<>();
		apiMap.putAll(ApiComponentFactory.getApiMap());
		//获取数据库中所有的API
		List<ApiVo> dbApiList = apiMapper.getAllApi();
		//内存API与数据库API去重：如果token相同，数据库api数据覆盖内存api数据
		for(ApiVo api : dbApiList){
			if(apiMap.containsKey(api.getToken())) {
				api.setIsDeletable(0);
			}
			apiMap.put(api.getToken(), api);
		}
		Collection<ApiVo> apiList = apiMap.values();
		for(ModuleVo vo : moduleVoGroupList){
			JSONObject moduleJson = new JSONObject();
			moduleJson.put("moduleGroup",vo.getGroup());
			moduleJson.put("moduleName",vo.getGroupName());
			//多个token的第一个单词相同，用Set可以去重
			Set<String> funcSet = new HashSet<>(16);
			for(ApiVo apiVo : apiList){
				String moduleGroup = apiVo.getModuleGroup();
				if(vo.getGroup().equals(moduleGroup)){
					String token = apiVo.getToken();
					String funcId;
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
		return menuJsonArray;
	}
}

package codedriver.module.tenant.api.apimanage;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.util.ModuleUtil;
import codedriver.framework.dto.ModuleGroupVo;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.framework.restful.core.ApiComponentFactory;
import codedriver.framework.restful.dao.mapper.ApiMapper;
import codedriver.framework.restful.dto.ApiVo;
import codedriver.module.tenant.service.apiaudit.ApiAuditService;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
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
@OperationType(type = OperationTypeEnum.SEARCH)
public class ApiManageTreeSearchApi extends ApiComponentBase {

	@Autowired
	private ApiMapper apiMapper;

	@Autowired
	private ApiAuditService apiAuditService;

	@Override
	public String getToken() {
		return "apimanage/tree/search";
	}

	@Override
	public String getName() {
		return "获取接口管理或操作审计树形目录";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({@Param(name = "menuType", type = ApiParamType.STRING, desc = "目录类型(system|custom|audit)",isRequired = true)})
	@Output({})
	@Description(desc = "获取接口管理或操作审计树形目录")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		// TODO 根据apiType判断返回系统接口目录还是自定义接口目录，还要有全部的目录，以提供给操作审计使用
		// TODO 默认展示两层目录，在第二层标识是否有子目录
		// TODO 增加一个接口，点击第二层目录时查询其下所有子目录
		String menuType = jsonObj.getString("menuType");
		//存储最终目录的数组
//		JSONArray menuJsonArray = new JSONArray();
		List<Map<String,Object>> menuMapList = new ArrayList<>();

		List<ApiVo> apiList = null;
		//获取系统中所有的模块
		Map<String, ModuleGroupVo> moduleGroupVoMap = ModuleUtil.getModuleGroupMap();
		if(ApiVo.TreeMenuType.SYSTEM.getValue().equals(menuType)){
			apiList = ApiComponentFactory.getApiList();
		}else if(ApiVo.TreeMenuType.CUSTOM.getValue().equals(menuType)) {
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
		}else if(ApiVo.TreeMenuType.AUDIT.getValue().equals(menuType)){
			/**
			 * 操作审计页面目录树
			 */
			apiList = apiAuditService.getApiListForTree();
		}
		if(CollectionUtils.isNotEmpty(apiList)){
			for(Map.Entry<String, ModuleGroupVo> vo : moduleGroupVoMap.entrySet()){
				Map<String,Object> moduleMap = new HashMap<>();
				moduleMap.put("moduleGroup",vo.getKey());
				moduleMap.put("moduleGroupName",vo.getValue().getGroupName());
				//多个token的第一个单词相同，用Set可以去重
				Set<Func> funcSet = new HashSet<>(16);
				for(ApiVo apiVo : apiList){
					String moduleGroup = apiVo.getModuleGroup();
					if(vo.getKey().equals(moduleGroup)){
						String token = apiVo.getToken();
						Func func = new Func();
						//有些API的token没有“/”，比如登出接口
						String[] slashSplit = token.split("/");
						func.setFuncId(slashSplit[0]);
//						if(token.indexOf("/") < 0){
//							func.setFuncId(token);
//						}else{
//							func.setFuncId(token.substring(0,token.indexOf("/")));
//						}
						if(funcSet.contains(func)){
							if(slashSplit.length > 2){
								funcSet.stream().forEach(func1 -> {
									if (func1.getFuncId().equals(func.funcId) && func1.getIsHasChild() == 0) {
										func1.setIsHasChild(1);
									}
								});
							}
						}else{
							if(slashSplit.length > 2){
								func.setIsHasChild(1);
							}
							funcSet.add(func);
						}
					}
				}
				moduleMap.put("funcList",funcSet);
				menuMapList.add(moduleMap);
			}
		}

		return menuMapList;
	}

	class Func{
		private String funcId;

		private int isHasChild = 0;

		public String getFuncId() {
			return funcId;
		}

		public void setFuncId(String funcId) {
			this.funcId = funcId;
		}

        public int getIsHasChild() {
            return isHasChild;
        }

        public void setIsHasChild(int isHasChild) {
            this.isHasChild = isHasChild;
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

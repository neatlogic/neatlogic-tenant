//package codedriver.module.tenant.api.apiaudit;
//
//import codedriver.framework.common.util.ModuleUtil;
//import codedriver.framework.dto.ModuleGroupVo;
//import codedriver.framework.reminder.core.OperationTypeEnum;
//import codedriver.framework.restful.annotation.Description;
//import codedriver.framework.restful.annotation.Input;
//import codedriver.framework.restful.annotation.OperationType;
//import codedriver.framework.restful.annotation.Output;
//import codedriver.framework.restful.core.ApiComponentBase;
//import codedriver.framework.restful.dto.ApiVo;
//import codedriver.module.tenant.service.apiaudit.ApiAuditService;
//import com.alibaba.fastjson.JSONObject;
//import org.apache.commons.collections4.CollectionUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.util.*;
//
//
//@Service
//@OperationType(type = OperationTypeEnum.SEARCH)
//public class ApiAuditTreeSearchApi extends ApiComponentBase {
//
//	@Autowired
//	private ApiAuditService apiAuditService;
//
//	@Override
//	public String getToken() {
//		return "apiaudit/tree/search";
//	}
//
//	@Override
//	public String getName() {
//		return "获取操作审计树形目录接口";
//	}
//
//	@Override
//	public String getConfig() {
//		return null;
//	}
//
//	@Input({})
//	@Output({})
//	@Description(desc = "获取操作审计树形目录接口")
//	@Override
//	public Object myDoService(JSONObject jsonObj) throws Exception {
//		List<ApiVo> apiList = apiAuditService.getApiListForTree();
//		Map<String, ModuleGroupVo> moduleGroupVoMap = ModuleUtil.getModuleGroupMap();
//		List<Map<String,Object>> menuMapList = new ArrayList<>();
//		if(CollectionUtils.isNotEmpty(apiList)){
//			for(Map.Entry<String, ModuleGroupVo> vo : moduleGroupVoMap.entrySet()){
//				Map<String,Object> moduleMap = new JSONObject();
//				moduleMap.put("moduleGroup",vo.getKey());
//				moduleMap.put("moduleGroupName",vo.getValue().getGroupName());
//				//多个token的第一个单词相同，用Set可以去重
//				Set<Func> funcSet = new HashSet<>(16);
//				for(ApiVo apiVo : apiList){
//					String moduleGroup = apiVo.getModuleGroup();
//					if(vo.getKey().equals(moduleGroup)){
//						String token = apiVo.getToken();
//						Func func = new Func();
//						//有些API的token没有“/”，比如登出接口
//						String[] slashSplit = token.split("/");
//						func.setFuncId(slashSplit[0]);
//						if(funcSet.contains(func)){
//							if(slashSplit.length > 2){
//								funcSet.stream().forEach(func1 -> {
//									if (func1.getFuncId().equals(func.funcId) && func1.getIsHasChild() == 0) {
//										func1.setIsHasChild(1);
//									}
//								});
//							}
//						}else{
//							if(slashSplit.length > 2){
//								func.setIsHasChild(1);
//							}
//							funcSet.add(func);
//						}
//					}
//				}
//				moduleMap.put("funcList",funcSet);
//				menuMapList.add(moduleMap);
//			}
//		}
//
//		return menuMapList;
//
//	}
//
//	class Func{
//		private String funcId;
//
//		private int isHasChild = 0;
//
//		public String getFuncId() {
//			return funcId;
//		}
//
//		public void setFuncId(String funcId) {
//			this.funcId = funcId;
//		}
//
//        public int getIsHasChild() {
//            return isHasChild;
//        }
//
//        public void setIsHasChild(int isHasChild) {
//            this.isHasChild = isHasChild;
//        }
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
//}

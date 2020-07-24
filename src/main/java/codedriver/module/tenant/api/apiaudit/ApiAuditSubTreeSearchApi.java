//package codedriver.module.tenant.api.apiaudit;
//
//import codedriver.framework.common.constvalue.ApiParamType;
//import codedriver.framework.restful.annotation.Description;
//import codedriver.framework.restful.annotation.Input;
//import codedriver.framework.restful.annotation.Output;
//import codedriver.framework.restful.annotation.Param;
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
//@Service
//public class ApiAuditSubTreeSearchApi extends ApiComponentBase {
//
//	@Autowired
//	private ApiAuditService apiAuditService;
//
//	@Override
//	public String getToken() {
//		return "apiaudit/subtree/search";
//	}
//
//	@Override
//	public String getName() {
//		return "获取操作审计树形目录子目录接口";
//	}
//
//	@Override
//	public String getConfig() {
//		return null;
//	}
//
//	@Input({
//			@Param(name = "moduleGroup", type = ApiParamType.STRING, desc = "所属模块"),
//			@Param(name = "funcId", type = ApiParamType.STRING, desc = "所属功能"),
//	})
//	@Output({})
//	@Description(desc = "获取操作审计树形目录子目录接口")
//	@Override
//	public Object myDoService(JSONObject jsonObj) throws Exception {
//		Object moduleGroup = jsonObj.get("moduleGroup");
//		Object funcId = jsonObj.get("funcId");
//		Set<Func> result = new HashSet<>();
//		if(moduleGroup != null && funcId != null){
//
//			List<ApiVo> apiList = apiAuditService.getApiListForTree();
//
//			if(CollectionUtils.isNotEmpty(apiList)){
//				List<String> tokenList = new ArrayList<>();
//				for(ApiVo vo : apiList){
//					if(vo.getModuleGroup().equals(moduleGroup) && vo.getToken().startsWith(funcId.toString() + "/")){
//						tokenList.add(vo.getToken());
//					}
//				}
//
//				String[] funcSplit = funcId.toString().split("/");
//				/**
//				 * 与入参的funcId根据"/"拆分后的数组比较长度
//				 * 如果大于1，说明下面还有func
//				 * 比如入参funcId为"process/channel"，当前token为"process/channel/form/get"
//				 * 那么下一层func就是form
//				 */
//				for(String token : tokenList){
//					String[] split = token.split("/");
//					if(split.length - funcSplit.length > 1){
//						Func func = new Func();
//						String s = split[funcSplit.length];
//						func.setFuncId(s);
//						if(result.contains(func)){
//							if(split.length - funcSplit.length > 2){
//								result.stream().forEach(vo -> {
//									if (vo.getFuncId().equals(func.funcId) && vo.getIsHasChild() == 0) {
//										vo.setIsHasChild(1);
//									}
//								});
//							}
//						}else{
//							if(split.length - funcSplit.length > 2){
//								func.setIsHasChild(1);
//							}
//							result.add(func);
//						}
//					}
//				}
//			}
//		}
//
//		return result;
//	}
//
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
//		public int getIsHasChild() {
//			return isHasChild;
//		}
//
//		public void setIsHasChild(int isHasChild) {
//			this.isHasChild = isHasChild;
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
//
//}

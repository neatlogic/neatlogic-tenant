package codedriver.module.tenant.api.notify;

import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
@Service
@OperationType(type = OperationTypeEnum.SEARCH)
@Deprecated
public class NotifyPolicyInvokerList extends PrivateApiComponentBase {

//	@Autowired
//	private NotifyMapper notifyMapper;
	
	@Override
	public String getToken() {
		return "notify/policy/invoker/list";
	}

	@Override
	public String getName() {
		return "通知策略引用列表接口";
	}

	@Override
	public String getConfig() {
		return null;
	}
	
	@Input({
		@Param(name = "policyId", type = ApiParamType.LONG, isRequired = true, desc = "策略id"),
		@Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否需要分页，默认true"),
		@Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页条目"),
		@Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页")
	})
	@Output({
		@Param(name="currentPage",type=ApiParamType.INTEGER,isRequired=true,desc="当前页码"),
		@Param(name="pageSize",type=ApiParamType.INTEGER,isRequired=true,desc="页大小"),
		@Param(name="pageCount",type=ApiParamType.INTEGER,isRequired=true,desc="总页数"),
		@Param(name="rowNum",type=ApiParamType.INTEGER,isRequired=true,desc="总行数"),
//		@Param(name = "notifyPolicyInvokerList", explode = NotifyPolicyInvokerVo[].class, desc = "通知策略引用列表")
	})
	@Description(desc = "通知策略引用列表接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JSONObject resultObj = new JSONObject();
//		NotifyPolicyInvokerVo notifyPolicyInvokerVo = JSON.toJavaObject(jsonObj, NotifyPolicyInvokerVo.class);
//		NotifyPolicyVo notifyPolicyVo = notifyMapper.getNotifyPolicyById(notifyPolicyInvokerVo.getPolicyId());
//		if(notifyPolicyVo == null) {
//			throw new NotifyPolicyNotFoundException(notifyPolicyInvokerVo.getPolicyId().toString());
//		}
//		List<NotifyPolicyInvokerVo> notifyPolicyInvokerList = notifyMapper.getNotifyPolicyInvokerList(notifyPolicyInvokerVo);
//		resultObj.put("notifyPolicyInvokerList", notifyPolicyInvokerList);
//		if(notifyPolicyInvokerVo.getNeedPage()) {
//			int rowNum = notifyMapper.getNotifyPolicyInvokerCountByPolicyId(notifyPolicyInvokerVo.getPolicyId());
//			resultObj.put("currentPage", notifyPolicyInvokerVo.getCurrentPage());
//			resultObj.put("pageSize", notifyPolicyInvokerVo.getPageSize());
//			resultObj.put("pageCount", PageUtil.getPageCount(rowNum, notifyPolicyInvokerVo.getPageSize()));
//			resultObj.put("rowNum", rowNum);
//		}
		return resultObj;
	}

}

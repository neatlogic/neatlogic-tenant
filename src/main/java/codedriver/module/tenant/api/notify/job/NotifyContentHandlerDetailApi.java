package codedriver.module.tenant.api.notify.job;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.dto.ConditionParamVo;
import codedriver.framework.notify.core.INotifyContentHandler;
import codedriver.framework.notify.core.NotifyContentHandlerFactory;
import codedriver.framework.notify.exception.NotifyContentHandlerNotFoundException;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class NotifyContentHandlerDetailApi extends PrivateApiComponentBase {

	@Override
	public String getToken() {
		return "notify/content/handler/detail";
	}

	@Override
	public String getName() {
		return "获取通知内容插件详情";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({@Param(name = "handler", type = ApiParamType.STRING, isRequired = true,desc = "通知内容插件")})
	@Output({
			@Param(name = "conditionList", explode = ConditionParamVo[].class,desc = "条件列表"),
			@Param(name = "dataColumnList", explode = ValueTextVo[].class,desc = "内容数据列")
	})
	@Description(desc = "获取通知内容插件详情")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		/** 传入new LinkedHashMap()可按put顺序排序，先渲染条件，再渲染数据列 */
		JSONObject result = new JSONObject(new LinkedHashMap<>());
		String handler = jsonObj.getString("handler");
		INotifyContentHandler notifyContentHandler = NotifyContentHandlerFactory.getHandler(handler);
		if(notifyContentHandler == null){
			throw new NotifyContentHandlerNotFoundException(handler);
		}
		JSONArray conditionList = notifyContentHandler.getConditionOptionList();
		List<ValueTextVo> dataColumn = notifyContentHandler.getDataColumnList();
		result.put("conditionList",conditionList);
		result.put("dataColumnList",dataColumn);
		return result;
	}
}

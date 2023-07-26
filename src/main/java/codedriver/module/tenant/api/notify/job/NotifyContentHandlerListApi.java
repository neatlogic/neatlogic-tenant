/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.notify.job;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.notify.core.NotifyContentHandlerFactory;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class NotifyContentHandlerListApi extends PrivateApiComponentBase {

	@Override
	public String getToken() {
		return "notify/content/handler/list";
	}

	@Override
	public String getName() {
		return "获取通知内容插件列表";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({})
	@Output({@Param(explode = ValueTextVo[].class,desc = "通知内容插件列表")})
	@Description(desc = "获取通知内容插件列表")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		return NotifyContentHandlerFactory.getNotifyContentHandlerList();
	}
}
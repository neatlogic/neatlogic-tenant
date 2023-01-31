/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package neatlogic.module.tenant.api.menu.mobile;

import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.tenant.dao.mapper.MenuMobileMapper;
import neatlogic.module.tenant.dto.MenuMobileVo;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class MenuMobileListApi extends PrivateApiComponentBase {
	
	@Autowired
	private MenuMobileMapper menuMobileMapper;

	@Override
	public String getToken() {
		return "menu/mobile/list";
	}

	@Override
	public String getName() {
		return "查询移动端菜单";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		})
	@Output({ 
		@Param(name = "list", explode = MenuMobileVo[].class, desc = "菜单列表")
		})
	@Description(desc = "查询移动端菜单")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		return menuMobileMapper.getMenuMobileList();
	}
}

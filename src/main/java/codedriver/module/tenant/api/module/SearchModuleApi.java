/*
 * Copyright(c) 2022 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.module;

import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.auth.label.MODULE_MODIFY;
import codedriver.framework.dto.module.ModuleGroupVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@AuthAction(action = MODULE_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class SearchModuleApi extends PrivateApiComponentBase {


    @Override
    public String getToken() {
        return "/module/search";
    }

    @Override
    public String getName() {
        return "获取模块列表";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Output({@Param(explode = ModuleGroupVo[].class)})
    @Description(desc = "获取模块列表接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        List<ModuleGroupVo> moduleGroupList = TenantContext.get().getActiveModuleGroupList();
        moduleGroupList.sort(Comparator.comparing(ModuleGroupVo::getGroupSort));
        return moduleGroupList;
    }
}

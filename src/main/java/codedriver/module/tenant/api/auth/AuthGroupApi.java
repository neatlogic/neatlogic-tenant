/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.auth;

import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.auth.core.AuthBase;
import codedriver.framework.auth.core.AuthFactory;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.util.ModuleUtil;
import codedriver.framework.dto.ModuleGroupVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @program: codedriver
 * @description:
 * @create: 2020-03-17 12:03
 **/
@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class AuthGroupApi extends PrivateApiComponentBase {

    @Override
    public String getToken() {
        return "auth/group";
    }

    @Override
    public String getName() {
        return "权限组列表获取接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Output({
            @Param( name = "groupList", type = ApiParamType.JSONARRAY, desc = "权限组列表")
    })
    @Description(desc = "权限组列表获取接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject returnObj = new JSONObject();
        JSONArray groupArray = new JSONArray();
        List<ModuleGroupVo> moduleGroupVos = TenantContext.get().getActiveModuleGroupList();
        if(CollectionUtils.isNotEmpty(moduleGroupVos)) {
            Map<String, List<AuthBase>> authGroupMap = AuthFactory.getAuthGroupMap();
            Set<String> groupSet = authGroupMap.keySet();
            for (ModuleGroupVo moduleGroupVo : moduleGroupVos) {
                String group = moduleGroupVo.getGroup();
                if (groupSet.contains(group)) {
                    JSONObject groupObj = new JSONObject();
                    groupObj.put("value", group);
                    groupObj.put("text", moduleGroupVo.getGroupName());
                    groupArray.add(groupObj);
                }
            }
        }
        returnObj.put("groupList", groupArray);
        return returnObj;
    }
}

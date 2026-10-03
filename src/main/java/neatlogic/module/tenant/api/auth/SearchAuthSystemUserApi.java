package neatlogic.module.tenant.api.auth;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.core.AuthFactory;
import neatlogic.framework.auth.label.AUTHORITY_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.constvalue.systemuser.ISystemUser;
import neatlogic.framework.common.constvalue.systemuser.SystemUserFactory;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.exception.auth.AuthNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/** 权限管理查询系统内置用户，只展示注册身份和当前权限的直接成员。 */
@Service
@AuthAction(action = AUTHORITY_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class SearchAuthSystemUserApi extends PrivateApiComponentBase {
    @Resource
    private UserMapper userMapper;

    @Override
    public String getToken() {
        return "auth/system/user/search";
    }

    @Override
    public String getName() {
        return "auth.systemuser.search";
    }

    @Override
    public String getConfig() {
        return null;
    }

    /** 未指定权限时返回所有注册用户；指定权限时批量查询直接成员，不构造凭据或展开角色权限。 */
    @Input({
            @Param(name = "auth", type = ApiParamType.STRING, desc = "common.auth")
    })
    @Output({
            @Param(name = "tbodyList", type = ApiParamType.JSONARRAY, desc = "common.tbodylist"),
            @Param(name = "rowNum", type = ApiParamType.INTEGER, desc = "common.rownum")
    })
    @Description(desc = "auth.systemuser.search")
    @Override
    public Object myDoService(JSONObject jsonObj) {
        List<ISystemUser> systemUserList = SystemUserFactory.getSystemUserList();
        String auth = jsonObj.getString("auth");
        Set<String> memberUuidSet = Collections.emptySet();
        boolean filterByAuth = StringUtils.isNotBlank(auth);
        if (filterByAuth) {
            if (AuthFactory.getAuthInstance(auth) == null) {
                throw new AuthNotFoundException(auth);
            }
            List<String> registeredUuidList = new ArrayList<>();
            for (ISystemUser systemUser : systemUserList) {
                registeredUuidList.add(systemUser.getUserUuid());
            }
            // 工厂为空时不执行 IN 查询，避免空集合扩大查询范围。
            if (!registeredUuidList.isEmpty()) {
                memberUuidSet = new HashSet<>(userMapper.getUserUuidListByAuthAndUserUuidList(auth, registeredUuidList));
            }
        }
        JSONArray tbodyList = new JSONArray();
        for (ISystemUser systemUser : systemUserList) {
            if (filterByAuth && !memberUuidSet.contains(systemUser.getUserUuid())) {
                continue;
            }
            JSONObject userObj = new JSONObject();
            userObj.put("uuid", systemUser.getUserUuid());
            userObj.put("userId", systemUser.getUserId());
            userObj.put("userName", systemUser.getUserName());
            tbodyList.add(userObj);
        }
        JSONObject result = new JSONObject();
        result.put("tbodyList", tbodyList);
        result.put("rowNum", tbodyList.size());
        return result;
    }
}

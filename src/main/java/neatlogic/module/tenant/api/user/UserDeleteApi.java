package neatlogic.module.tenant.api.user;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.USER_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.tenant.service.UserSessionService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@AuthAction(action = USER_MODIFY.class)
@Service
@OperationType(type = OperationTypeEnum.DELETE)
public class UserDeleteApi extends PrivateApiComponentBase {

    @Resource
    private UserMapper userMapper;

    @Resource
    UserSessionService userSessionService;

    @Override
    public String getToken() {
        return "user/delete";
    }

    @Override
    public String getName() {
        return "nmtau.userdeleteapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }


    @Input({
            @Param(name = "userUuidList", type = ApiParamType.JSONARRAY, desc = "common.useruuidlist", isRequired = true, minSize = 1)
    })
    @Output({})
    @Description(desc = "nmtau.userdeleteapi.getname")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONArray userUuidArray = jsonObj.getJSONArray("userUuidList");
        List<String> userUuidList = userUuidArray.toJavaList(String.class);
        for (String userUuid : userUuidList) {
            userMapper.updateUserIsDeletedByUuid(userUuid);
            userSessionService.deleteUserSessionAndCache(userUuid);
        }
        return null;
    }
}


/*
 * Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package neatlogic.module.tenant.api.user;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dto.UserVo;
import neatlogic.framework.exception.user.UserNotFoundException;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @program: neatlogic
 * @description:
 * @create: 2020-03-06 11:49
 **/
@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class UserGetListApi extends PrivateApiComponentBase {

    @Autowired
    private UserMapper userMapper;

    @Override
    public String getToken() {
        return "user/get/list";
    }

    @Override
    public String getName() {
        return "批量获取用户信息接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param( name = "userUuidList", type = ApiParamType.JSONARRAY, desc = "用户uuid集合", isRequired = true)
    })
    @Output({
            @Param( name = "userList", explode = UserVo[].class, desc = "用户信息集合")
    })
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject returnObj = new JSONObject();
        List<String> userUuidList = JSON.parseArray(jsonObj.getString("userUuidList"), String.class);
        List<UserVo> userList = new ArrayList<>();
        for (String userUuid : userUuidList){
            UserVo userVo = userMapper.getUserByUuid(userUuid);
            if(userVo == null) {
    			throw new UserNotFoundException(userUuid);
    		}else {
                userList.add(userVo);
            }
        }
        returnObj.put("userList", userList);
        return returnObj;
    }
}

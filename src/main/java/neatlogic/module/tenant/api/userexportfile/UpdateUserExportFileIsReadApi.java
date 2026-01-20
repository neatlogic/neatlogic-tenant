/*
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package neatlogic.module.tenant.api.userexportfile;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.NoAuth;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.crossover.CrossoverServiceFactory;
import neatlogic.framework.crossover.IUserExportFileCrossoverMapper;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.userexportfile.dto.UserExportFileVo;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@AuthAction(action = NoAuth.class)
@OperationType(type = OperationTypeEnum.DELETE)
public class UpdateUserExportFileIsReadApi extends PrivateApiComponentBase {

    @Override
    public String getName() {
        return "nmtau.updateuserexportfileisreadapi.getname";
    }

    @Input({
            @Param(name = "idList", type = ApiParamType.JSONARRAY, isRequired = true, minSize = 1, desc = "common.id")
    })
    @Output({

    })
    @Description(desc = "nmtau.updateuserexportfileisreadapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        JSONArray idArray = paramObj.getJSONArray("idList");
        IUserExportFileCrossoverMapper userExportFileCrossoverMapper = CrossoverServiceFactory.getApi(IUserExportFileCrossoverMapper.class);
        List<Long> idList = new ArrayList<>();
        List<UserExportFileVo> userExportFileList = userExportFileCrossoverMapper.getUserExportFileListByIdList(idArray.toJavaList(Long.class));
        for (UserExportFileVo userExportFileVo : userExportFileList) {
            if (Objects.equals(userExportFileVo.getUserUuid(), UserContext.get().getUserUuid())) {
                idList.add(userExportFileVo.getId());
            }
        }
        if (CollectionUtils.isNotEmpty(idList)) {
            userExportFileCrossoverMapper.updateUserExportFileIsReadByIdList(idList);
        }
        int unreadCount = userExportFileCrossoverMapper.getUserExportFileUnreadCount(UserContext.get().getUserUuid());
        JSONObject resultObj = new JSONObject();
        resultObj.put("idList", idList);
        resultObj.put("unreadCount", unreadCount);
        return resultObj;
    }

    @Override
    public String getToken() {
        return "user/exportfile/isread/update";
    }
}

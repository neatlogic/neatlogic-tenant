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

package neatlogic.module.tenant.api.util;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.ADMIN;
import neatlogic.framework.rebuilddatabaseview.core.RebuildDataBaseViewManager;
import neatlogic.framework.rebuilddatabaseview.core.ViewStatusInfo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.TableResultUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@AuthAction(action = ADMIN.class)
@OperationType(type = OperationTypeEnum.OPERATE)
public class RebuildDataBaseViewApi extends PrivateApiComponentBase {
    @Override
    public String getName() {
        return "重建数据库视图";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({})
    @Output({
            @Param(name = "tbodyList", explode = ViewStatusInfo.class, desc = "视图列表")
    })
    @Description(desc = "重建数据库视图")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        List<ViewStatusInfo> resultList = RebuildDataBaseViewManager.execute();
        return TableResultUtil.getResult(resultList);
    }

    @Override
    public String getToken() {
        return "util/rebuilddatabaesview";
    }
}

/*Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

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

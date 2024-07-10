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

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.ADMIN;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.lrcode.LRCodeManager;
import neatlogic.framework.lrcode.dao.mapper.TreeMapper;
import neatlogic.framework.lrcode.dto.TreeNodeVo;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@Transactional
@AuthAction(action = ADMIN.class)
@OperationType(type = OperationTypeEnum.UPDATE)
public class LRCodeRebuildApi extends PrivateApiComponentBase {

    @Resource
    TreeMapper treeMapper;


    @Override
    public String getToken() {
        return "/util/lrcoderebuild";
    }

    @Override
    public String getName() {
        return "重建左右编码";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "tableName", type = ApiParamType.STRING, isRequired = true, desc = "表名"),
            @Param(name = "idKey", type = ApiParamType.STRING, isRequired = true, desc = "id字段名"),
            @Param(name = "parentIdKey", type = ApiParamType.STRING, isRequired = true, desc = "父id字段名"),
            @Param(name = "isRebuildUpwardPath", type = ApiParamType.BOOLEAN, desc = "是否重建全路径"),
            @Param(name = "sortKey", type = ApiParamType.STRING, desc = "排序字段名"),
            @Param(name = "condition", type = ApiParamType.STRING, desc = "条件")
    })
    @Description(desc = "重建左右编码接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String tableName = jsonObj.getString("tableName");
        String idKey = jsonObj.getString("idKey");
        String parentIdKey = jsonObj.getString("parentIdKey");
        String sortKey = jsonObj.getString("sortKey");
        String condition = jsonObj.getString("condition");
        LRCodeManager.rebuildLeftRightCodeOrderBySortKey(tableName, idKey, parentIdKey, condition, sortKey);
        if (jsonObj.getBooleanValue("isRebuildUpwardPath")) {
            TreeNodeVo treeNodeVo = new TreeNodeVo(tableName);
            treeMapper.updateUpwardIdPathByLftRht(treeNodeVo);
            treeMapper.updateUpwardNamePathByLftRht(treeNodeVo);
        }
        return null;
    }

}

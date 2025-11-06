/*
 * Copyright (C) 2025  深圳极向量科技有限公司 All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package neatlogic.module.tenant.api.changelog;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.NoAuth;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.dto.ChangelogAuditVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.TableResultUtil;
import neatlogic.module.tenant.dao.mapper.ChangelogAuditMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
@AuthAction(action = NoAuth.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class SearchChangeLogAuditListApi extends PrivateApiComponentBase {

    @Resource
    private ChangelogAuditMapper changelogAuditMapper;

    @Override
    public String getName() {
        return "nmtac.searchchangelogauditlistapi.getname";
    }

    @Input({
            @Param(name = "keyword", type = ApiParamType.STRING, desc = "common.keyword"),
            @Param(name = "moduleId", type = ApiParamType.STRING, desc = "term.cmdb.moduleid"),
            @Param(name = "sqlStatus", type = ApiParamType.ENUM, rule = "0,1", desc = "common.status"),
            @Param(name = "version", type = ApiParamType.STRING, desc = "common.versionnum"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "common.currentpage"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "common.pagesize"),
    })
    @Output({
            @Param(explode = BasePageVo.class),
            @Param(name = "tbodyList", type = ApiParamType.JSONARRAY, desc = "common.tbodylist")
    })
    @Description(desc = "查询数据库变更记录列表")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        List<ChangelogAuditVo> changelogAuditList = new ArrayList<>();
        ChangelogAuditVo changelogAuditVo = paramObj.toJavaObject(ChangelogAuditVo.class);
        changelogAuditVo.setTenantUuid(TenantContext.get().getTenantUuid());
        int rowNum = changelogAuditMapper.getChangelogAuditCount(changelogAuditVo);
        if (rowNum > 0) {
            changelogAuditVo.setRowNum(rowNum);
            changelogAuditList = changelogAuditMapper.getChangelogAuditList(changelogAuditVo);
        }
        return TableResultUtil.getResult(changelogAuditList, changelogAuditVo);
    }

    @Override
    public String getToken() {
        return "changelog/audit/search";
    }
}

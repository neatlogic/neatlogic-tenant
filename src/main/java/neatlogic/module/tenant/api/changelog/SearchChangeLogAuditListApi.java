/*
 *
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
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

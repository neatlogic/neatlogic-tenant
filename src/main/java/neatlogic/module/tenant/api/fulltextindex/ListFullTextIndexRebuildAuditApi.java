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

package neatlogic.module.tenant.api.fulltextindex;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.fulltextindex.core.FullTextIndexHandlerFactory;
import neatlogic.framework.fulltextindex.dao.mapper.FullTextIndexMapper;
import neatlogic.framework.fulltextindex.dao.mapper.FullTextIndexRebuildAuditMapper;
import neatlogic.framework.fulltextindex.dto.fulltextindex.FullTextIndexRebuildAuditVo;
import neatlogic.framework.fulltextindex.dto.fulltextindex.FullTextIndexTypeVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class ListFullTextIndexRebuildAuditApi extends PrivateApiComponentBase {
    @Resource
    private FullTextIndexRebuildAuditMapper fullTextIndexRebuildAuditMapper;

    @Resource
    private FullTextIndexMapper fullTextIndexMapper;

    @Override
    public String getToken() {
        return "fulltextindex/rebuildaudit/list";
    }

    @Override
    public String getName() {
        return "获取索引重建记录列表";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "typeList", type = ApiParamType.JSONARRAY, desc = "类型列表")})
    @Output({@Param(explode = FullTextIndexRebuildAuditVo[].class)})
    @Description(desc = "获取索引重建记录列表接口")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        FullTextIndexRebuildAuditVo audit = JSONObject.toJavaObject(paramObj, FullTextIndexRebuildAuditVo.class);
        List<FullTextIndexRebuildAuditVo> rebuildAuditList = fullTextIndexRebuildAuditMapper.searchFullTextIndexRebuildAudit(audit);
        List<FullTextIndexTypeVo> fullTextIndexTypeList = FullTextIndexHandlerFactory.getAllTypeList();
        for (FullTextIndexTypeVo typeVo : fullTextIndexTypeList) {
            Optional<FullTextIndexRebuildAuditVo> op = rebuildAuditList.stream().filter(d -> d.getType().equals(typeVo.getType())).findFirst();
            if (!op.isPresent()) {
                FullTextIndexRebuildAuditVo auditVo = new FullTextIndexRebuildAuditVo();
                auditVo.setType(typeVo.getType());
                auditVo.setTypeName(typeVo.getTypeName());
                auditVo.setIndexCount(0);
                rebuildAuditList.add(auditVo);
            } else {
                op.get().setTypeName(typeVo.getTypeName());
                op.get().setIndexCount(fullTextIndexMapper.getFullTextIndexCountByType(typeVo));
            }
        }
        return rebuildAuditList;
    }
}

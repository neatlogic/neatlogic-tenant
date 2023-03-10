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

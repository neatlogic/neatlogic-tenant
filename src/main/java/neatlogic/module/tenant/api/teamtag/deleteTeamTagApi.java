/*
 * Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.
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

package neatlogic.module.tenant.api.teamtag;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.REGION_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dao.mapper.teamtag.TeamTagMapper;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@AuthAction(action = REGION_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
@Transactional
public class deleteTeamTagApi extends PrivateApiComponentBase {
    @Resource
    TeamTagMapper teamTagMapper;

    @Override
    public String getName() {
        return "nmtat.deleteteamtagapi.getname";
    }


    @Input({
            @Param(name = "tagId", type = ApiParamType.LONG, desc = "common.tagid", isRequired = true, help = "标签id"),
    })
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        Long tagId = paramObj.getLong("tagId");
        teamTagMapper.deleteTeamTag(tagId);
        return null;
    }

    @Override
    public String getToken() {
        return "/team/tag/delete";
    }
}

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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.REGION_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dao.mapper.teamtag.TeamTagMapper;
import neatlogic.framework.dto.teamtag.TeamTagVo;
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
public class SaveTeamTagApi extends PrivateApiComponentBase {
    @Resource
    TeamTagMapper teamTagMapper;

    @Override
    public String getName() {
        return "nmtat.saveteamtagapi.getname";
    }


    @Input({
            @Param(name = "matrixUuid", type = ApiParamType.STRING, desc = "term.framework.matrixuuid", isRequired = true, help = "矩阵uuid"),
            @Param(name = "matrixType", type = ApiParamType.STRING, desc = "term.framework.matrixtype", isRequired = true, help = "矩阵uuid"),
            @Param(name = "matrixAttr", type = ApiParamType.STRING, desc = "term.framework.matrixattr", isRequired = true, help = "矩阵属性"),
            @Param(name = "matrixAttrValues", type = ApiParamType.JSONARRAY, desc = "nmtat.saveteamtagapi.input.param.desc.matrixattrvalue", isRequired = true, minSize = 1, help = "矩阵属性值"),
    })
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        JSONArray matrixAttrValues = paramObj.getJSONArray("matrixAttrValues");
        for (int i = 0; i < matrixAttrValues.size(); i++) {
            TeamTagVo teamTagVo = JSON.toJavaObject(paramObj, TeamTagVo.class);
            teamTagVo.setMatrixAttrValue(matrixAttrValues.getString(i));
            teamTagMapper.insertTeamTag(teamTagVo);
        }
        return null;
    }

    @Override
    public String getToken() {
        return "/team/tag/save";
    }
}

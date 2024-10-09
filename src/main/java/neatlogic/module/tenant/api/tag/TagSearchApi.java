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

package neatlogic.module.tenant.api.tag;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dto.TagVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.tenant.service.TagService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class TagSearchApi extends PrivateApiComponentBase {

    @Resource
    private TagService tagService;

    @Override
    public String getToken() {
        return "tag/search";
    }

    @Override
    public String getName() {
        return "标签查询接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "name", desc = "标签名称", type = ApiParamType.STRING, xss = true),
            @Param(name = "type", desc = "类型", type = ApiParamType.STRING, isRequired = true)
    })

    @Output({
            @Param(name = "tagList", desc = "标签集合", explode = TagVo[].class)
    })
    @Description(desc = "标签查询接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject returnObj = new JSONObject();
        String tagName = jsonObj.getString("name");
        String type = jsonObj.getString("type");
        TagVo tagVo = new TagVo();
        tagVo.setName(tagName);
        tagVo.setType(type);
        List<TagVo> tagList = tagService.searchTag(tagVo);
        returnObj.put("tagList", tagList);
        return returnObj;
    }
}

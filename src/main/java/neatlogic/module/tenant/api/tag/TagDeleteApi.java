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

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dto.TagVo;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.tenant.service.TagService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Deprecated
@Service

@OperationType(type = OperationTypeEnum.DELETE)
public class TagDeleteApi extends PrivateApiComponentBase {

    @Autowired
    private TagService tagService;

    @Override
    public String getToken() {
        return "tag/delete";
    }

    @Override
    public String getName() {
        return "标签删除接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param( name = "tagIdList", desc = "标签ID集合", type = ApiParamType.JSONARRAY, isRequired = true)
    })
    @Description(desc = "标签删除接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONArray tagIdList = jsonObj.getJSONArray("tagIdList");
        for (int i = 0; i < tagIdList.size(); i++){
            Long tagId = tagIdList.getLong(i);
            TagVo tag = new TagVo();
            tag.setId(tagId);
            tagService.deleteTag(tag);
        }
        return null;
    }
}

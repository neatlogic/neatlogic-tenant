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

package neatlogic.module.tenant.api.tag;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dto.TagVo;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.tenant.service.TagService;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Deprecated
@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class TagSearchApi extends PrivateApiComponentBase {

    @Autowired
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
            @Param(name = "name", desc = "标签名称", type = ApiParamType.STRING, isRequired = true, xss = true)
    })

    @Output({
            @Param(name = "tagList", desc = "标签集合", explode = TagVo[].class)
    })
    @Description( desc = "标签查询接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject returnObj = new JSONObject();
        String tagName = jsonObj.getString("name");
        TagVo tagVo = new TagVo();
        tagVo.setName(tagName);
        List<TagVo> tagList = tagService.searchTag(tagVo);
        returnObj.put("tagList", tagList);
        return returnObj;
    }
}

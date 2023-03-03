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
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.tenant.service.TagService;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Deprecated
@Service

@OperationType(type = OperationTypeEnum.CREATE)
public class TagSaveApi extends PrivateApiComponentBase {

    @Autowired
    private TagService tagService;

    @Override
    public String getToken() {
        return "tag/save";
    }

    @Override
    public String getName() {
        return "标签保存接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "id", type = ApiParamType.LONG, desc = "标签id"),
            @Param(name = "name", type = ApiParamType.STRING, desc = "标签名称",isRequired = true, xss = true)
    })
    @Description( desc = "标签保存接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject returnObj = new JSONObject();
        TagVo tag = new TagVo();
        if (jsonObj.containsKey("id")){
            tag.setId(jsonObj.getLong("id"));
        }
        if (jsonObj.containsKey("name")){
            tag.setName(jsonObj.getString("name"));
        }
        tagService.saveTag(tag);
        returnObj.put("id", tag.getId());
        return returnObj;
    }
}

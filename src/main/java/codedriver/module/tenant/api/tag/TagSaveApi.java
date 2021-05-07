/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.tag;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dto.TagVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.tenant.service.TagService;
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

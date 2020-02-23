package codedriver.module.tenant.api.tag;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.dto.TagVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.tenant.service.TagService;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TagSaveApi extends ApiComponentBase {

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
            @Param(name = "name", type = ApiParamType.STRING, desc = "标签名称",isRequired = true)
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

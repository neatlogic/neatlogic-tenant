package codedriver.module.tenant.api.tag;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dto.TagVo;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.tenant.service.TagService;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
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

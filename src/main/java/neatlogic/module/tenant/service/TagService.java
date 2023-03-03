package neatlogic.module.tenant.service;

import neatlogic.framework.dto.TagVo;

import java.util.List;

public interface TagService {
    public int saveTag(TagVo tagVo);

    public int deleteTag(TagVo tagVo);

    public List<TagVo> searchTag(TagVo tagVo);
}

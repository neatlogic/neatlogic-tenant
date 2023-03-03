package neatlogic.module.tenant.service;

import neatlogic.framework.dao.mapper.TagMapper;
import neatlogic.framework.dto.TagVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TagServiceImpl implements TagService {

    @Autowired
    private TagMapper tagMapper;

    @Override
    public int saveTag(TagVo tagVo) {
        if (tagVo.getId() != null && tagVo.getId() != 0L){
            tagMapper.updateTag(tagVo);
        }else {
            tagMapper.insertTag(tagVo);
        }
        return 0;
    }

    @Override
    public int deleteTag(TagVo tagVo) {
        tagMapper.deleteTagById(tagVo.getId());
        return 0;
    }

    @Override
    public List<TagVo> searchTag(TagVo tagVo) {
        return tagMapper.searchTag(tagVo);
    }
}

package codedriver.module.tenant.dao.mapper;

import java.util.HashMap;
import java.util.List;

public interface TestMapper {
    public List<HashMap> testBg();

    String getContent(String hash);

    void insertContent(String content);

}

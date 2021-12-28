/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.globalsearch.documenttype;

import codedriver.framework.fulltextindex.core.FullTextIndexHandlerFactory;
import codedriver.framework.fulltextindex.dto.fulltextindex.FullTextIndexTypeVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;

import java.util.stream.Collectors;

//@Service
//@OperationType(type = OperationTypeEnum.SEARCH)
public class ListDocumentTypeApi extends PrivateApiComponentBase {

    @Override
    public String getToken() {
        return "globalsearch/document/type/list";
    }

    @Override
    public String getName() {
        return "获取全局搜索文档类型";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Output({@Param(explode = FullTextIndexTypeVo[].class)})
    @Description(desc = "获取全局搜索文档类型接口")
    @Override
    public Object myDoService(JSONObject jsonObj) {
        return FullTextIndexHandlerFactory.getAllTypeList().stream().filter(FullTextIndexTypeVo::isActiveGlobalSearch).collect(Collectors.toList());
    }

}

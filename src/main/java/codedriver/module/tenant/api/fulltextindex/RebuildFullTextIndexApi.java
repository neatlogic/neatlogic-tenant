/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.fulltextindex;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.fulltextindex.core.FullTextIndexHandlerFactory;
import codedriver.framework.fulltextindex.core.IFullTextIndexHandler;
import codedriver.framework.fulltextindex.dao.mapper.FullTextIndexRebuildAuditMapper;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.tenant.exception.fulltextindex.FullTextIndexHandlerNotFoundException;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@OperationType(type = OperationTypeEnum.OPERATE)
public class RebuildFullTextIndexApi extends PrivateApiComponentBase {
    @Resource
    private FullTextIndexRebuildAuditMapper fullTextIndexRebuildAuditMapper;

    @Override
    public String getToken() {
        return "fulltextindex/rebuild";
    }

    @Override
    public String getName() {
        return "重建检索索引";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "type", desc = "索引类型", type = ApiParamType.STRING, isRequired = true),
            @Param(name = "isAll", desc = "是否全部重建", type = ApiParamType.BOOLEAN, isRequired = true)})
    @Description(desc = "重建检索索引接口")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        String type = paramObj.getString("type");
        boolean isAll = paramObj.getBooleanValue("isAll");
        IFullTextIndexHandler handler = FullTextIndexHandlerFactory.getHandler(type);
        if (handler == null) {
            throw new FullTextIndexHandlerNotFoundException(type);
        }
        handler.rebuildIndex(type, isAll);
        return null;
    }
}

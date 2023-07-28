/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.integration;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.auth.label.INTEGRATION_MODIFY;
import codedriver.framework.common.config.Config;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.crossover.CrossoverServiceFactory;
import codedriver.framework.crossover.IFileCrossoverService;
import codedriver.framework.exception.file.FilePathIllegalException;
import codedriver.framework.file.dto.AuditFilePathVo;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@AuthAction(action = INTEGRATION_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class IntegrationAuditDetailGetApi extends PrivateApiComponentBase {

    @Override
    public String getToken() {
        return "integration/audit/detail/get";
    }

    @Override
    public String getName() {
        return "获取集成管理审计内容";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "filePath", type = ApiParamType.STRING, desc = "文件路径", isRequired = true)
    })
    @Output({
            @Param(name = "content", type = ApiParamType.STRING, desc = "内容"),
            @Param(name = "hasMore", type = ApiParamType.BOOLEAN, desc = "是否还有更多内容")
    })
    @Description(desc = "获取集成管理审计内容")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {

        String filePath = paramObj.getString("filePath");
        if (!filePath.contains("integrationaudit")) {
            throw new FilePathIllegalException(filePath);
        }
        AuditFilePathVo auditFilePathVo = new AuditFilePathVo(filePath);
        IFileCrossoverService fileCrossoverService = CrossoverServiceFactory.getApi(IFileCrossoverService.class);
        if (Objects.equals(auditFilePathVo.getServerId(), Config.SCHEDULE_SERVER_ID)) {
            return fileCrossoverService.readLocalFile(auditFilePathVo.getPath(), auditFilePathVo.getStartIndex(), auditFilePathVo.getOffset());
        } else {
            return fileCrossoverService.readRemoteFile(paramObj, auditFilePathVo.getServerId());
        }
    }
}

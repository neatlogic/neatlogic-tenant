package neatlogic.module.tenant.api.mailserver;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.restful.annotation.Mcp;
import javax.annotation.Resource;
import neatlogic.framework.auth.label.NOTIFY_CONFIG_MODIFY;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.ADMIN;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.tenant.service.McpMailService;
import org.springframework.stereotype.Service;

/** 复用所属模块服务的 MCP 入口，权限和参数检查仍走原 API 执行链。 */
@Service
@Mcp
@AuthAction(action=NOTIFY_CONFIG_MODIFY.class)
@AuthAction(action=ADMIN.class)
@OperationType(type=OperationTypeEnum.OPERATE)
public class SendMcpMailApi extends PrivateApiComponentBase {
    @Resource private McpMailService mailService;
    /** 返回稳定的业务能力标识。 */
    @Override public String getToken() { return "mail/send"; }
    /** 返回所属模块的接口名称。 */
    @Override public String getName() { return "nmai.aitool.sendemailtool.label"; }
    /** 接口配置沿用平台 API 管理。 */
    @Override public String getConfig() { return null; }
    /** API 帮助与 MCP 共用所属模块的场景说明。 */
    @Override public String getDescription() { return "tenant.mcp.mailhelp"; }

    @Input({
        @Param(name="subject", type=ApiParamType.STRING, desc="nmai.aitool.sendemailtool.param.subject", isRequired=true),
        @Param(name="body", type=ApiParamType.STRING, desc="nmai.aitool.sendemailtool.param.body", isRequired=true),
        @Param(name="from", type=ApiParamType.STRING, desc="nmai.aitool.sendemailtool.param.from"),
        @Param(name="password", type=ApiParamType.STRING, desc="nmai.aitool.sendemailtool.param.password"),
        @Param(name="to", type=ApiParamType.STRING, desc="nmai.aitool.sendemailtool.param.to", isRequired=true),
        @Param(name="cc", type=ApiParamType.STRING, desc="nmai.aitool.sendemailtool.param.cc")
    })
    @Output({})
    @Example(title="tenant.mcpexamples.mail.title", description="tenant.mcpexamples.mail.description", example="{\"subject\":\"Operations Report\",\"body\":\"Replace with the approved report content\",\"to\":\"recipient@example.com\",\"cc\":\"reviewer@example.com\"}")
    @Description(desc="tenant.mcp.mailhelp")
    /** 参数经公共 API 校验后委托原业务服务。 */
    @Override public Object myDoService(JSONObject input) throws Exception {
        return mailService.send(input);
    }
}

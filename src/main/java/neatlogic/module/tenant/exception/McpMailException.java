package neatlogic.module.tenant.exception;
import neatlogic.framework.exception.core.ApiRuntimeException;
/** 保留原业务失败原因，不将已知错误转换为未知异常。 */
public class McpMailException extends ApiRuntimeException {
    /** 原因只包含可展示的业务上下文，不携带凭据。 */
    public McpMailException(String reason) { super("tenant.mcp.mail.failed", reason); }
}

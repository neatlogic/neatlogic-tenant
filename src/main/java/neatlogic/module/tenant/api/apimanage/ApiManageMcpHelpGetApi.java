package neatlogic.module.tenant.api.apimanage;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.INTERFACE_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.exception.type.ApiNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentFactory;
import neatlogic.framework.restful.dao.mapper.ApiMapper;
import neatlogic.framework.restful.dto.ApiVo;
import neatlogic.framework.restful.enums.ApiType;
import neatlogic.framework.restful.mcp.McpToolMetadataBuilder;
import neatlogic.framework.util.$;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

@Service
@AuthAction(action = INTERFACE_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ApiManageMcpHelpGetApi extends PrivateApiComponentBase {

    @Resource
    private ApiMapper apiMapper;

    @Override
    public String getToken() {
        return "apimanage/mcp/help/get";
    }

    @Override
    public String getName() {
        return "接口MCP服务说明获取接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "token", type = ApiParamType.STRING, isRequired = true, xss = true, desc = "接口token")
    })
    @Output({
            @Param(name = "Return", type = ApiParamType.JSONOBJECT, desc = "MCP服务说明")
    })
    @Description(desc = "接口MCP服务说明获取接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        // MCP说明需要同时展示保存配置和当前运行时组件状态，便于前端做可用性诊断。
        String token = normalizeToken(jsonObj.getString("token"));
        ApiVo ramApi = PrivateApiComponentFactory.getApiByToken(token);
        ApiVo dbApi = apiMapper.getApiByTokenWithoutPsw(token);
        if (ramApi == null && dbApi == null) {
            throw new ApiNotFoundException(token);
        }

        ApiVo api = buildApiForMetadata(ramApi, dbApi);
        JSONObject resultObj = new JSONObject();
        resultObj.put("enabled", dbApi != null && Objects.equals(dbApi.getIsMcp(), 1));
        resultObj.put("available", isAvailable(ramApi, dbApi));
        resultObj.put("unavailableReason", getUnavailableReason(ramApi, dbApi));
        resultObj.put("endpoint", "/api/mcp");
        resultObj.put("scopedEndpoint", StringUtils.isBlank(api.getModuleGroup()) ? "/api/mcp" : "/api/mcp/" + api.getModuleGroup());
        resultObj.put("token", api.getToken());
        resultObj.put("toolName", McpToolMetadataBuilder.getToolName(api));
        resultObj.put("title", $.t(api.getName()));
        resultObj.put("description", api.getDescription());
        resultObj.put("moduleId", api.getModuleId());
        resultObj.put("moduleGroup", api.getModuleGroup());
        resultObj.put("moduleGroupName", api.getModuleGroupName());

        JSONObject toolObj = McpToolMetadataBuilder.buildTool(api);
        resultObj.put("inputSchema", toolObj.getJSONObject("inputSchema"));
        resultObj.put("outputSchema", toolObj.getJSONObject("outputSchema"));
        resultObj.put("annotations", toolObj.getJSONObject("annotations"));
        resultObj.put("meta", toolObj.getJSONObject("_meta"));
        resultObj.put("example", McpToolMetadataBuilder.getExample(api));
        resultObj.put("initializeExample", getInitializeExample());
        resultObj.put("listToolsExample", getListToolsExample(api.getModuleGroup()));
        resultObj.put("callToolExample", getCallToolExample(api));
        return resultObj;
    }

    private String normalizeToken(String token) {
        String normalizedToken = StringUtils.trimToEmpty(token);
        if (normalizedToken.startsWith("/")) {
            normalizedToken = normalizedToken.substring(1);
        }
        if (normalizedToken.endsWith("/")) {
            normalizedToken = normalizedToken.substring(0, normalizedToken.length() - 1);
        }
        return normalizedToken;
    }

    private ApiVo buildApiForMetadata(ApiVo ramApi, ApiVo dbApi) throws CloneNotSupportedException {
        // 元数据以运行时API为准，再叠加DB里的MCP开关和审计等管理配置。
        ApiVo api = ramApi != null ? ramApi.clone() : dbApi;
        if (dbApi != null) {
            api.setIsMcp(dbApi.getIsMcp());
            api.setIsActive(dbApi.getIsActive());
            api.setNeedAudit(dbApi.getNeedAudit());
            api.setQps(dbApi.getQps());
        }
        return api;
    }

    private boolean isAvailable(ApiVo ramApi, ApiVo dbApi) {
        return dbApi != null
                && Objects.equals(dbApi.getIsMcp(), 1)
                && Objects.equals(dbApi.getIsActive(), 1)
                && ramApi != null
                && Objects.equals(ApiType.OBJECT.getValue(), ramApi.getType());
    }

    private String getUnavailableReason(ApiVo ramApi, ApiVo dbApi) {
        if (dbApi == null) {
            return "接口尚未保存配置";
        }
        if (!Objects.equals(dbApi.getIsMcp(), 1)) {
            return "接口未启用MCP服务";
        }
        if (!Objects.equals(dbApi.getIsActive(), 1)) {
            return "接口未激活";
        }
        if (ramApi == null) {
            return "接口运行时组件不存在";
        }
        if (!Objects.equals(ApiType.OBJECT.getValue(), ramApi.getType())) {
            return "MCP服务仅支持object类型接口";
        }
        return null;
    }

    private JSONObject getInitializeExample() {
        JSONObject requestObj = new JSONObject();
        requestObj.put("jsonrpc", "2.0");
        requestObj.put("id", 1);
        requestObj.put("method", "initialize");
        JSONObject paramsObj = new JSONObject();
        paramsObj.put("protocolVersion", "2025-11-25");
        requestObj.put("params", paramsObj);
        return requestObj;
    }

    private JSONObject getListToolsExample(String moduleGroup) {
        JSONObject requestObj = new JSONObject();
        requestObj.put("jsonrpc", "2.0");
        requestObj.put("id", 2);
        requestObj.put("method", "tools/list");
        requestObj.put("endpoint", StringUtils.isBlank(moduleGroup) ? "/api/mcp" : "/api/mcp/" + moduleGroup);
        return requestObj;
    }

    private JSONObject getCallToolExample(ApiVo api) {
        JSONObject requestObj = new JSONObject();
        requestObj.put("jsonrpc", "2.0");
        requestObj.put("id", 3);
        requestObj.put("method", "tools/call");
        JSONObject paramsObj = new JSONObject();
        paramsObj.put("name", McpToolMetadataBuilder.getToolName(api));
        paramsObj.put("arguments", new JSONObject());
        requestObj.put("params", paramsObj);
        return requestObj;
    }
}

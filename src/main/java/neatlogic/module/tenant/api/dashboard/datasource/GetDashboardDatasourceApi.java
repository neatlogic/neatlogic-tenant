package neatlogic.module.tenant.api.dashboard.datasource;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.NoAuth;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dashboard.datasource.core.DashboardDatasourceHandlerFactory;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.springframework.stereotype.Service;

/** 大屏数据源详情接口，保留当前 NoAuth 契约，底层查询交由 Handler 工厂按类型分发。 */
@Service
@AuthAction(action = NoAuth.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class GetDashboardDatasourceApi extends PrivateApiComponentBase {
    /** 提供租户模块的大屏统一接口地址，隔离底层数据源实现。 */
    @Override
    public String getToken() {
        return "dashboard/datasource/get";
    }

    /** 接口名称使用租户模块国际化资源。 */
    @Override
    public String getName() {
        return "nmtadd.dashboarddatasource.get";
    }

    /** 沿用普通查询接口的默认配置。 */
    @Override
    public String getConfig() {
        return null;
    }

    /** 详情使用字符串 ID 兼容集成 UUID 和原数值 ID，返回通用 fieldList；来源类型由工厂处理。 */
    @Input({
            @Param(name = "datasourceType", type = ApiParamType.STRING, desc = "nmtadd.dashboarddatasource.type"),
            @Param(name = "id", type = ApiParamType.STRING, desc = "common.id", isRequired = true)
    })
    @Output({@Param(name = "fieldList", type = ApiParamType.JSONARRAY, desc = "nmtadd.dashboarddatasource.fieldlist",
            help = "nmtadd.dashboarddatasource.fieldlisthelp")})
    @Description(desc = "nmtadd.dashboarddatasource.get")
    @Override
    public JSONObject myDoService(JSONObject jsonObj) {
        return DashboardDatasourceHandlerFactory.getHandler(jsonObj.getString("datasourceType")).getDatasource(jsonObj);
    }
}


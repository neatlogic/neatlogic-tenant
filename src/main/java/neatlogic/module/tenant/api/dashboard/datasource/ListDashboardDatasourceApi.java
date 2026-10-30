package neatlogic.module.tenant.api.dashboard.datasource;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.NoAuth;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.dashboard.datasource.core.DashboardDatasourceHandlerFactory;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.springframework.stereotype.Service;

/** 大屏数据源列表接口，保留当前 NoAuth 契约，底层查询交由 Handler 工厂按类型分发。 */
@Service
@AuthAction(action = NoAuth.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ListDashboardDatasourceApi extends PrivateApiComponentBase {
    /** 提供租户模块的大屏统一接口地址，隔离底层数据源实现。 */
    @Override
    public String getToken() {
        return "dashboard/datasource/list";
    }

    /** 接口名称使用租户模块国际化资源。 */
    @Override
    public String getName() {
        return "nmtadd.dashboarddatasource.list";
    }

    /** 沿用普通查询接口的默认配置。 */
    @Override
    public String getConfig() {
        return null;
    }

    /** 保留原查询参数和返回结构，缺省类型由工厂兼容旧大屏，未知类型明确拒绝。 */
    @Input({
            @Param(name = "datasourceType", type = ApiParamType.STRING, desc = "nmtadd.dashboarddatasource.type"),
            @Param(name = "moduleId", type = ApiParamType.STRING, desc = "nfdd.datasourcevo.entityfield.name.moduleid"),
            @Param(name = "keyword", type = ApiParamType.STRING, desc = "common.keyword", xss = true),
            @Param(name = "defaultValue", type = ApiParamType.JSONARRAY, desc = "nmtadd.dashboarddatasource.defaultvalue"),
            @Param(name = "idList", type = ApiParamType.JSONARRAY, desc = "nmtadd.dashboarddatasource.idlist"),
            @Param(name = "isActive", type = ApiParamType.INTEGER, desc = "common.isactive"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "common.currentpage"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "common.pagesize")
    })
    // 不展开数据仓库 DTO，列表选项以通用 id、label 兼容数值 ID 和集成 UUID。
    @Output({@Param(explode = BasePageVo.class), @Param(name = "tbodyList", type = ApiParamType.JSONARRAY,
            desc = "nmtadd.dashboarddatasource.tbodylist", help = "nmtadd.dashboarddatasource.tbodylisthelp")})
    @Description(desc = "nmtadd.dashboarddatasource.list")
    @Override
    public Object myDoService(JSONObject jsonObj) {
        return DashboardDatasourceHandlerFactory.getHandler(jsonObj.getString("datasourceType")).listDatasource(jsonObj);
    }
}


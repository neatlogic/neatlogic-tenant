package neatlogic.module.tenant.api.dashboard.datasource;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.NoAuth;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.datawarehouse.dto.DataSourceVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.tenant.service.dashboard.DashboardDatasourceService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/** 大屏数据源详情接口，仅使用报表基础权限，底层查询交由数据源服务分发。 */
@Service
@AuthAction(action = NoAuth.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class GetDashboardDatasourceApi extends PrivateApiComponentBase {
    @Resource
    private DashboardDatasourceService dashboardDatasourceService;

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

    /** 保留原查询参数和返回结构，缺省类型由服务层兼容旧大屏。 */
    @Input({
            @Param(name = "datasourceType", type = ApiParamType.STRING, desc = "nmtadd.dashboarddatasource.type"),
            @Param(name = "id", type = ApiParamType.LONG, desc = "common.id", isRequired = true)
    })
    @Output({@Param(explode = DataSourceVo.class)})
    @Description(desc = "nmtadd.dashboarddatasource.get")
    @Override
    public Object myDoService(JSONObject jsonObj) {
        return dashboardDatasourceService.getDatasource(jsonObj);
    }
}


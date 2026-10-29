package neatlogic.module.tenant.api.dashboard.datasource;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.NoAuth;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.datawarehouse.dto.DataSourceVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.tenant.service.dashboard.DashboardDatasourceService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/** 大屏数据源列表接口，仅使用报表基础权限，底层查询交由数据源服务分发。 */
@Service
@AuthAction(action = NoAuth.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ListDashboardDatasourceApi extends PrivateApiComponentBase {
    @Resource
    private DashboardDatasourceService dashboardDatasourceService;

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

    /** 保留原查询参数和返回结构，缺省类型由服务层兼容旧大屏。 */
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
    @Output({@Param(explode = BasePageVo.class), @Param(name = "tbodyList", explode = DataSourceVo[].class)})
    @Description(desc = "nmtadd.dashboarddatasource.list")
    @Override
    public Object myDoService(JSONObject jsonObj) {
        return dashboardDatasourceService.listDatasource(jsonObj);
    }
}


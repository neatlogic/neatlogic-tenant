package neatlogic.module.tenant.api.dashboard.datasource;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.NoAuth;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.tenant.service.dashboard.DashboardDatasourceService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/** 大屏数据源取数接口，仅使用报表基础权限，底层查询交由数据源服务分发。 */
@Service
@AuthAction(action = NoAuth.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class SearchDashboardDatasourceDataApi extends PrivateApiComponentBase {
    @Resource
    private DashboardDatasourceService dashboardDatasourceService;

    /** 提供租户模块的大屏统一接口地址，隔离底层数据源实现。 */
    @Override
    public String getToken() {
        return "dashboard/datasource/data/search";
    }

    /** 接口名称使用租户模块国际化资源。 */
    @Override
    public String getName() {
        return "nmtadd.dashboarddatasource.searchdata";
    }

    /** 沿用普通查询接口的默认配置。 */
    @Override
    public String getConfig() {
        return null;
    }

    /** 保留原查询参数和返回结构，缺省类型由服务层兼容旧大屏。 */
    @Input({
            @Param(name = "datasourceType", type = ApiParamType.STRING, desc = "nmtadd.dashboarddatasource.type"),
            @Param(name = "dataSourceId", type = ApiParamType.LONG, desc = "nmtad.searchdatasourcedataapi.input.param.desc.datasourceid", isRequired = true),
            @Param(name = "conditionList", type = ApiParamType.JSONARRAY, desc = "nmtad.searchdatasourcedataapi.input.param.desc.conditionlist"),
            @Param(name = "sortList", type = ApiParamType.JSONARRAY, desc = "nmtad.searchdatasourcedataapi.input.param.desc.sortlist"),
            @Param(name = "limit", type = ApiParamType.INTEGER, desc = "nmtad.searchdatasourcedataapi.input.param.desc.limit"),
            @Param(name = "isExpired", type = ApiParamType.INTEGER, desc = "nmtad.searchdatasourcedataapi.input.param.desc.isexpired"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "common.currentpage"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "common.pagesize")
    })
    @Output({@Param(explode = BasePageVo.class), @Param(name = "theadList", type = ApiParamType.JSONARRAY), @Param(name = "tbodyList", type = ApiParamType.JSONARRAY)})
    @Description(desc = "nmtadd.dashboarddatasource.searchdata")
    @Override
    public Object myDoService(JSONObject jsonObj) {
        return dashboardDatasourceService.searchDatasourceData(jsonObj);
    }
}


package neatlogic.module.tenant.api.dashboard.datasource;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.NoAuth;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.ValueTextVo;
import neatlogic.framework.dashboard.datasource.core.DashboardDatasourceHandlerFactory;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.springframework.stereotype.Service;

/** 查询已注册的大屏数据源类型，仅返回选项，不执行具体数据源查询。 */
@Service
@AuthAction(action = NoAuth.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ListDashboardDatasourceTypeApi extends PrivateApiComponentBase {
    /** 类型列表使用独立路径，保留原具体数据源列表接口的缺省行为。 */
    @Override
    public String getToken() {
        return "dashboard/datasource/type/list";
    }

    /** 接口名称由租户模块的国际化资源解析。 */
    @Override
    public String getName() {
        return "nmtadd.dashboarddatasource.typelist";
    }

    /** 沿用普通查询接口的默认配置。 */
    @Override
    public String getConfig() {
        return null;
    }

    /** 每次读取工厂中的类型和当前语言名称，业务对象只包含 tbodyList，不增加分页字段。 */
    @Input({})
    @Output({@Param(name = "tbodyList", type = ApiParamType.JSONARRAY, explode = ValueTextVo[].class,
            desc = "nmtadd.dashboarddatasource.typeoptions", help = "nmtadd.dashboarddatasource.typeoptionshelp")})
    @Description(desc = "nmtadd.dashboarddatasource.typelist")
    @Override
    public JSONObject myDoService(JSONObject jsonObj) {
        JSONObject result = new JSONObject();
        result.put("tbodyList", DashboardDatasourceHandlerFactory.getTypeList());
        return result;
    }
}

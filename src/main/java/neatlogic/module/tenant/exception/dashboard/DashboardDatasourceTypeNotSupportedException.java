package neatlogic.module.tenant.exception.dashboard;

import neatlogic.framework.exception.core.ApiRuntimeException;

/** 未接入的数据源类型必须明确拒绝，不能回退到数据仓库查询。 */
public class DashboardDatasourceTypeNotSupportedException extends ApiRuntimeException {
    private static final long serialVersionUID = 1L;

    /** 使用租户模块的国际化消息说明不支持的类型。 */
    public DashboardDatasourceTypeNotSupportedException(String datasourceType) {
        super("nmted.dashboarddatasourcetypenotsupportedexception.message", datasourceType);
    }
}

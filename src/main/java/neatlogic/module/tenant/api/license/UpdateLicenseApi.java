/*
 * Copyright(c) 2022 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package neatlogic.module.tenant.api.license;

import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dao.mapper.LicenseMapper;
import neatlogic.framework.dao.mapper.TenantMapper;
import neatlogic.framework.dto.TenantVo;
import neatlogic.framework.license.LicenseManager;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.ApiAnonymousAccessSupportEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class UpdateLicenseApi extends PrivateApiComponentBase {

    @Resource
    LicenseMapper licenseMapper;

    @Resource
    TenantMapper tenantMapper;

    @Override
    public String getName() {
        return "更新许可";
    }

    @Override
    public String getToken() {
        return "license/update";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "license", type = ApiParamType.STRING, desc = "许可密钥")
    })
    @Output({
    })
    @Description(desc = "更新许可接口")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        String licenseStr = paramObj.getString("license");
        String tenantUuid = TenantContext.get().getTenantUuid();
        TenantContext.get().setUseDefaultDatasource(true);
        TenantVo tenantVo = tenantMapper.getTenantByUuid(tenantUuid);
        if (StringUtils.isBlank(licenseStr)) {
            licenseStr = licenseMapper.getTenantLicenseByTenantUuid(tenantVo.getUuid());
        }
        LicenseManager.initLicenseVo(tenantVo.getUuid(), licenseStr);
        if (StringUtils.isNotBlank(paramObj.getString("license"))) {
            licenseMapper.insertLicenseByTenantUuid(tenantVo.getId(), tenantVo.getUuid(), licenseStr);
        }
        TenantContext.get().setUseDefaultDatasource(false);
        return null;
    }

    @Override
    public ApiAnonymousAccessSupportEnum supportAnonymousAccess() {
        return ApiAnonymousAccessSupportEnum.ANONYMOUS_ACCESS_WITHOUT_ENCRYPTION;
    }
}

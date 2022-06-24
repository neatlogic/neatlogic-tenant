/*
 * Copyright (c)  2021 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.license;

import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.auth.core.AuthBase;
import codedriver.framework.auth.core.AuthFactory;
import codedriver.framework.auth.label.LICENSE_MODIFY;
import codedriver.framework.common.util.ModuleUtil;
import codedriver.framework.dao.mapper.LicenseMapper;
import codedriver.framework.dao.mapper.TenantMapper;
import codedriver.framework.dto.ModuleGroupVo;
import codedriver.framework.dto.TenantVo;
import codedriver.framework.dto.license.LicenseAuthModuleGroupVo;
import codedriver.framework.dto.license.LicenseAuthVo;
import codedriver.framework.dto.license.LicenseVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.util.TimeUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@AuthAction(action = LICENSE_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
@Service
public class GetLicenseApi extends PrivateApiComponentBase {

    @Resource
    TenantMapper tenantMapper;

    @Resource
    LicenseMapper licenseMapper;

    @Override
    public String getName() {
        return "获取license";
    }

    @Override
    public String getToken() {
        return "license/get";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({

    })
    @Output({
    })
    @Description(desc = "获取license接口")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        JSONObject result = new JSONObject();
        Map<String, String> moduleGroupMap = ModuleUtil.getAllModuleGroupList().stream().collect(Collectors.toMap(o -> o.getGroup().toUpperCase(Locale.ROOT), ModuleGroupVo::getGroupName));
        ;
        LicenseVo licenseVo = TenantContext.get().getLicenseVo();
        String tenant = TenantContext.get().getTenantUuid();
        TenantContext.get().setUseDefaultDatasource(true);
        TenantVo tenantVo = tenantMapper.getTenantByUuid(tenant);
        result.put("purchaser", licenseVo.getPurchaser());
        result.put("tenantUuid", tenant);
        result.put("tenant", tenantVo.getName());
        result.put("issueTime", TimeUtil.convertDateToString(licenseVo.getIssueTime(), TimeUtil.YYYY_MM_DD));
        result.put("expiredTime", TimeUtil.convertDateToString(licenseVo.getExpireTime(), TimeUtil.YYYY_MM_DD));
        result.put("stopServiceTime", TimeUtil.addDateByDay(licenseVo.getExpireTime(), licenseVo.getExpiredDay(), TimeUtil.YYYY_MM_DD));
        LicenseAuthVo licenseAuthVo = licenseVo.getAuth();
        List<LicenseAuthModuleGroupVo> licenseAuthModuleGroupVoList = licenseAuthVo.getModuleGroupList();
        JSONArray moduleGroupArray = new JSONArray();
        result.put("moduleGroupList", moduleGroupArray);
        Optional<LicenseAuthModuleGroupVo> allModuleOptional = licenseAuthModuleGroupVoList.stream().filter(o -> Objects.equals("ALL", o.getName().toUpperCase(Locale.ROOT))).findFirst();
        if (allModuleOptional.isPresent()) {
            JSONObject moduleGroupJson = new JSONObject();
            moduleGroupJson.put("moduleGroup", "ALL");
            moduleGroupJson.put("moduleGroupName", "所有");
            moduleGroupArray.add(moduleGroupJson);
            initAuth(new ArrayList<>(), moduleGroupJson, true);
            List<String> operationList = allModuleOptional.get().getOperationTypeList();
            initOperation(operationList, moduleGroupJson, true);
        } else {
            for (LicenseAuthModuleGroupVo licenseAuthModuleGroupVo : licenseAuthModuleGroupVoList) {
                JSONObject moduleGroupJson = new JSONObject();
                moduleGroupJson.put("moduleGroup", licenseAuthModuleGroupVo.getName());
                moduleGroupJson.put("moduleGroupName", moduleGroupMap.get(licenseAuthModuleGroupVo.getName().toUpperCase(Locale.ROOT)));
                initAuth(licenseAuthModuleGroupVo.getAuthList(), moduleGroupJson, false);
                List<String> operationList = licenseAuthModuleGroupVo.getOperationTypeList();
                initOperation(operationList, moduleGroupJson, false);
                moduleGroupArray.add(moduleGroupJson);
            }
        }
        result.put("license", licenseMapper.getTenantLicenseByTenantUuid(tenant));
        TenantContext.get().setUseDefaultDatasource(false);
        return result;
    }

    /**
     * 初始化操作
     *
     * @param operationList   操作列表
     * @param moduleGroupJson 模块组
     */
    private void initOperation(List<String> operationList, JSONObject moduleGroupJson, boolean isAllModule) {
        Optional<String> allOperationOptional = operationList.stream().filter(o -> Objects.equals("ALL", o.toUpperCase(Locale.ROOT))).findFirst();
        JSONArray operationArray = new JSONArray();
        moduleGroupJson.put("operationList", operationArray);
        if (allOperationOptional.isPresent()) {
            JSONObject operationJson = new JSONObject();
            operationJson.put("name", "ALL");
            operationJson.put("displayName", "所有");
            if (isAllModule) {
                operationJson.put("desc", "拥有所有操作");
            } else {
                operationJson.put("desc", "拥有此模块所有操作");
            }
            operationArray.add(operationJson);
        } else {
            for (String operation : operationList) {
                JSONObject operationJson = new JSONObject();
                operationJson.put("name", operation);
                operationJson.put("displayName", OperationTypeEnum.getText(operation));
                operationArray.add(operationJson);
            }
        }
    }

    /**
     * 初始化操作
     *
     * @param authList        权限列表
     * @param moduleGroupJson 模块组
     */
    private void initAuth(List<String> authList, JSONObject moduleGroupJson, boolean isAllModule) {
        JSONArray authArray = new JSONArray();
        moduleGroupJson.put("authList", authArray);
        if (isAllModule || authList.stream().anyMatch(o -> Objects.equals("ALL", o.toUpperCase(Locale.ROOT)))) {
            JSONObject authJson = new JSONObject();
            authJson.put("name", "ALL");
            authJson.put("displayName", "所有");
            if (isAllModule) {
                authJson.put("desc", "拥有所有权限");
            } else {
                authJson.put("desc", "拥有此模块所有权限");
            }
            authArray.add(authJson);
        } else {
            for (String auth : authList) {
                JSONObject authJson = new JSONObject();
                AuthBase authBase = AuthFactory.getAuthInstance(auth.toUpperCase(Locale.ROOT));
                authJson.put("name", auth.toUpperCase(Locale.ROOT));
                if (authBase != null) {
                    authJson.put("displayName", authBase.getAuthDisplayName());
                    authJson.put("desc", authBase.getAuthIntroduction());
                }
                authArray.add(authJson);
            }
        }
    }
}

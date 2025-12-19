package neatlogic.module.tenant.api.license;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.ADMIN;
import neatlogic.framework.auth.label.LICENSE_MODIFY;
import neatlogic.framework.common.config.Config;
import neatlogic.framework.common.config.LocalConfig;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dto.license.LicenseModuleVo;
import neatlogic.framework.dto.license.LicenseVo;
import neatlogic.framework.exception.type.LicenseInvalidException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.LicenseUtil;
import neatlogic.module.tenant.service.ServerService;
import org.apache.commons.collections4.CollectionUtils;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AuthAction(action = ADMIN.class)
@AuthAction(action = LICENSE_MODIFY.class)
@OperationType(type = OperationTypeEnum.UPDATE)
public class UpdateLicenseExpireDateApi extends PrivateApiComponentBase {
    private static final Logger logger = LoggerFactory.getLogger(UpdateLicenseExpireDateApi.class);

    @Resource
    Config config;

    @Resource
    ServerService serverService;

    @Override
    public String getToken() {
        return "license/expiredate/update";
    }

    @Override
    public String getName() {
        return "nmtal.updatelicenseexpiredateapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }


    @Input({
            @Param(name = "isPassive", type = ApiParamType.INTEGER, desc = "是否被动清理，0：否，1：是，默认 0")
    })
    @Output({@Param(explode = LicenseVo.class)})
    @Description(desc = "nmtal.updatelicenseexpiredateapi.getname")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        int isPassive = jsonObj.getIntValue("isPassive");
        if (!Objects.equals(LocalConfig.getPropertiesFrom(), "Nacos")) {
            //重新获取本地配置
            config.reloadLocalConfig();
        }
        Map<String, Long> licenseModulePolicyMap = new HashMap<>();
        LicenseVo licenseVo = LicenseUtil.deLicense(Config.LICENSE(), Config.LICENSE_PK());
        if (licenseVo != null && licenseVo.getIsValid()) {
            List<LicenseModuleVo> licenseModulePolicyVos = licenseVo.getModulesPolicy();
            if (CollectionUtils.isNotEmpty(licenseModulePolicyVos)) {
                licenseModulePolicyMap = licenseModulePolicyVos.stream().collect(Collectors.toMap(LicenseModuleVo::getModule, LicenseModuleVo::getExpirationDate));
            }
        } else {
            throw new LicenseInvalidException();
        }
        //修改对应模块中的超时时间
        Reflections reflections = new Reflections("neatlogic");
        Set<Class<? extends BeanDefinitionRegistryPostProcessor>> authClass = reflections.getSubTypesOf(BeanDefinitionRegistryPostProcessor.class);
        for (Class<? extends BeanDefinitionRegistryPostProcessor> c : authClass) {
            if (!c.getSimpleName().endsWith("AuthBean")) {
                continue;
            }
            Field licenseField = c.getDeclaredField("licenseVo");
            licenseField.setAccessible(true);

            if (!Modifier.isStatic(licenseField.getModifiers())) {
                logger.error("licenseVo is not static");
                continue;
            }
            Object valueLicense = licenseField.get(null);
            if (valueLicense == null) {
                logger.error("licenseVo is null");
                continue;
            }
            if (!(valueLicense instanceof LicenseVo licenseV)) {
                logger.error("licenseVo is not instance of LicenseVo");
                continue;
            }
            licenseV.setExpirationDate(licenseVo.getExpirationDate());


            Field field = c.getDeclaredField("licenseModuleVo");
            field.setAccessible(true);
            if (!Modifier.isStatic(field.getModifiers())) {
                logger.error("licenseModuleVo is not static");
                continue;
            }
            Object value = field.get(null);
            if (value == null) {
                logger.error("licenseModuleVo is null");
                continue;
            }
            if (!(value instanceof LicenseModuleVo licenseModuleVo)) {
                logger.error("licenseModuleVo is not instance of LicenseModuleVo");
                continue;
            }
            licenseModuleVo.setExpirationDate(licenseModulePolicyMap.get(licenseModuleVo.getModule()));
        }
        JSONObject result = new JSONObject();
        if (isPassive == 0) {
            jsonObj.put("isPassive", 1);
            result.put("resultArray", serverService.postOtherServersApi(jsonObj, Config.SCHEDULE_SERVER_ID));
        }
        return result;
    }
}

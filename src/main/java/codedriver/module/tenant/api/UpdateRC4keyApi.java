/*
 * Copyright (c)  2022 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api;

import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.auth.label.AUTHORITY_MODIFY;
import codedriver.framework.autoexec.crossover.IAutoexecGlobalParamCrossoverMapper;
import codedriver.framework.autoexec.dao.mapper.AutoexecCombopMapper;
import codedriver.framework.autoexec.dao.mapper.AutoexecScriptMapper;
import codedriver.framework.autoexec.dto.combop.AutoexecCombopParamVo;
import codedriver.framework.autoexec.dto.global.param.AutoexecGlobalParamVo;
import codedriver.framework.autoexec.dto.script.AutoexecScriptVersionParamVo;
import codedriver.framework.cmdb.dao.mapper.resourcecenter.ResourceCenterMapper;
import codedriver.framework.cmdb.dto.resourcecenter.AccountVo;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.util.RC4Util;
import codedriver.framework.crossover.CrossoverServiceFactory;
import codedriver.framework.dao.mapper.DatasourceMapper;
import codedriver.framework.dao.mapper.MongoDbMapper;
import codedriver.framework.dto.DatasourceVo;
import codedriver.framework.dto.MongoDbVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.restful.dao.mapper.ApiMapper;
import codedriver.framework.restful.dto.ApiVo;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@AuthAction(action = AUTHORITY_MODIFY.class)
@OperationType(type = OperationTypeEnum.UPDATE)
public class UpdateRC4keyApi extends PrivateApiComponentBase {

    @Resource
    DatasourceMapper datasourceMapper;
    @Resource
    MongoDbMapper mongoDbMapper;
    @Resource
    ApiMapper apiMapper;
    @Resource
    ResourceCenterMapper resourceCenterMapper;
    @Resource
    AutoexecScriptMapper autoexecScriptMapper;
    @Resource
    AutoexecCombopMapper autoexecCombopMapper;

    @Override
    public String getToken() {
        return "rc4/key/update";
    }

    @Override
    public String getName() {
        return "更新RC4 key";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "oldPre", type = ApiParamType.STRING, isRequired = true, desc = "老的前缀"),
            @Param(name = "oldKey", type = ApiParamType.STRING, isRequired = true, desc = "老的key"),
    })
    @Description(desc = "更新RC4 key接口")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        String oldPre = paramObj.getString("oldPre");
        String oldKey = paramObj.getString("oldKey");
        TenantContext.get().setUseDefaultDatasource(true);
        //datasource
        List<DatasourceVo> datasourceVos = datasourceMapper.getAllTenantDatasource();
        for (DatasourceVo datasourceVo : datasourceVos) {
            String oldPassword = datasourceVo.getPasswordCipher();
            if (StringUtils.isNotBlank(oldPassword) && oldPassword.startsWith(oldPre)) {
                String newPassword = RC4Util.encrypt(RC4Util.decrypt(oldKey, oldPassword));
                datasourceMapper.updateDatasourcePasswordByTenantId(datasourceVo.getTenantId(), newPassword);
            }
        }
        //mongo
        List<MongoDbVo> mongoDbVos = mongoDbMapper.getAllTenantMongoDb();
        for (MongoDbVo mongodbVo : mongoDbVos) {
            String oldPassword = mongodbVo.getPasswordCipher();
            if (StringUtils.isNotBlank(oldPassword) && oldPassword.startsWith(oldPre)) {
                String newPassword = RC4Util.encrypt(RC4Util.decrypt(oldKey, oldPassword));
                mongoDbMapper.updateTenantMongoDbPasswordByTenantId(mongodbVo.getTenantId(), newPassword);
            }
        }
        TenantContext.get().setUseDefaultDatasource(false);
        //api
        List<ApiVo> apiVos = apiMapper.getAllApi();
        for (ApiVo apiVo : apiVos) {
            String oldPassword = apiVo.getPasswordCipher();
            if (StringUtils.isNotBlank(oldPassword) && oldPassword.startsWith(oldPre)) {
                String newPassword = RC4Util.encrypt(RC4Util.decrypt(oldKey, oldPassword));
                apiMapper.updatePasswordByToken(apiVo.getToken(), newPassword);
            }
        }
        //cmdb account
        List<AccountVo> accounts = resourceCenterMapper.getAllAccountList();
        for (AccountVo account : accounts) {
            String oldPassword = account.getPasswordCipher();
            if (StringUtils.isNotBlank(oldPassword) && oldPassword.startsWith(oldPre)) {
                String newPassword = RC4Util.encrypt(RC4Util.decrypt(oldKey, oldPassword));
                resourceCenterMapper.updateAccountPasswordById(account.getId(), newPassword);
            }
        }
        //autoexec global param
        IAutoexecGlobalParamCrossoverMapper autoexecGlobalParamCrossoverMapper = CrossoverServiceFactory.getApi(IAutoexecGlobalParamCrossoverMapper.class);
        List<AutoexecGlobalParamVo> autoexecGlobalParamVos = autoexecGlobalParamCrossoverMapper.getAllPasswordGlobalParam();
        for (AutoexecGlobalParamVo autoexecGlobalParamVo : autoexecGlobalParamVos) {
            String oldPassword = autoexecGlobalParamVo.getDefaultValueStr();
            if (StringUtils.isNotBlank(oldPassword) && oldPassword.startsWith(oldPre)) {
                String newPassword = RC4Util.encrypt(RC4Util.decrypt(oldKey, oldPassword));
                autoexecGlobalParamCrossoverMapper.updateGlobalParamPasswordById(autoexecGlobalParamVo.getId(), newPassword);
            }
        }
        //autoexec combop param
        List<AutoexecCombopParamVo> autoexecCombopParamVos = autoexecCombopMapper.getAllAutoexecCombopPasswordParamList();
        for (AutoexecCombopParamVo autoexecCombopParamVo : autoexecCombopParamVos) {
            String oldPassword = autoexecCombopParamVo.getDefaultValueStr();
            if (StringUtils.isNotBlank(oldPassword) && oldPassword.startsWith(oldPre)) {
                String newPassword = RC4Util.encrypt(RC4Util.decrypt(oldKey, oldPassword));
                autoexecCombopMapper.updateAutoexecCombopPasswordParam(autoexecCombopParamVo, newPassword);
            }
        }

        //autoexec script version param
        List<AutoexecScriptVersionParamVo> autoexecScriptVersionParamVos = autoexecScriptMapper.getAllPasswordScriptParam();
        for (AutoexecScriptVersionParamVo autoexecScriptVersionParamVo : autoexecScriptVersionParamVos) {
            String oldPassword = autoexecScriptVersionParamVo.getDefaultValueStr();
            if (StringUtils.isNotBlank(oldPassword) && oldPassword.startsWith(oldPre)) {
                String newPassword = RC4Util.encrypt(RC4Util.decrypt(oldKey, oldPassword));
                autoexecScriptMapper.updateScriptVersionParamPassword(autoexecScriptVersionParamVo, newPassword);
            }
        }
        return null;
    }
}

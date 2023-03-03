/*
Copyright(c) $today.year NeatLogic Co., Ltd. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package neatlogic.module.tenant.api;

import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.AUTHORITY_MODIFY;
import neatlogic.framework.autoexec.crossover.IAutoexecGlobalParamCrossoverMapper;
import neatlogic.framework.autoexec.crossover.IAutoexecProfileCrossoverMapper;
import neatlogic.framework.autoexec.dao.mapper.AutoexecCombopMapper;
import neatlogic.framework.autoexec.dao.mapper.AutoexecScriptMapper;
import neatlogic.framework.autoexec.dto.combop.AutoexecCombopParamVo;
import neatlogic.framework.autoexec.dto.global.param.AutoexecGlobalParamVo;
import neatlogic.framework.autoexec.dto.profile.AutoexecProfileParamVo;
import neatlogic.framework.autoexec.dto.script.AutoexecScriptVersionParamVo;
import neatlogic.framework.cmdb.crossover.IResourceAccountCrossoverMapper;
import neatlogic.framework.cmdb.dto.resourcecenter.AccountVo;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.util.RC4Util;
import neatlogic.framework.crossover.CrossoverServiceFactory;
import neatlogic.framework.dao.mapper.DatasourceMapper;
import neatlogic.framework.dao.mapper.MongoDbMapper;
import neatlogic.framework.dto.DatasourceVo;
import neatlogic.framework.dto.MongoDbVo;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.restful.dao.mapper.ApiMapper;
import neatlogic.framework.restful.dto.ApiVo;
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
        IResourceAccountCrossoverMapper resourceAccountCrossoverMapper = CrossoverServiceFactory.getApi(IResourceAccountCrossoverMapper.class);
        //cmdb account
        List<AccountVo> accounts = resourceAccountCrossoverMapper.getAllAccountList();
        for (AccountVo account : accounts) {
            String oldPassword = account.getPasswordCipher();
            if (StringUtils.isNotBlank(oldPassword) && oldPassword.startsWith(oldPre)) {
                String newPassword = RC4Util.encrypt(RC4Util.decrypt(oldKey, oldPassword));
                resourceAccountCrossoverMapper.updateAccountPasswordById(account.getId(), newPassword);
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
//        List<AutoexecCombopParamVo> autoexecCombopParamVos = autoexecCombopMapper.getAllAutoexecCombopPasswordParamList();
//        for (AutoexecCombopParamVo autoexecCombopParamVo : autoexecCombopParamVos) {
//            String oldPassword = autoexecCombopParamVo.getDefaultValueStr();
//            if (StringUtils.isNotBlank(oldPassword) && oldPassword.startsWith(oldPre)) {
//                String newPassword = RC4Util.encrypt(RC4Util.decrypt(oldKey, oldPassword));
//                autoexecCombopMapper.updateAutoexecCombopPasswordParam(autoexecCombopParamVo, newPassword);
//            }
//        }

        //autoexec script version param
        List<AutoexecScriptVersionParamVo> autoexecScriptVersionParamVos = autoexecScriptMapper.getAllPasswordScriptParam();
        for (AutoexecScriptVersionParamVo autoexecScriptVersionParamVo : autoexecScriptVersionParamVos) {
            String oldPassword = autoexecScriptVersionParamVo.getDefaultValueStr();
            if (StringUtils.isNotBlank(oldPassword) && oldPassword.startsWith(oldPre)) {
                String newPassword = RC4Util.encrypt(RC4Util.decrypt(oldKey, oldPassword));
                autoexecScriptMapper.updateScriptVersionParamPassword(autoexecScriptVersionParamVo, newPassword);
            }
        }
        //autoexec_profile_param
        IAutoexecProfileCrossoverMapper autoexecProfileCrossoverMapper = CrossoverServiceFactory.getApi(IAutoexecProfileCrossoverMapper.class);
        List<AutoexecProfileParamVo> autoexecProfileParamVos = autoexecProfileCrossoverMapper.getAllProfileParamList();
        for (AutoexecProfileParamVo autoexecProfileParamVo : autoexecProfileParamVos) {
            String oldPassword = autoexecProfileParamVo.getDefaultValueStr();
            if (StringUtils.isNotBlank(oldPassword) && oldPassword.startsWith(oldPre)) {
                String newPassword = RC4Util.encrypt(RC4Util.decrypt(oldKey, oldPassword));
                autoexecProfileCrossoverMapper.updateProfileParamPassword(autoexecProfileParamVo, newPassword);
            }
        }
        return null;
    }
}

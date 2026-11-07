/*
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package neatlogic.module.tenant.api;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.ADMIN;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.crypto.core.CryptoHandlerFactory;
import neatlogic.framework.exception.util.PasswordDecryptException;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * 管理员密文解密工具；沿用私有接口默认关闭审计的配置，使用时必须保持审计关闭。
 */
@Service
@AuthAction(action = ADMIN.class)
@OperationType(type = OperationTypeEnum.OPERATE)
public class DecryptCipherApi extends PrivateApiComponentBase {

    @Override
    public String getName() {
        return "nmta.decryptcipherapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Override
    public String getToken() {
        return "/cipher/decrypt";
    }

    @Description(desc = "nmta.decryptcipherapi.description.desc")
    @Input({
            @Param(name = "text", type = ApiParamType.STRING, isRequired = true, desc = "nmta.decryptcipherapi.input.param.desc")
    })
    @Output({
            @Param(name = "Return", type = ApiParamType.STRING, desc = "nmta.decryptcipherapi.output.param.desc")
    })
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        String passwordCipher = paramObj.getString("text");
        if (StringUtils.isNotBlank(passwordCipher)) {
            try {
                // 仅在内存中解密并返回结果，禁止输出密码明文或密文日志。
                return CryptoHandlerFactory.getCryptoHandlerByCiphertext(passwordCipher).decrypt(passwordCipher);
            } catch (Exception e) {
                // 不传递原始异常及其消息，避免框架日志记录底层异常中的敏感内容。
                throw new PasswordDecryptException();
            }
        }
        // HTTP 请求由框架校验必填参数，此处兼容直接调用时的空值。
        return null;
    }
}

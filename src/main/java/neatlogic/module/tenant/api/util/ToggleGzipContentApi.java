/*
 *
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 */

package neatlogic.module.tenant.api.util;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.NoAuth;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.GzipUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
@AuthAction(action = NoAuth.class)
@OperationType(type = OperationTypeEnum.OPERATE)
public class ToggleGzipContentApi extends PrivateApiComponentBase {

    @Override
    public String getToken() {
        return "/util/togglegzipcontent";
    }

    @Override
    public String getName() {
        return "转换压缩内容";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "content", type = ApiParamType.STRING, isRequired = true, desc = "如果是压缩内容，需要用GIZ:开头")})
    @Description(desc = "转换压缩内容接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String content = jsonObj.getString("content");
        if (StringUtils.isNotBlank(content)) {
            if (content.startsWith("GZIP:")) {
                content = GzipUtil.uncompress(content.substring(5));
            } else {
                content = "GZIP:" + GzipUtil.compress(content);
            }
        }
        return content;
    }
}

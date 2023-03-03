/*
 * Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package neatlogic.module.tenant.api.util;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.GzipUtil;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service

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

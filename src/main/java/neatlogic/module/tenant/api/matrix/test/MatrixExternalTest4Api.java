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

package neatlogic.module.tenant.api.matrix.test;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 用于矩阵外部数据源测试
 */
@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class MatrixExternalTest4Api extends PrivateApiComponentBase {

    private final static Logger logger = LoggerFactory.getLogger(MatrixExternalTest4Api.class);

    @Override
    public String getToken() {
        return "matrix/external/test4";
    }

    @Override
    public String getName() {
        return "返回结果为空串";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Description(desc = "返回结果为空串")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        return "";
    }

}

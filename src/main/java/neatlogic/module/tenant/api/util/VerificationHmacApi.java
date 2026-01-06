/*
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package neatlogic.module.tenant.api.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.RequestContext;
import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.ADMIN;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.SHA256Util;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@AuthAction(action = ADMIN.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class VerificationHmacApi extends PrivateApiComponentBase {

    @Override
    public String getName() {
        return "核对Hmac";
    }

    @Input({
            @Param(name = "requestURI", type = ApiParamType.STRING, isRequired = true, desc = "请求URI", help = "以/neatlogic为前缀，例如/neatlogic/api/rest/xxx, /neatlogic/api/binary/xxx"),
            @Param(name = "requestParam", type = ApiParamType.JSONOBJECT, isRequired = true, desc = "请求参数"),
            @Param(name = "userId", type = ApiParamType.STRING, desc = "用户ID", isRequired = true, help = "在用户基本信息页面"),
            @Param(name = "userToken", type = ApiParamType.STRING, desc = "用户令牌", isRequired = true, help = "在用户基本信息页面")
    })
    @Output({})
    @Description(desc = "核对Hmac")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        JSONObject resultObj = new JSONObject(true);
        resultObj.put("inputParam", paramObj);
        String requestURI = paramObj.getString("requestURI");
        JSONObject requestParam = paramObj.getJSONObject("requestParam");
        String userId = paramObj.getString("userId");
        String userToken = paramObj.getString("userToken");

        JSONObject hmacObj = new JSONObject(true);
        String requestParamToJSONString = JSON.toJSONString(requestParam,false);
        hmacObj.put("requestParamToJSONString", requestParamToJSONString);
        String base64Encode = Base64Utils.encodeToString(requestParamToJSONString.getBytes(StandardCharsets.UTF_8));
        hmacObj.put("base64Encode", base64Encode);
        String sign = userId + "#" + requestURI + "#" + base64Encode;
        hmacObj.put("sign", sign);
        String authorization = SHA256Util.encrypt(userToken, sign);
        hmacObj.put("authorization", authorization);
        resultObj.put("hmac", hmacObj);

        HttpServletRequest request = RequestContext.get().getRequest();
        String url = request.getRequestURL().toString().replace(request.getRequestURI(), "") + requestURI;
        JSONObject requestObj = new JSONObject(true);
        requestObj.put("url", url);

        HttpHeaders requestHeaders = new HttpHeaders();
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Tenant", TenantContext.get().getTenantUuid());
        headers.put("x-access-key",userId);
        headers.put("AuthType","hmac");
        headers.put("Authorization","Hmac " + authorization);
        requestObj.put("headers", headers);
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            requestHeaders.add(entry.getKey(), entry.getValue());
        }
        HttpEntity<JSONObject> requestEntity = new HttpEntity<>(requestParam, requestHeaders);
        RestTemplate restTemplate = new RestTemplate();

        try {
            JSONObject result = restTemplate.postForObject(url, requestEntity, JSONObject.class);
            requestObj.put("result", result);
        } catch (Exception e) {
            requestObj.put("error", ExceptionUtils.getStackFrames(e));
        }
        resultObj.put("request", requestObj);
        return resultObj;
    }

    @Override
    public String getToken() {
        return "util/verification/hmac";
    }
}

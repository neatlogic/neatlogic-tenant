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

package neatlogic.module.tenant.api.apimanage;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.core.AuthBase;
import neatlogic.framework.auth.core.AuthFactory;
import neatlogic.framework.auth.label.INTERFACE_MODIFY;
import neatlogic.framework.auth.label.NoAuth;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dto.AuthVo;
import neatlogic.framework.exception.type.ApiNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentFactory;
import neatlogic.framework.restful.dao.mapper.ApiMapper;
import neatlogic.framework.restful.dto.ApiHandlerVo;
import neatlogic.framework.restful.dto.ApiVo;
import neatlogic.framework.restful.enums.ApiType;
import org.springframework.aop.support.AopUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** 获取接口配置，并补充最终处理器声明的接口级执行权限，不校验当前用户权限。 */
@Service
@AuthAction(action = INTERFACE_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ApiManageGetApi extends PrivateApiComponentBase {

	@Resource
	private ApiMapper ApiMapper;
	
	@Override
	public String getToken() {
		return "apimanage/get";
	}

	@Override
	public String getName() {
		return "nmta.apimanagegetapi.getname";
	}

	@Override
	public String getConfig() {
		return null;
	}

	/** 合并数据库配置与代码定义，再读取实际生效处理器的权限。 */
	@Input({
		@Param(name = "token", type = ApiParamType.STRING, isRequired = true, desc = "nmta.apimanagegetapi.token")
	})
	@Output({
		@Param(name = "Return", explode = ApiVo.class, isRequired = true, desc = "nmta.apimanagegetapi.config")
	})
	@Description(desc = "nmta.apimanagegetapi.getname")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String token = jsonObj.getString("token");
		String normalizedToken = token;
		if (normalizedToken != null && normalizedToken.startsWith("/")) {
			normalizedToken = normalizedToken.substring(1);
		}
		if (normalizedToken != null && normalizedToken.endsWith("/")) {
			normalizedToken = normalizedToken.substring(0, normalizedToken.length() - 1);
		}
		ApiVo api = PrivateApiComponentFactory.getApiByToken(normalizedToken);
		ApiVo apiVo = ApiMapper.getApiByTokenWithoutPsw(token);
		if(apiVo != null) {
            if(api != null){
                apiVo.setAuthTypeList(api.getAuthTypeList());
                apiVo.setHandler(api.getHandler());
                apiVo.setModuleId(api.getModuleId());
                apiVo.setType(api.getType());
            } else if (apiVo.getHandler() != null) {
                ApiHandlerVo apiHandlerVo = PrivateApiComponentFactory.getApiHandlerByHandler(apiVo.getHandler());
                if (apiHandlerVo != null) {
                    apiVo.setType(apiHandlerVo.getType());
                    apiVo.setModuleId(apiHandlerVo.getModuleId());
                }
            }
			apiVo.setRequiredAuthList(getRequiredAuthList(apiVo));
			return apiVo;

		}
		if(api != null) {
			api.setRequiredAuthList(getRequiredAuthList(api));
			return api;
		}
		throw new ApiNotFoundException(token);
	}

    /**
     * 复用组件及权限注册表提取声明权限；空列表表示无需接口级功能权限，null 表示处理器不可解析。
     * 仅描述声明，不展开包含关系，也不调用会按当前用户短路的权限校验。
     */
    private List<AuthVo> getRequiredAuthList(ApiVo apiVo) {
        Object component = null;
        for (ApiType apiType : ApiType.values()) {
            if (Objects.equals(apiType.getValue(), apiVo.getType())) {
                component = PrivateApiComponentFactory.getComponent(apiVo.getHandler(), apiType, Object.class);
                break;
            }
        }
        if (component == null) {
            return null;
        }
        AuthAction[] actions = AopUtils.getTargetClass(component).getAnnotationsByType(AuthAction.class);
        Map<String, AuthVo> authMap = new LinkedHashMap<>();
        for (AuthAction action : actions) {
            // 任一 NoAuth 声明会使接口级功能权限直接通过，不能把其他声明显示为必需权限。
            if (action.action() == NoAuth.class) {
                return Collections.emptyList();
            }
            String name = action.action().getSimpleName();
            if (!authMap.containsKey(name)) {
                AuthBase auth = AuthFactory.getAuthInstance(name);
                AuthVo authVo = auth != null ? new AuthVo(auth) : new AuthVo();
                authVo.setName(name);
                authMap.put(name, authVo);
            }
        }
        return new ArrayList<>(authMap.values());
    }

}

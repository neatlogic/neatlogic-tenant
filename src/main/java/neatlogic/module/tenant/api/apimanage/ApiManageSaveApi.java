/*Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

package neatlogic.module.tenant.api.apimanage;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.INTERFACE_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dto.FieldValidResultVo;
import neatlogic.framework.exception.type.ApiAuthTypeNotFoundException;
import neatlogic.framework.exception.type.ApiNotFoundException;
import neatlogic.framework.exception.type.ApiRepeatException;
import neatlogic.framework.exception.type.ComponentNotFoundException;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.IValid;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentFactory;
import neatlogic.framework.restful.core.publicapi.PublicApiComponentFactory;
import neatlogic.framework.restful.dao.mapper.ApiMapper;
import neatlogic.framework.restful.dto.ApiHandlerVo;
import neatlogic.framework.restful.dto.ApiVo;
import neatlogic.framework.restful.enums.ApiKind;
import neatlogic.framework.restful.enums.PublicApiAuthType;
import neatlogic.framework.util.RegexUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@Transactional
@AuthAction(action = INTERFACE_MODIFY.class)
@OperationType(type = OperationTypeEnum.UPDATE)
public class ApiManageSaveApi extends PrivateApiComponentBase {

    @Autowired
    private ApiMapper ApiMapper;

    @Override
    public String getToken() {
        return "apimanage/save";
    }

    @Override
    public String getName() {
        return "接口配置信息保存接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "token", type = ApiParamType.REGEX, rule = RegexUtils.API_TOKEN, isRequired = true, desc = "token"),
            @Param(name = "name", type = ApiParamType.STRING, maxLength = 50, isRequired = true, desc = "名称"),
            @Param(name = "handler", type = ApiParamType.STRING, isRequired = true, desc = "处理器"),
            @Param(name = "needAudit", type = ApiParamType.ENUM, rule = "0,1", isRequired = true, desc = "是否保存调用记录"),
            @Param(name = "authtype", type = ApiParamType.ENUM, rule = "basic,token", isRequired = true, desc = "认证方式"),
            @Param(name = "isActive", type = ApiParamType.ENUM, rule = "0,1", isRequired = true, desc = "是否激活"),
            @Param(name = "timeout", type = ApiParamType.INTEGER, desc = "请求时效"),
            @Param(name = "qps", type = ApiParamType.INTEGER, desc = "每秒访问几次，大于0生效"),
            @Param(name = "expire", type = ApiParamType.LONG, desc = "使用期限"),
            @Param(name = "config", type = ApiParamType.JSONOBJECT, desc = "json格式,配置信息"),
            @Param(name = "username", type = ApiParamType.STRING, desc = "用户名称"),
            @Param(name = "password", type = ApiParamType.REGEX, rule = RegexUtils.PASSWORD, desc = "密码"),
            @Param(name = "description", type = ApiParamType.STRING, desc = "描述"),
            @Param(name = "apiType", type = ApiParamType.STRING, desc = "API类型", isRequired = true),
            @Param(name = "operationType", type = ApiParamType.STRING, desc = "操作类型(create|update)", isRequired = true)
    })
    @Description(desc = "接口配置信息保存接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        ApiVo apiVo = JSON.toJavaObject(jsonObj,ApiVo.class);
        String operationType = jsonObj.getString("operationType");

        ApiHandlerVo apiHandlerVo = PrivateApiComponentFactory.getApiHandlerByHandler(apiVo.getHandler());
        ApiVo ramApiVo = PrivateApiComponentFactory.getApiByToken(apiVo.getToken());
        if (apiHandlerVo == null) {
            apiHandlerVo = PublicApiComponentFactory.getApiHandlerByHandler(apiVo.getHandler());
            if (apiHandlerVo == null) {
                throw new ComponentNotFoundException("接口组件:" + apiVo.getHandler() + "不存在");
            }
        }

        apiVo.setType(apiHandlerVo.getType());
        apiVo.setModuleId(apiHandlerVo.getModuleId());

        if (ApiKind.CUSTOM.getValue().equals(apiVo.getApiType())) {
            if (PublicApiAuthType.getAuthenticateType(apiVo.getAuthtype()) == null) {
                throw new ApiAuthTypeNotFoundException(apiVo.getAuthtype());
            }
            if (ramApiVo != null) {
                throw new ApiRepeatException("不可与系统接口使用同一个token");
            }
            List<ApiVo> dbApiList = ApiMapper.getAllApi();
            //校验token是否与自定义接口重复
            boolean isTokenRepeat = false;
            //如果添加自定义接口，就要校验当前token是否与其余自定义接口重复
            if (OperationTypeEnum.CREATE.getValue().equals(operationType)) {
                if (CollectionUtils.isNotEmpty(dbApiList) && !isTokenRepeat) {
                    for (ApiVo api : dbApiList) {
                        if (api.getToken().equals(apiVo.getToken())) {
                            isTokenRepeat = true;
                            break;
                        }
                    }
                }
            }
            if (isTokenRepeat) {
                throw new ApiRepeatException("接口地址：" + apiVo.getToken() + "已存在");
            }

            boolean isNameRepeat = false;
            //校验接口名称是否与系统接口重复
            for (ApiVo api : PrivateApiComponentFactory.getApiList()) {
                if (Objects.equals(api.getName(), apiVo.getName())) {
                    isNameRepeat = true;
                    break;
                }
            }
            //校验接口名称是否与其他自定义接口重复
//			List<ApiVo> dbApiList = ApiMapper.getAllApi();
            if (CollectionUtils.isNotEmpty(dbApiList) && !isNameRepeat) {
                for (ApiVo api : dbApiList) {
                    if (Objects.equals(api.getName(), apiVo.getName()) && !Objects.equals(api.getToken(), apiVo.getToken())) {
                        isNameRepeat = true;
                        break;
                    }
                }
            }

            if (isNameRepeat) {
                throw new ApiRepeatException("接口名称：" + apiVo.getName() + "已被占用");
            }
            ApiMapper.replaceApi(apiVo);
            return null;
        } else if (ApiKind.SYSTEM.getValue().equals(apiVo.getApiType())) {
            if (ramApiVo == null) {
                throw new ApiNotFoundException(apiVo.getToken());
            }
            if (ramApiVo.equals(apiVo)) {
                //如果接口配置信息与内存中接口配置信息一致，删除数据库中该接口的配置数据
                ApiMapper.deleteApiByToken(apiVo.getToken());
            } else {
                ApiMapper.replaceApi(apiVo);
            }
        }

//		ApiVo ramApiVo = ApiComponentFactory.getApiByToken(apiVo.getToken());
//		if(ramApiVo == null) {
//			ApiMapper.replaceApi(apiVo);
//			return null;
//		}
//		if(ramApiVo.equals(apiVo)) {
//			//如果接口配置信息与内存中接口配置信息一致，删除数据库中该接口的配置数据
//			ApiMapper.deleteApiByToken(apiVo.getToken());
//		}else {
//			ApiMapper.replaceApi(apiVo);
//		}
        return null;
    }

    public IValid token() {
        return value -> {
            String token = value.getString("token");
            ApiVo ramApiVo = PrivateApiComponentFactory.getApiByToken(token);
            if (ramApiVo != null) {
                return new FieldValidResultVo(new ApiRepeatException(token + "已存在"));
            }
            List<ApiVo> dbApiList = ApiMapper.getAllApi();
            //校验token是否与自定义接口重复
            boolean isTokenRepeat = false;
            if (CollectionUtils.isNotEmpty(dbApiList) && !isTokenRepeat) {
                for (ApiVo api : dbApiList) {
                    if (api.getToken().equals(token)) {
                        isTokenRepeat = true;
                        break;
                    }
                }
            }
            if (isTokenRepeat) {
                return new FieldValidResultVo(new ApiRepeatException(token + "已存在"));
            }
            return new FieldValidResultVo();
        };
    }

    public IValid name() {
        return value -> {
            String token = value.getString("token");
            String name = value.getString("name");
            boolean isNameRepeat = false;
            //校验接口名称是否与系统接口重复
            for (ApiVo api : PrivateApiComponentFactory.getApiList()) {
                if (Objects.equals(api.getName(), name)) {
                    isNameRepeat = true;
                    break;
                }
            }
            //校验接口名称是否与其他自定义接口重复
            List<ApiVo> dbApiList = ApiMapper.getAllApi();
            if (CollectionUtils.isNotEmpty(dbApiList) && !isNameRepeat) {
                for (ApiVo api : dbApiList) {
                    if (api.getName().equals(name) && !api.getToken().equals(token)) {
                        isNameRepeat = true;
                        break;
                    }
                }
            }

            if (isNameRepeat) {
                return new FieldValidResultVo(new ApiRepeatException(name + "已存在"));
            }
            return new FieldValidResultVo();
        };
    }

}

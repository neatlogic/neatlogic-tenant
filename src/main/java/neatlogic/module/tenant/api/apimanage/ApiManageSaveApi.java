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
        return "??????????????????????????????";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "token", type = ApiParamType.REGEX, rule = RegexUtils.API_TOKEN, isRequired = true, desc = "token"),
            @Param(name = "name", type = ApiParamType.STRING, maxLength = 50, isRequired = true, desc = "??????"),
            @Param(name = "handler", type = ApiParamType.STRING, isRequired = true, desc = "?????????"),
            @Param(name = "needAudit", type = ApiParamType.ENUM, rule = "0,1", isRequired = true, desc = "????????????????????????"),
            @Param(name = "authtype", type = ApiParamType.ENUM, rule = "basic,token", isRequired = true, desc = "????????????"),
            @Param(name = "isActive", type = ApiParamType.ENUM, rule = "0,1", isRequired = true, desc = "????????????"),
            @Param(name = "timeout", type = ApiParamType.INTEGER, desc = "????????????"),
            @Param(name = "qps", type = ApiParamType.INTEGER, desc = "???????????????????????????0??????"),
            @Param(name = "expire", type = ApiParamType.LONG, desc = "????????????"),
            @Param(name = "config", type = ApiParamType.JSONOBJECT, desc = "json??????,????????????"),
            @Param(name = "username", type = ApiParamType.STRING, desc = "????????????"),
            @Param(name = "password", type = ApiParamType.REGEX, rule = RegexUtils.PASSWORD, desc = "??????"),
            @Param(name = "description", type = ApiParamType.STRING, desc = "??????"),
            @Param(name = "apiType", type = ApiParamType.STRING, desc = "API??????", isRequired = true),
            @Param(name = "operationType", type = ApiParamType.STRING, desc = "????????????(create|update)", isRequired = true)
    })
    @Description(desc = "??????????????????????????????")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        ApiVo apiVo = JSON.toJavaObject(jsonObj,ApiVo.class);
        String operationType = jsonObj.getString("operationType");

        ApiHandlerVo apiHandlerVo = PrivateApiComponentFactory.getApiHandlerByHandler(apiVo.getHandler());
        ApiVo ramApiVo = PrivateApiComponentFactory.getApiByToken(apiVo.getToken());
        if (apiHandlerVo == null) {
            apiHandlerVo = PublicApiComponentFactory.getApiHandlerByHandler(apiVo.getHandler());
            if (apiHandlerVo == null) {
                throw new ComponentNotFoundException("????????????:" + apiVo.getHandler() + "?????????");
            }
        }

        apiVo.setType(apiHandlerVo.getType());
        apiVo.setModuleId(apiHandlerVo.getModuleId());

        if (ApiKind.CUSTOM.getValue().equals(apiVo.getApiType())) {
            if (PublicApiAuthType.getAuthenticateType(apiVo.getAuthtype()) == null) {
                throw new ApiAuthTypeNotFoundException(apiVo.getAuthtype());
            }
            if (ramApiVo != null) {
                throw new ApiRepeatException("????????????????????????????????????token");
            }
            List<ApiVo> dbApiList = ApiMapper.getAllApi();
            //??????token??????????????????????????????
            boolean isTokenRepeat = false;
            //????????????????????????????????????????????????token????????????????????????????????????
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
                throw new ApiRepeatException("???????????????" + apiVo.getToken() + "?????????");
            }

            boolean isNameRepeat = false;
            //?????????????????????????????????????????????
            for (ApiVo api : PrivateApiComponentFactory.getApiList()) {
                if (Objects.equals(api.getName(), apiVo.getName())) {
                    isNameRepeat = true;
                    break;
                }
            }
            //??????????????????????????????????????????????????????
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
                throw new ApiRepeatException("???????????????" + apiVo.getName() + "????????????");
            }
            ApiMapper.replaceApi(apiVo);
            return null;
        } else if (ApiKind.SYSTEM.getValue().equals(apiVo.getApiType())) {
            if (ramApiVo == null) {
                throw new ApiNotFoundException(apiVo.getToken());
            }
            if (ramApiVo.equals(apiVo)) {
                //?????????????????????????????????????????????????????????????????????????????????????????????????????????
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
//			//?????????????????????????????????????????????????????????????????????????????????????????????????????????
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
                return new FieldValidResultVo(new ApiRepeatException(token + "?????????"));
            }
            List<ApiVo> dbApiList = ApiMapper.getAllApi();
            //??????token??????????????????????????????
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
                return new FieldValidResultVo(new ApiRepeatException(token + "?????????"));
            }
            return new FieldValidResultVo();
        };
    }

    public IValid name() {
        return value -> {
            String token = value.getString("token");
            String name = value.getString("name");
            boolean isNameRepeat = false;
            //?????????????????????????????????????????????
            for (ApiVo api : PrivateApiComponentFactory.getApiList()) {
                if (Objects.equals(api.getName(), name)) {
                    isNameRepeat = true;
                    break;
                }
            }
            //??????????????????????????????????????????????????????
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
                return new FieldValidResultVo(new ApiRepeatException(name + "?????????"));
            }
            return new FieldValidResultVo();
        };
    }

}

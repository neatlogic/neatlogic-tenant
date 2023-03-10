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

import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.INTERFACE_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.util.PageUtil;
import neatlogic.framework.dto.module.ModuleVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentFactory;
import neatlogic.framework.restful.core.publicapi.PublicApiComponentFactory;
import neatlogic.framework.restful.dao.mapper.ApiAuditMapper;
import neatlogic.framework.restful.dao.mapper.ApiMapper;
import neatlogic.framework.restful.dto.ApiHandlerVo;
import neatlogic.framework.restful.dto.ApiVo;
import neatlogic.framework.restful.enums.ApiKind;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@AuthAction(action = INTERFACE_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ApiManageSearchApi extends PrivateApiComponentBase {

    @Autowired
    private ApiMapper ApiMapper;


    @Autowired
    private ApiAuditMapper apiAuditMapper;

    @Override
    public String getToken() {
        return "apimanage/search";
    }

    @Override
    public String getName() {
        return "??????????????????????????????";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "needAudit", type = ApiParamType.ENUM, rule = "0,1", desc = "??????????????????"),
            @Param(name = "keyword", type = ApiParamType.STRING, xss = true, desc = "?????????????????????????????????"),
            @Param(name = "moduleGroup", type = ApiParamType.STRING, desc = "?????????????????????"),
            @Param(name = "funcId", type = ApiParamType.STRING, desc = "??????????????????"),
            @Param(name = "apiType", type = ApiParamType.STRING, desc = "????????????(system|custom)"),
            @Param(name = "handler", type = ApiParamType.STRING, desc = "?????????"),
            @Param(name = "isActive", type = ApiParamType.ENUM, rule = "0,1", desc = "????????????"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "????????????????????????1"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "?????????????????????10"),
            @Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "????????????????????????true")})
    @Output({@Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "????????????"), @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "?????????"), @Param(name = "pageCount", type = ApiParamType.INTEGER, desc = "?????????"), @Param(name = "rowNum", type = ApiParamType.INTEGER, desc = "?????????"), @Param(name = "tbodyList", explode = ApiVo[].class, isRequired = true, desc = "????????????????????????")})
    @Description(desc = "??????????????????????????????")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        ApiVo apiVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<ApiVo>() {
        });
        Integer needAudit = jsonObj.getInteger("needAudit");
        if (needAudit == null) {
            apiVo.setNeedAudit(null);
        }
        String keyword = jsonObj.getString("keyword");
        if (StringUtils.isNotBlank(keyword)) {
            if (keyword.startsWith("/")) {
                keyword = keyword.substring(1);
            }
        }
//		String handler = apiVo.getHandler();
//		
//		if(handler != null) {
//			ApiHandlerVo apiHandlerVo = PrivateApiComponentFactory.getApiHandlerByHandler(handler);
//			if(apiHandlerVo == null) {
//				throw new ComponentNotFoundException("????????????:" + handler + "?????????");
//			}
//		}
        List<ApiVo> dbAllApiList = ApiMapper.getAllApiByModuleId(TenantContext.get().getActiveModuleList().stream().map(ModuleVo::getId).collect(Collectors.toList()));
        List<ApiVo> ramApiList = new ArrayList<>();
        List<String> tokenList = new ArrayList<>();
        List<String> ramTokenList = new ArrayList<>();
        //???????????????????????????????????????api???token??????
        for (ApiVo api : PrivateApiComponentFactory.getTenantActiveApiList()) {
            if (apiVo.getIsActive() != null && !apiVo.getIsActive().equals(api.getIsActive())) {
                continue;
            }
//			if(StringUtils.isNotBlank(apiVo.getModuleId()) && !apiVo.getModuleId().equals(api.getHandler())) {
//				continue;
//			}
//			if(StringUtils.isNotBlank(handler) && !handler.equals(api.getHandler())) {
//				continue;
//			}
            //?????????????????????????????????????????????????????????????????????/?????????????????????????????????
            if (StringUtils.isNotBlank(apiVo.getApiType()) && !apiVo.getApiType().equals(api.getApiType())) {
                continue;
            }
            //?????????????????????????????????????????????????????????????????????
            if (StringUtils.isNotBlank(apiVo.getModuleGroup()) && !apiVo.getModuleGroup().equals(api.getModuleGroup())) {
                continue;
            }
            //?????????????????????????????????????????????????????????????????????
            if (StringUtils.isNotBlank(apiVo.getFuncId())) {
                if (api.getToken().contains("/")) {
                    if (!api.getToken().startsWith(apiVo.getFuncId() + "/")) {
                        continue;
                    }
                } else {
                    if (!api.getToken().equals(apiVo.getFuncId())) {
                        continue;
                    }
                }

            }
            if (StringUtils.isNotBlank(keyword)) {
                if (!(StringUtils.isNotBlank(api.getName()) && api.getName().contains(keyword)) && !(StringUtils.isNotBlank(api.getToken()) && api.getToken().contains(keyword))) {
                    continue;
                }
            }
            // ????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
            if (apiVo.getNeedAudit() != null) {
                Optional<ApiVo> first = dbAllApiList.stream().filter(o -> Objects.equals(o.getToken(), api.getToken())).findFirst();
                if (first.isPresent()) {
                    if (!Objects.equals(apiVo.getNeedAudit(), first.get().getNeedAudit())) {
                        continue;
                    }
                } else if (!Objects.equals(apiVo.getNeedAudit(), api.getNeedAudit())) {
                    continue;
                }
            }
            ramApiList.add(api);
            tokenList.add(api.getToken());
            ramTokenList.add(api.getToken());
        }

        List<ApiVo> dbApiList = new ArrayList<>();
        List<String> dbTokenList = new ArrayList<>();
        Map<String, ApiVo> ramApiMap = PrivateApiComponentFactory.getApiMap();
        for (ApiVo api : dbAllApiList) {
            if (ramApiMap.get(api.getToken()) != null) {
                api.setIsPrivate(true);
                api.setApiType(ApiKind.SYSTEM.getValue());
            } else {
                ApiHandlerVo publicApiHandler = PublicApiComponentFactory.getApiHandlerByHandler(api.getHandler());
                if (publicApiHandler == null) {
                    api.setHandlerName("????????????:" + api.getHandler() + "?????????");
                } else {
                    api.setHandlerName(publicApiHandler.getName());
                }
                api.setIsPrivate(false);
                api.setApiType(ApiKind.CUSTOM.getValue());
            }

            //?????????????????????????????????????????????????????????????????????/?????????????????????????????????
            if (StringUtils.isNotBlank(apiVo.getApiType()) && !apiVo.getApiType().equals(api.getApiType())) {
                continue;
            }
            //?????????????????????????????????????????????????????????????????????
            if (StringUtils.isNotBlank(apiVo.getModuleGroup()) && !apiVo.getModuleGroup().equals(api.getModuleGroup())) {
                continue;
            }
            //?????????????????????????????????????????????????????????????????????
            if (StringUtils.isNotBlank(apiVo.getFuncId())) {
                if (api.getToken().contains("/")) {
                    if (!api.getToken().startsWith(apiVo.getFuncId() + "/")) {
                        continue;
                    }
                } else {
                    if (!api.getToken().equals(apiVo.getFuncId())) {
                        continue;
                    }
                }
            }
            if (StringUtils.isNotBlank(keyword)) {
                if (!(StringUtils.isNotBlank(api.getName()) && api.getName().contains(keyword)) && !(StringUtils.isNotBlank(api.getToken()) && api.getToken().contains(keyword))) {
                    continue;
                }
            }
            if (apiVo.getNeedAudit() != null && !Objects.equals(apiVo.getNeedAudit(), api.getNeedAudit())) {
                continue;
            }
            dbTokenList.add(api.getToken());
            dbApiList.add(api);
        }
        //??????????????????????????????????????????token??????
//		List<String> dbTokenList = ApiMapper.getApiTokenList(apiVo);
        //??????????????????????????????token????????????
        for (String token : dbTokenList) {
            if (tokenList.contains(token)) {
                continue;
            }
            tokenList.add(token);
        }
        //token??????(??????????????????????????????????????????token??????)
        if (StringUtils.isBlank(apiVo.getApiType()) || (StringUtils.isNotBlank(apiVo.getApiType()) && !apiVo.getApiType().equals(ApiKind.CUSTOM.getValue()))) {
            tokenList.sort(String::compareTo);
        }

        JSONObject resultObj = new JSONObject();
        if (apiVo.getNeedPage()) {
            int rowNum = tokenList.size();
            int pageCount = PageUtil.getPageCount(rowNum, apiVo.getPageSize());
            resultObj.put("currentPage", apiVo.getCurrentPage());
            resultObj.put("pageSize", apiVo.getPageSize());
            resultObj.put("pageCount", pageCount);
            resultObj.put("rowNum", rowNum);
            //???????????????token
            int fromIndex = apiVo.getStartNum();
            if (fromIndex < rowNum) {
                int toIndex = fromIndex + apiVo.getPageSize();
                toIndex = Math.min(toIndex, rowNum);
                tokenList = tokenList.subList(fromIndex, toIndex);
            } else {
                tokenList = new ArrayList<>();
            }
            ramTokenList.retainAll(tokenList);
            dbTokenList.retainAll(tokenList);
        }

        Map<String, ApiVo> apiMap = new HashMap<>();
        if (!ramTokenList.isEmpty()) {
            //???????????????????????????api??????????????????map???
            for (ApiVo api : ramApiList) {
                if (apiVo.getNeedPage() && !ramTokenList.contains(api.getToken())) {
                    continue;
                }
                apiMap.put(api.getToken(), api);
            }
        }
        for (ApiVo api : dbApiList) {
            if (apiMap.containsKey(api.getToken())) {
                api.setIsDeletable(0);
            }
            apiMap.put(api.getToken(), api);
        }
//		Map<String, Integer> visitTimesMap = new HashMap<>();
//		if(!tokenList.isEmpty()) {
//			List<ApiVo> visitTimesList = ApiMapper.getApiVisitTimesListByTokenList(tokenList);
//			for(ApiVo api : visitTimesList) {
//				visitTimesMap.put(api.getToken(), api.getVisitTimes());
//			}
//		}

        //???map??????????????????api??????
        List<ApiVo> apiList = new ArrayList<>();
        for (String token : tokenList) {
            ApiVo api = apiMap.get(token);
//			Integer visitTimes = visitTimesMap.get(token);
//			if(visitTimes != null) {
//				api.setVisitTimes(visitTimes);
//			}
            apiList.add(api);
        }

        /**
         * ??????token????????????API??????????????????????????????ApiVo???visitTimes?????????
         */
        List<String> apiTokenList = new ArrayList<>();
        apiList.forEach(vo -> apiTokenList.add(vo.getToken()));
        if (!apiTokenList.isEmpty()) {
            List<ApiVo> apiVisitTimesList = apiAuditMapper.getApiAccessCountByTokenList(apiTokenList);
            if (!apiVisitTimesList.isEmpty()) {
                apiList.forEach(api -> {
                    for (ApiVo vo : apiVisitTimesList) {
                        if (api.getToken().equals(vo.getToken())) {
                            api.setVisitTimes(vo.getVisitTimes());
                            break;
                        }
                    }
                });
            }
        }

        resultObj.put("tbodyList", apiList);

        return resultObj;
    }

}

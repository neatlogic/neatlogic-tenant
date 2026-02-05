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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
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
import neatlogic.framework.restful.dao.mapper.ApiAuditMapper;
import neatlogic.framework.restful.dao.mapper.ApiMapper;
import neatlogic.framework.restful.dto.ApiHandlerVo;
import neatlogic.framework.restful.dto.ApiVo;
import neatlogic.framework.restful.enums.ApiKind;
import neatlogic.framework.util.$;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@AuthAction(action = INTERFACE_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ApiManageSearchApi extends PrivateApiComponentBase {

    @Resource
    private ApiMapper ApiMapper;


    @Resource
    private ApiAuditMapper apiAuditMapper;

    @Override
    public String getToken() {
        return "apimanage/search";
    }

    @Override
    public String getName() {
        return "接口配置信息列表接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "needAudit", type = ApiParamType.ENUM, rule = "0,1", desc = "是否保存记录"),
            @Param(name = "keyword", type = ApiParamType.STRING, xss = true, desc = "关键字，接口名模糊查询"),
            @Param(name = "moduleGroup", type = ApiParamType.STRING, desc = "接口所属模块组"),
            @Param(name = "funcId", type = ApiParamType.STRING, desc = "接口所属功能"),
            @Param(name = "handler", type = ApiParamType.STRING, desc = "处理器"),
            @Param(name = "isActive", type = ApiParamType.ENUM, rule = "0,1", desc = "是否激活"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页码，默认值1"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "页大小，默认值10"),
            @Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否分页，默认值true")})
    @Output({@Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页码"), @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "页大小"), @Param(name = "pageCount", type = ApiParamType.INTEGER, desc = "总页数"), @Param(name = "rowNum", type = ApiParamType.INTEGER, desc = "总行数"), @Param(name = "tbodyList", explode = ApiVo[].class, isRequired = true, desc = "接口配置信息列表")})
    @Description(desc = "接口配置信息列表接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        List<ApiVo> apiList = new ArrayList<>();
        // ---------- 1. 参数解析 ----------
        ApiVo apiParam = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<>() {
        });
        String keyword = StringUtils.trimToNull(jsonObj.getString("keyword"));
        if (keyword != null && keyword.startsWith("/")) {
            keyword = keyword.substring(1);
        }

        // ---------- 2. DB API 预索引 ----------
        List<ApiVo> dbAllApiList = ApiMapper.getAllApiByModuleId(
                TenantContext.get().getActiveModuleList()
                        .stream()
                        .map(ModuleVo::getId)
                        .collect(Collectors.toList())
        );

        Map<String, ApiVo> dbApiMap = dbAllApiList.stream()
                .collect(Collectors.toMap(ApiVo::getToken, Function.identity(), (a, b) -> a));

        // ---------- 3. 构建统一过滤器 ----------
        Predicate<ApiVo> apiFilter = getApiVoPredicate(keyword, apiParam, dbApiMap);

        // ---------- 4. RAM API ----------
        List<ApiVo> ramApiList = new ArrayList<>();
        Set<String> ramTokenSet = new HashSet<>();
        Set<String> allTokenSet = new LinkedHashSet<>();

        for (ApiVo api : PrivateApiComponentFactory.getTenantActiveApiList()) {
            if (!apiFilter.test(api)) continue;
            ramApiList.add(api);
            ramTokenSet.add(api.getToken());
            allTokenSet.add(api.getToken());
        }

        // ---------- 5. DB API ----------
        Map<String, ApiVo> ramApiMap = PrivateApiComponentFactory.getApiMap();
        List<ApiVo> dbApiList = new ArrayList<>();
        Set<String> dbTokenSet = new HashSet<>();

        for (ApiVo api : dbAllApiList) {
            ApiVo ramApi = ramApiMap.get(api.getToken());
            if (ramApi != null) {
                api.setIsPrivate(true);
                api.setApiType(ApiKind.SYSTEM.getValue());
                api.setHandler(ramApi.getHandler());
                api.setName($.t(ramApi.getName()));
                api.setModuleId(ramApi.getModuleId());
                api.setAuthTypeList(ramApi.getAuthTypeList());
                ApiHandlerVo handler = PrivateApiComponentFactory.getApiHandlerByHandler(api.getHandler());
                if (handler != null) api.setHandlerName($.t(handler.getName()));
            }

            if (!apiFilter.test(api)) continue;

            dbApiList.add(api);
            dbTokenSet.add(api.getToken());
            allTokenSet.add(api.getToken());
        }

        // ---------- 6. Token 排序 ----------
        List<String> tokenList = new ArrayList<>(allTokenSet);
        tokenList.sort(String::compareTo);

        // ---------- 7. 分页 ----------
        JSONObject resultObj = new JSONObject();
        if (apiParam.getNeedPage()) {
            int rowNum = tokenList.size();
            resultObj.put("rowNum", rowNum);
            resultObj.put("pageSize", apiParam.getPageSize());
            resultObj.put("currentPage", apiParam.getCurrentPage());
            resultObj.put("pageCount", PageUtil.getPageCount(rowNum, apiParam.getPageSize()));

            int from = apiParam.getStartNum();
            int to = Math.min(from + apiParam.getPageSize(), rowNum);
            tokenList = from < rowNum ? tokenList.subList(from, to) : Collections.emptyList();

            ramTokenSet.retainAll(tokenList);
            dbTokenSet.retainAll(tokenList);
        }

        // ---------- 8. API 合并 ----------
        Map<String, ApiVo> apiMap = new HashMap<>();

        ramApiList.stream()
                .filter(a -> !apiParam.getNeedPage() || ramTokenSet.contains(a.getToken()))
                .forEach(a -> apiMap.put(a.getToken(), a));

        for (ApiVo api : dbApiList) {
            if (apiMap.containsKey(api.getToken())) {
                api.setIsDeletable(0);
                api.setAuthTypeList(apiMap.get(api.getToken()).getAuthTypeList());
            }
            apiMap.put(api.getToken(), api);
        }


        if(CollectionUtils.isNotEmpty(tokenList)) {
            // ---------- 9. 访问次数 ----------
            List<String> pageTokens = new ArrayList<>(tokenList);
            Map<String, Integer> visitTimesMap = apiAuditMapper
                    .getApiAccessCountByTokenList(pageTokens)
                    .stream()
                    .collect(Collectors.toMap(ApiVo::getToken, ApiVo::getVisitTimes));

            // ---------- 10. 输出 ----------
            for (String token : tokenList) {

                ApiVo api = apiMap.get(token);
                api.setVisitTimes(visitTimesMap.getOrDefault(token, 0));
                apiList.add(api);
            }
        }

        resultObj.put("tbodyList", apiList);
        return resultObj;

    }

    @NotNull
    private static Predicate<ApiVo> getApiVoPredicate(String keyword, ApiVo apiParam, Map<String, ApiVo> dbApiMap) {
        return api -> {

            if (apiParam.getIsActive() != null && !apiParam.getIsActive().equals(api.getIsActive())) {
                return false;
            }
            if (StringUtils.isNotBlank(apiParam.getApiType()) && !apiParam.getApiType().equals(api.getApiType())) {
                return false;
            }
            if (StringUtils.isNotBlank(apiParam.getModuleGroup())
                    && !apiParam.getModuleGroup().equals(api.getModuleGroup())) {
                return false;
            }
            if (StringUtils.isNotBlank(apiParam.getFuncId())) {
                String funcId = apiParam.getFuncId();
                if (api.getToken().contains("/")) {
                    if (!api.getToken().startsWith(funcId + "/")) return false;
                } else if (!api.getToken().equals(funcId)) {
                    return false;
                }
            }
            if (keyword != null) {
                boolean match =
                        (StringUtils.isNotBlank(api.getName()) && api.getName().contains(keyword)) ||
                                (StringUtils.isNotBlank(api.getToken()) && api.getToken().contains(keyword));
                if (!match) {
                    return false;
                }
            }
            if (apiParam.getNeedAudit() != null) {
                ApiVo dbApi = dbApiMap.get(api.getToken());
                Integer needAudit = dbApi != null ? dbApi.getNeedAudit() : api.getNeedAudit();
                if(!Objects.equals(apiParam.getNeedAudit(), needAudit)){
                    return false;
                }
            }

            if (StringUtils.isNotBlank(apiParam.getAuthType())) {
                return api.getAuthTypeList().contains(apiParam.getAuthType());
            }
            return true;
        };
    }

}

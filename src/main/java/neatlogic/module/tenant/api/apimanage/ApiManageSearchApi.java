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
import neatlogic.framework.restful.core.publicapi.PublicApiComponentFactory;
import neatlogic.framework.restful.dao.mapper.ApiAuditMapper;
import neatlogic.framework.restful.dao.mapper.ApiMapper;
import neatlogic.framework.restful.dto.ApiHandlerVo;
import neatlogic.framework.restful.dto.ApiVo;
import neatlogic.framework.restful.enums.ApiKind;
import neatlogic.framework.util.$;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
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
            @Param(name = "apiType", type = ApiParamType.STRING, desc = "接口类型(system|custom)"),
            @Param(name = "handler", type = ApiParamType.STRING, desc = "处理器"),
            @Param(name = "isActive", type = ApiParamType.ENUM, rule = "0,1", desc = "是否激活"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页码，默认值1"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "页大小，默认值10"),
            @Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否分页，默认值true")})
    @Output({@Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页码"), @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "页大小"), @Param(name = "pageCount", type = ApiParamType.INTEGER, desc = "总页数"), @Param(name = "rowNum", type = ApiParamType.INTEGER, desc = "总行数"), @Param(name = "tbodyList", explode = ApiVo[].class, isRequired = true, desc = "接口配置信息列表")})
    @Description(desc = "接口配置信息列表接口")
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
//				throw new ComponentNotFoundException("接口组件:" + handler + "不存在");
//			}
//		}
        List<ApiVo> dbAllApiList = ApiMapper.getAllApiByModuleId(TenantContext.get().getActiveModuleList().stream().map(ModuleVo::getId).collect(Collectors.toList()));
        List<ApiVo> ramApiList = new ArrayList<>();
        List<String> tokenList = new ArrayList<>();
        List<String> ramTokenList = new ArrayList<>();
        //从内存中取出符合搜索条件的api、token数据
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
            //根据接口类型筛选接口（用于接口管理页的系统接口/自定义接口的相互切换）
            if (StringUtils.isNotBlank(apiVo.getApiType()) && !apiVo.getApiType().equals(api.getApiType())) {
                continue;
            }
            //根据模块筛选接口（用于接口管理页的目录树筛选）
            if (StringUtils.isNotBlank(apiVo.getModuleGroup()) && !apiVo.getModuleGroup().equals(api.getModuleGroup())) {
                continue;
            }
            //根据功能筛选接口（用于接口管理页的目录树筛选）
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
            // 系统接口默认关闭审计，开启审计的接口会在数据库有记录，所以先从数据库看是否有记录
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
            ApiVo ramApi = ramApiMap.get(api.getToken());
            if (ramApi != null) {
                api.setIsPrivate(true);
                api.setApiType(ApiKind.SYSTEM.getValue());
                api.setHandler(ramApi.getHandler());
                api.setName($.t(ramApi.getName()));
                api.setModuleId(ramApi.getModuleId());
                ApiHandlerVo apiHandlerVo = PrivateApiComponentFactory.getApiHandlerByHandler(api.getHandler());
                if (apiHandlerVo != null) {
                    api.setHandlerName($.t(apiHandlerVo.getName()));
                }
            } else {
                ApiHandlerVo publicApiHandler = PublicApiComponentFactory.getApiHandlerByHandler(api.getHandler());
                if (publicApiHandler == null) {
                    api.setHandlerName("接口组件:" + api.getHandler() + "不存在");
                } else {
                    api.setHandlerName($.t(publicApiHandler.getName()));
                }
                api.setIsPrivate(false);
                api.setApiType(ApiKind.CUSTOM.getValue());
            }

            //根据接口类型筛选接口（用于接口管理页的系统接口/自定义接口的相互切换）
            if (StringUtils.isNotBlank(apiVo.getApiType()) && !apiVo.getApiType().equals(api.getApiType())) {
                continue;
            }
            //根据模块筛选接口（用于接口管理页的目录树筛选）
            if (StringUtils.isNotBlank(apiVo.getModuleGroup()) && !apiVo.getModuleGroup().equals(api.getModuleGroup())) {
                continue;
            }
            //根据功能筛选接口（用于接口管理页的目录树筛选）
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
        //从数据库中取出符合搜索条件的token数据
//		List<String> dbTokenList = ApiMapper.getApiTokenList(apiVo);
        //将内存和数据库取出的token合并去重
        for (String token : dbTokenList) {
            if (tokenList.contains(token)) {
                continue;
            }
            tokenList.add(token);
        }
        //token排序(如果不是搜索自定义接口，就按token排序)
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
            //取出当前页token
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
            //从内存中取出当前页api数据，保存在map中
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

        //从map中按顺序取出api数据
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
         * 根据token获取每个API的访问次数，并保存在ApiVo的visitTimes字段中
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

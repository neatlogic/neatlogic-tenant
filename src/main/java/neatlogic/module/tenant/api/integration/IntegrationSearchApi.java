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

package neatlogic.module.tenant.api.integration;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.NoAuth;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.constvalue.GroupSearch;
import neatlogic.framework.common.constvalue.ParamType;
import neatlogic.framework.common.constvalue.UserType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.dao.mapper.RoleMapper;
import neatlogic.framework.dao.mapper.TeamMapper;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dto.AuthorityVo;
import neatlogic.framework.dto.RoleVo;
import neatlogic.framework.dto.TeamVo;
import neatlogic.framework.dto.UserVo;
import neatlogic.framework.dto.WorkAssignmentUnitVo;
import neatlogic.framework.integration.dto.IntegrationAuthorityVo;
import neatlogic.framework.dependency.constvalue.FrameworkFromType;
import neatlogic.framework.dependency.core.DependencyManager;
import neatlogic.framework.integration.dao.mapper.IntegrationMapper;
import neatlogic.framework.integration.dto.IntegrationVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.TableResultUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AuthAction(action = NoAuth.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class IntegrationSearchApi extends PrivateApiComponentBase {

    @Resource
    private IntegrationMapper integrationMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private TeamMapper teamMapper;

    @Resource
    private RoleMapper roleMapper;

    @Override
    public String getToken() {
        return "integration/search";
    }

    @Override
    public String getName() {
        return "nmtai.integrationsearchapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "keyword", type = ApiParamType.STRING, desc = "关键字"),
            @Param(name = "defaultValue", type = ApiParamType.JSONARRAY, desc = "回显值"),
            @Param(name = "handler", type = ApiParamType.STRING, desc = "组件"),
            @Param(name = "isActive", type = ApiParamType.ENUM, rule = "0,1", desc = "启用"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页数量")})
    @Output({@Param(explode = BasePageVo.class), @Param(name = "integrationList", explode = IntegrationVo[].class, desc = "集成设置列表")})
    @Description(desc = "nmtai.integrationsearchapi.getname")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        List<IntegrationVo> integrationList;
        IntegrationVo integrationVo = JSON.toJavaObject(jsonObj, IntegrationVo.class);
        JSONArray defaultValue = integrationVo.getDefaultValue();
        if (CollectionUtils.isNotEmpty(defaultValue)) {
            List<String> uuidList = defaultValue.toJavaList(String.class);
            integrationList = integrationMapper.getIntegrationListByUuidList(uuidList);
        } else {
            integrationList = integrationMapper.searchIntegration(integrationVo);
            if (!integrationList.isEmpty()) {
                int rowNum = integrationMapper.searchIntegrationCount(integrationVo);
                integrationVo.setRowNum(rowNum);
            }
        }
        //补充类型对应表达式信息
        if (CollectionUtils.isNotEmpty(integrationList)) {
            List<String> integrationUuidList = integrationList.stream().map(IntegrationVo::getUuid).collect(Collectors.toList());
            List<IntegrationAuthorityVo> authorityVoList = integrationMapper.getIntegrationAuthorityListByIntegrationUuidListAndAction(integrationUuidList, "execute");
            Map<String, List<IntegrationAuthorityVo>> authorityMap = authorityVoList.stream().collect(Collectors.groupingBy(IntegrationAuthorityVo::getIntegrationUuid));
            Set<String> userUuidSet = authorityVoList.stream().filter(o -> Objects.equals(o.getType(), GroupSearch.USER.getValue())).map(AuthorityVo::getUuid).collect(Collectors.toSet());
            Set<String> teamUuidSet = authorityVoList.stream().filter(o -> Objects.equals(o.getType(), GroupSearch.TEAM.getValue())).map(AuthorityVo::getUuid).collect(Collectors.toSet());
            Set<String> roleUuidSet = authorityVoList.stream().filter(o -> Objects.equals(o.getType(), GroupSearch.ROLE.getValue())).map(AuthorityVo::getUuid).collect(Collectors.toSet());
            Map<String, UserVo> userMap = CollectionUtils.isNotEmpty(userUuidSet)
                    ? userMapper.getUserListByUuidList(new ArrayList<>(userUuidSet)).stream().collect(Collectors.toMap(UserVo::getUuid, user -> user, (a, b) -> a))
                    : null;
            Map<String, TeamVo> teamMap = CollectionUtils.isNotEmpty(teamUuidSet)
                    ? teamMapper.getTeamByUuidList(new ArrayList<>(teamUuidSet)).stream().collect(Collectors.toMap(TeamVo::getUuid, team -> team, (a, b) -> a))
                    : null;
            Map<String, RoleVo> roleMap = CollectionUtils.isNotEmpty(roleUuidSet)
                    ? roleMapper.getRoleByUuidList(new ArrayList<>(roleUuidSet)).stream().collect(Collectors.toMap(RoleVo::getUuid, role -> role, (a, b) -> a))
                    : null;
            for (IntegrationVo inte : integrationList) {
                JSONObject paramJson = inte.getConfig().getJSONObject("param");
                if (paramJson != null) {
                    JSONArray paramList = paramJson.getJSONArray("paramList");
                    if (CollectionUtils.isNotEmpty(paramList)) {
                        for (Object paramObj : paramList) {
                            JSONObject param = (JSONObject) paramObj;
                            //设置typeName
                            String type = param.getString("type");
                            if (StringUtils.isNotBlank(type)) {
                                ParamType pt = ParamType.getParamType(type);
                                if (pt != null) {
                                    //增加参数回显模版-赖文韬-202006291121
                                    String freemarkerTemplate = pt.getFreemarkerTemplate(param.getString("name"));
                                    param.put("freemarkerTemplate", freemarkerTemplate);
                                    param.put("expresstionList", pt.getExpressionJSONArray());
                                    param.put("typeName", Objects.requireNonNull(pt).getText());
                                }
                            }
                        }
                    }
                }
                int count = DependencyManager.getDependencyCount(FrameworkFromType.INTEGRATION, inte.getUuid());
                inte.setReferenceCount(count);
                JSONArray authorityVoArray = new JSONArray();
                List<IntegrationAuthorityVo> currentAuthorityList = authorityMap.get(inte.getUuid());
                if (currentAuthorityList == null) {
                    currentAuthorityList = new ArrayList<>();
                }
                inte.setExecuteAuthorityList(AuthorityVo.getAuthorityList(new ArrayList<>(currentAuthorityList)));
                if (CollectionUtils.isNotEmpty(currentAuthorityList)) {
                    for (IntegrationAuthorityVo authorityVo : currentAuthorityList) {
                        if (Objects.equals(authorityVo.getType(), GroupSearch.USER.getValue())) {
                            UserVo userVo = userMap != null ? userMap.get(authorityVo.getUuid()) : null;
                            if (userVo != null) {
                                authorityVoArray.add(new WorkAssignmentUnitVo(userVo));
                            }
                        } else if (Objects.equals(authorityVo.getType(), GroupSearch.TEAM.getValue())) {
                            TeamVo teamVo = teamMap != null ? teamMap.get(authorityVo.getUuid()) : null;
                            if (teamVo != null) {
                                authorityVoArray.add(new WorkAssignmentUnitVo(teamVo));
                            }
                        } else if (Objects.equals(authorityVo.getType(), GroupSearch.ROLE.getValue())) {
                            RoleVo roleVo = roleMap != null ? roleMap.get(authorityVo.getUuid()) : null;
                            if (roleVo != null) {
                                authorityVoArray.add(new WorkAssignmentUnitVo(roleVo));
                            }
                        } else if (Objects.equals(authorityVo.getType(), GroupSearch.COMMON.getValue())) {
                            WorkAssignmentUnitVo workAssignmentUnitVo = new WorkAssignmentUnitVo();
                            workAssignmentUnitVo.setUuid(authorityVo.getUuid());
                            workAssignmentUnitVo.setName(UserType.getText(authorityVo.getUuid()));
                            workAssignmentUnitVo.setInitType(GroupSearch.COMMON.getValue());
                            authorityVoArray.add(workAssignmentUnitVo);
                        }
                    }
                }
                inte.setExecuteAuthorityVoList(authorityVoArray);
            }
        }
        return TableResultUtil.getResult(integrationList, integrationVo);
    }
}

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

package neatlogic.module.tenant.api.role;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.util.PageUtil;
import neatlogic.framework.dao.mapper.RoleMapper;
import neatlogic.framework.dto.RoleVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.service.RoleService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class RoleSearchApi extends PrivateApiComponentBase {

    @Autowired
    private RoleMapper roleMapper;

    @Resource
    private RoleService roleService;

    @Override
    public String getToken() {
        return "role/search";
    }

    @Override
    public String getName() {
        return "角色查询接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "keyword",
                    type = ApiParamType.STRING,
                    desc = "关键字，匹配名称或说明", xss = true),
            @Param(name = "authGroup",
                    type = ApiParamType.STRING,
                    desc = "权限模块"),
            @Param(name = "auth",
                    type = ApiParamType.STRING,
                    desc = "权限"),
            @Param(name = "needPage",
                    type = ApiParamType.BOOLEAN,
                    desc = "是否需要分页，默认true"),
            @Param(name = "pageSize",
                    type = ApiParamType.INTEGER,
                    desc = "每页条目"),
            @Param(name = "currentPage",
                    type = ApiParamType.INTEGER,
                    desc = "当前页")})
    @Output({
            @Param(name = "tbodyList",
                    explode = RoleVo[].class,
                    desc = "table数据列表"),
            @Param(name = "pageSize",
                    type = ApiParamType.INTEGER,
                    desc = "每页数据条目"),
            @Param(name = "currentPage",
                    type = ApiParamType.INTEGER,
                    desc = "当前页"),
            @Param(name = "rowNum",
                    type = ApiParamType.INTEGER,
                    desc = "返回条目总数"),
            @Param(name = "pageCount",
                    type = ApiParamType.INTEGER,
                    desc = "总页数")})
    @Description(desc = "角色查询接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject returnObj = new JSONObject();
        RoleVo roleVo = JSON.toJavaObject(jsonObj, RoleVo.class);
        if (roleVo.getNeedPage()) {
            int rowNum = roleMapper.searchRoleCount(roleVo);
            returnObj.put("pageSize", roleVo.getPageSize());
            returnObj.put("currentPage", roleVo.getCurrentPage());
            returnObj.put("rowNum", rowNum);
            returnObj.put("pageCount", PageUtil.getPageCount(rowNum, roleVo.getPageSize()));
        }
        List<RoleVo> roleList = roleMapper.searchRole(roleVo);
        roleService.setRoleTeamCountAndRoleUserCount(roleList);
        returnObj.put("tbodyList", roleList);
        return returnObj;
    }
}

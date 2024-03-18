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

package neatlogic.module.tenant.api.user;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.common.util.PageUtil;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dto.UserTitleVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class UserTitleListApi extends PrivateApiComponentBase {

    @Resource
    private UserMapper userMapper;

    @Override
    public String getToken() {
        return "user/title/search";
    }

    @Override
    public String getName() {
        return "查询用户头衔列表";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "keyword", type = ApiParamType.STRING, xss = true, desc = "关键词"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页数据条目"),
            @Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否需要分页，默认true")
    })
    @Output({
            @Param(name = "tbodyList", explode = UserTitleVo[].class, desc = "列表"),
            @Param(explode = BasePageVo.class),
    })
    @Description(desc = "查询用户头衔列表接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject resultObj = new JSONObject();
        UserTitleVo userTitleVo = JSON.toJavaObject(jsonObj, UserTitleVo.class);
        if (userTitleVo.getNeedPage()) {
            int rowNum = userMapper.searchUserTitleCount(userTitleVo);
            resultObj.put("rowNum", rowNum);
            resultObj.put("pageSize", userTitleVo.getPageSize());
            resultObj.put("currentPage", userTitleVo.getCurrentPage());
            resultObj.put("pageCount", PageUtil.getPageCount(rowNum, userTitleVo.getPageSize()));
        }
        resultObj.put("tbodyList", userMapper.searchUserTitle(userTitleVo));
        return resultObj;
    }
}

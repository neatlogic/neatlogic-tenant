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

package neatlogic.module.tenant.api.form;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.form.dao.mapper.FormMapper;
import neatlogic.framework.form.dto.FormCustomItemVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.TableResultUtil;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class SearchFormCustomItemApi extends PrivateApiComponentBase {
    @Resource
    private FormMapper formMapper;

    @Override
    public String getName() {
        return "查询表单自定义组件";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "keyword", type = ApiParamType.STRING, desc = "关键字"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页大小"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页码")})
    @Output({@Param(name = "tbodyList", explode = FormCustomItemVo[].class),
            @Param(explode = BasePageVo.class)})
    @Description(desc = "查询表单自定义组件接口")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        FormCustomItemVo formCustomItemVo = JSONObject.toJavaObject(paramObj, FormCustomItemVo.class);
        List<FormCustomItemVo> formCustomItemList = formMapper.searchFormCustomItem(formCustomItemVo);
        if (CollectionUtils.isNotEmpty(formCustomItemList)) {
            int rowNum = formMapper.searchFormCustomItemCount(formCustomItemVo);
            formCustomItemVo.setRowNum(rowNum);
        }
        return TableResultUtil.getResult(formCustomItemList, formCustomItemVo);
    }

    @Override
    public String getToken() {
        return "/form/customitem/search";
    }
}

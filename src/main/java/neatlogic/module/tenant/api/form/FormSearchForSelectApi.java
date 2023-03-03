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

package neatlogic.module.tenant.api.form;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.ValueTextVo;
import neatlogic.framework.common.util.PageUtil;
import neatlogic.framework.form.dao.mapper.FormMapper;
import neatlogic.framework.form.dto.FormVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Deprecated
@OperationType(type = OperationTypeEnum.SEARCH)
public class FormSearchForSelectApi extends PrivateApiComponentBase {

    @Autowired
    private FormMapper formMapper;

    @Override
    public String getToken() {
        return "form/search/forselect";
    }

    @Override
    public String getName() {
        return "查询表单列表_下拉框";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "keyword", type = ApiParamType.STRING, desc = "关键字，匹配名称", xss = true),
            @Param(name = "isActive", type = ApiParamType.ENUM, desc = "是否激活", rule = "0,1"),
            @Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否需要分页，默认true"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页条目"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页")
    })
    @Output({
            @Param(name = "currentPage", type = ApiParamType.INTEGER, isRequired = true, desc = "当前页码"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, isRequired = true, desc = "页大小"),
            @Param(name = "pageCount", type = ApiParamType.INTEGER, isRequired = true, desc = "总页数"),
            @Param(name = "rowNum", type = ApiParamType.INTEGER, isRequired = true, desc = "总行数"),
            @Param(name = "list", explode = ValueTextVo[].class, desc = "表单列表")
    })
    @Description(desc = "查询表单列表_下拉框")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        FormVo formVo = JSON.toJavaObject(jsonObj, FormVo.class);
        JSONObject resultObj = new JSONObject();
        if (formVo.getNeedPage()) {
            int rowNum = formMapper.searchFormCount(formVo);
            int pageCount = PageUtil.getPageCount(rowNum, formVo.getPageSize());
            formVo.setPageCount(pageCount);
            resultObj.put("currentPage", formVo.getCurrentPage());
            resultObj.put("pageSize", formVo.getPageSize());
            resultObj.put("pageCount", pageCount);
            resultObj.put("rowNum", rowNum);
        }
        List<ValueTextVo> formList = formMapper.searchFormListForSelect(formVo);
        resultObj.put("list", formList);
        return resultObj;
    }

}

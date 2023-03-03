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

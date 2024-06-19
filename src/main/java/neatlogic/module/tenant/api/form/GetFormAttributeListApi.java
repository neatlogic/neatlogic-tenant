/*
 * Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package neatlogic.module.tenant.api.form;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.crossover.CrossoverServiceFactory;
import neatlogic.framework.form.dao.mapper.FormMapper;
import neatlogic.framework.form.dto.FormAttributeVo;
import neatlogic.framework.form.dto.FormVo;
import neatlogic.framework.form.exception.FormNotFoundException;
import neatlogic.framework.form.service.IFormCrossoverService;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class GetFormAttributeListApi extends PrivateApiComponentBase {

    @Resource
    private FormMapper formMapper;

    @Override
    public String getName() {
        return "nmtaf.getformattributelistapi.getname";
    }

    @Input({
            @Param(name = "formUuid", type = ApiParamType.STRING, desc = "term.framework.formuuid", isRequired = true),
            @Param(name = "tag", type = ApiParamType.STRING, desc = "common.tag")
    })
    @Output({
            @Param(name = "tbodyList", explode = FormAttributeVo[].class, desc = "common.tbodylist")
    })
    @Description(desc = "nmtaf.getformattributelistapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        String formUuid = paramObj.getString("formUuid");
        String tag = paramObj.getString("tag");
        FormVo form = formMapper.getFormByUuid(formUuid);
        if (form == null) {
            throw new FormNotFoundException(formUuid);
        }
        IFormCrossoverService formCrossoverService = CrossoverServiceFactory.getApi(IFormCrossoverService.class);
//        return formCrossoverService.getFormAttributeList(formUuid, form.getName(), tag);
        return formCrossoverService.getFormAttributeListNew(formUuid, form.getName(), tag);
    }

    @Override
    public String getToken() {
        return "form/attribute/list";
    }
}

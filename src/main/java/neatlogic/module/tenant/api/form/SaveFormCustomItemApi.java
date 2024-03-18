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

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.FORM_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.exception.form.FormCustomItemNameExistsException;
import neatlogic.framework.form.dao.mapper.FormMapper;
import neatlogic.framework.form.dto.FormCustomItemVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@AuthAction(action = FORM_MODIFY.class)
@OperationType(type = OperationTypeEnum.UPDATE)
public class SaveFormCustomItemApi extends PrivateApiComponentBase {
    @Resource
    private FormMapper formMapper;

    @Override
    public String getName() {
        return "保存表单自定义组件";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "id", type = ApiParamType.LONG, desc = "组件id"),
            @Param(name = "name", type = ApiParamType.STRING, desc = "组件唯一标识")})
    @Output({@Param(explode = FormCustomItemVo.class)})
    @Description(desc = "保存表单自定义组件接口")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        Long id = paramObj.getLong("id");
        FormCustomItemVo formCustomItemVo = JSONObject.toJavaObject(paramObj, FormCustomItemVo.class);
        if (formMapper.checkFormCustomItemNameIsExists(formCustomItemVo) > 0) {
            throw new FormCustomItemNameExistsException(formCustomItemVo.getName());
        }
        if (id == null) {
            formMapper.insertFormCustomItem(formCustomItemVo);
        } else {
            formMapper.updateFormCustomItem(formCustomItemVo);
        }
        return null;
    }

    @Override
    public String getToken() {
        return "/form/customitem/save";
    }
}

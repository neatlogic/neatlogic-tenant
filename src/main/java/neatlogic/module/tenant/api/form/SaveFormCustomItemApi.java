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

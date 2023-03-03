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
import neatlogic.framework.dependency.constvalue.FrameworkFromType;
import neatlogic.framework.dependency.core.DependencyManager;
import neatlogic.framework.exception.type.ParamNotExistsException;
import neatlogic.framework.form.dao.mapper.FormMapper;
import neatlogic.framework.form.dto.FormVersionVo;
import neatlogic.framework.form.dto.FormVo;
import neatlogic.framework.form.exception.FormActiveVersionNotFoundExcepiton;
import neatlogic.framework.form.exception.FormNotFoundException;
import neatlogic.framework.form.exception.FormVersionNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class FormGetApi extends PrivateApiComponentBase {

    @Resource
    private FormMapper formMapper;

    @Override
    public String getToken() {
        return "form/get";
    }

    @Override
    public String getName() {
        return "单个表单查询接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Override
    @Input({
            @Param(name = "uuid", type = ApiParamType.STRING, desc = "表单uuid"),
            @Param(name = "currentVersionUuid", type = ApiParamType.STRING, desc = "选择表单版本uuid"),
    })
    @Output({@Param(explode = FormVo.class)})
    @Description(desc = "单个表单查询接口")
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String currentVersionUuid = jsonObj.getString("currentVersionUuid");
        String uuid = jsonObj.getString("uuid");
        if (StringUtils.isNotBlank(currentVersionUuid)) {
            FormVersionVo formVersion = formMapper.getFormVersionByUuid(currentVersionUuid);
            if (formVersion == null) {
                throw new FormVersionNotFoundException(currentVersionUuid);
            }
            FormVo formVo = formMapper.getFormByUuid(formVersion.getFormUuid());
            if (formVo == null) {
                throw new FormNotFoundException(formVersion.getFormUuid());
            }
            formVo.setCurrentVersionUuid(currentVersionUuid);
            formVo.setFormConfig(formVersion.getFormConfig());
            List<FormVersionVo> formVersionList = formMapper.getFormVersionSimpleByFormUuid(formVersion.getFormUuid());
            formVo.setVersionList(formVersionList);
            int count = DependencyManager.getDependencyCount(FrameworkFromType.FORM, formVo.getUuid());
            formVo.setReferenceCount(count);
            return formVo;
        } else if (StringUtils.isNotBlank(uuid)) {
            FormVo formVo = formMapper.getFormByUuid(uuid);
            if (formVo == null) {
                throw new FormNotFoundException(uuid);
            }
            FormVersionVo formVersion = formMapper.getActionFormVersionByFormUuid(uuid);
            if (formVersion == null) {
                throw new FormActiveVersionNotFoundExcepiton(uuid);
            }
            formVo.setCurrentVersionUuid(formVersion.getUuid());
            formVo.setFormConfig(formVersion.getFormConfig());
            List<FormVersionVo> formVersionList = formMapper.getFormVersionSimpleByFormUuid(uuid);
            formVo.setVersionList(formVersionList);
            int count = DependencyManager.getDependencyCount(FrameworkFromType.FORM, formVo.getUuid());
            formVo.setReferenceCount(count);
            return formVo;
        } else {
            throw new ParamNotExistsException("uuid", "currentVersionUuid");
        }
    }

}

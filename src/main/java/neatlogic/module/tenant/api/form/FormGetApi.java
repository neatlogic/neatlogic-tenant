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

import com.alibaba.fastjson.JSONObject;
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
        return "获取表单信息";
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
    @Description(desc = "获取表单信息")
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

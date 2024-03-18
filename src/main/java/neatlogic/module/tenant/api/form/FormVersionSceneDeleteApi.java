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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.FORM_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.form.dao.mapper.FormMapper;
import neatlogic.framework.form.dto.FormVersionVo;
import neatlogic.framework.form.exception.FormVersionNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Iterator;
import java.util.Objects;

@Service
@Transactional
@AuthAction(action = FORM_MODIFY.class)
@OperationType(type = OperationTypeEnum.UPDATE)
public class FormVersionSceneDeleteApi extends PrivateApiComponentBase {

    @Resource
    private FormMapper formMapper;

    @Override
    public String getToken() {
        return "form/version/scene/delete";
    }

    @Override
    public String getName() {
        return "删除表单版本场景";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Override
    @Input({
            @Param(name = "versionUuid", type = ApiParamType.STRING, desc = "版本uuid", isRequired = true),
            @Param(name = "sceneUuid", type = ApiParamType.STRING, desc = "场景uuid", isRequired = true)
    })
    @Output({})
    @Description(desc = "删除表单版本场景")
    public Object myDoService(JSONObject paramObj) throws Exception {
        String versionUuid = paramObj.getString("versionUuid");
        FormVersionVo formVersionVo = formMapper.getFormVersionByUuid(versionUuid);
        if (formVersionVo == null) {
            throw new FormVersionNotFoundException(versionUuid);
        }
        JSONObject config = formVersionVo.getFormConfig();
        if (MapUtils.isEmpty(config)) {
            return null;
        }
        JSONArray sceneList = config.getJSONArray("sceneList");
        if (CollectionUtils.isEmpty(sceneList)) {
            return null;
        }
        String sceneUuid = paramObj.getString("sceneUuid");
        Iterator<Object> iterator = sceneList.iterator();
        while (iterator.hasNext()) {
            Object element = iterator.next();
            if (element == null) {
                iterator.remove();
                continue;
            }
            if (element instanceof JSONObject) {
                JSONObject scene = (JSONObject) element;
                if (MapUtils.isEmpty(scene)) {
                    iterator.remove();
                    continue;
                }
                String uuid = scene.getString("uuid");
                if (StringUtils.isBlank(uuid)) {
                    iterator.remove();
                    continue;
                }
                if (Objects.equals(sceneUuid, uuid)) {
                    iterator.remove();
                    break;
                }
            }
        }
        config.put("sceneList", sceneList);
        formVersionVo.setFormConfig(config);
        formMapper.updateFormVersionConfigByUuid(formVersionVo);
        return null;
    }

}

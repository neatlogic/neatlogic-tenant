/*
 *
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 */

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

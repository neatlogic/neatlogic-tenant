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

package neatlogic.module.tenant.api.theme;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.THEME_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dao.mapper.ThemeMapper;
import neatlogic.framework.dto.ThemeVo;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * @author longrf
 * @date 2022/4/8 3:53 下午
 */

@AuthAction(action = THEME_MODIFY.class)
@Service
@Transactional
@OperationType(type = OperationTypeEnum.CREATE)
public class ThemeSaveApi extends PrivateApiComponentBase {

    @Resource
    ThemeMapper themeMapper;

    @Override
    public String getName() {
        return "保存主题配置";
    }

    @Override
    public String getToken() {
        return "theme/save";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "config", type = ApiParamType.JSONOBJECT, desc = "主题配置")
    })
    @Description(desc = "用于保存主题配置、还原主题配置")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        ThemeVo themeVo = paramObj.toJavaObject(ThemeVo.class);
        themeMapper.deleteTheme();
        if (MapUtils.isNotEmpty(themeVo.getConfig())) {
            themeMapper.insertTheme(themeVo);
        }
        return null;
    }
}

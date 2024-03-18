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

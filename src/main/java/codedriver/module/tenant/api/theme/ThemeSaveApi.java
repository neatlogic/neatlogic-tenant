/*
 * Copyright(c) 2022 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.theme;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.auth.label.THEME_MODIFY;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dao.mapper.ThemeMapper;
import codedriver.framework.dto.ThemeVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
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

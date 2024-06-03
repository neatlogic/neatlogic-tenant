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

package neatlogic.module.tenant.api.dynamicplugin;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.dynamicplugin.PluginClassLoader;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.file.dao.mapper.FileMapper;
import neatlogic.framework.file.dto.FileVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;

@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class PluginLoadTestApi extends PrivateApiComponentBase {

    @Autowired
    private FileMapper fileMapper;

    @Override
    public String getName() {
        return "测试加载插件";
    }

    @Input({
            @Param(name = "fileId", type = ApiParamType.LONG, desc = ""),
    })
    @Output({

    })
    @Description(desc = "测试加载插件")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        Long fileId = paramObj.getLong("fileId");
        FileVo file = fileMapper.getFileById(fileId);
        String path = file.getPath();
        PluginClassLoader pluginClassLoader = new PluginClassLoader(PluginLoadTestApi.class.getClassLoader());
        Class<?> aClass = pluginClassLoader.loadClass(path);
        Object helloWorld = aClass.newInstance();
        Method welcomeMethod = aClass.getMethod("welcome");
        String result = (String) welcomeMethod.invoke(helloWorld);
        return result;
    }

    @Override
    public String getToken() {
        return "plugin/load/test";
    }
}

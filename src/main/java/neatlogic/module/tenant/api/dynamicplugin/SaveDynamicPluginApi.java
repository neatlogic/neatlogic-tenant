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
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.crossover.CrossoverServiceFactory;
import neatlogic.framework.dynamicplugin.DynamicPluginManager;
import neatlogic.framework.dynamicplugin.crossover.IDynamicPluginCrossoverMapper;
import neatlogic.framework.dynamicplugin.dto.DynamicPluginVo;
import neatlogic.framework.exception.file.FileNotFoundException;
import neatlogic.framework.file.dao.mapper.FileMapper;
import neatlogic.framework.file.dto.FileVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

@Service
@OperationType(type = OperationTypeEnum.UPDATE)
public class SaveDynamicPluginApi extends PrivateApiComponentBase {

    @Resource
    private FileMapper fileMapper;



    @Override
    public String getName() {
        return "保存动态插件";
    }
    @Input({
            @Param(name = "id", type = ApiParamType.LONG, desc = "common.id"),
            @Param(name = "key", type = ApiParamType.STRING, isRequired = true, desc = "common.key"),
            @Param(name = "name", type = ApiParamType.STRING, isRequired = true, desc = "common.name"),
            @Param(name = "type", type = ApiParamType.STRING, isRequired = true, desc = "common.type"),
            @Param(name = "fileId", type = ApiParamType.LONG, isRequired = true, desc = "common.fileid"),
            @Param(name = "description", type = ApiParamType.STRING, desc = "common.description")
    })
    @Output({
            @Param(name = "id", type = ApiParamType.LONG, isRequired = true, desc = "common.id")
    })
    @Description(desc = "保存动态插件")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        DynamicPluginVo dynamicPlugin = paramObj.toJavaObject(DynamicPluginVo.class);

        FileVo fileVo = fileMapper.getFileById(dynamicPlugin.getFileId());
        if (fileVo == null) {
            throw new FileNotFoundException(dynamicPlugin.getFileId());
        }
        if (Objects.equals(fileVo.getType(), "dynamicPlugin")) {

        }
        Class<?> aClass = DynamicPluginManager.loadPlugin(fileVo);
        dynamicPlugin.setClassName(aClass.getName());
        dynamicPlugin.setFileName(fileVo.getName());
        dynamicPlugin.setFcu(UserContext.get().getUserId());
        dynamicPlugin.setLcu(UserContext.get().getUserId());
        IDynamicPluginCrossoverMapper dynamicPluginCrossoverMapper = CrossoverServiceFactory.getApi(IDynamicPluginCrossoverMapper.class);
        dynamicPluginCrossoverMapper.insertDynamicPlugin(dynamicPlugin);
        JSONObject resultObj = new JSONObject();
        resultObj.put("id", dynamicPlugin.getId());
        return resultObj;
    }

    @Override
    public String getToken() {
        return "dynamicplugin/save";
    }
}

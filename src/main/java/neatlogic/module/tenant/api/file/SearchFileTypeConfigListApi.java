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

package neatlogic.module.tenant.api.file;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.FILE_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.file.core.FileTypeHandlerFactory;
import neatlogic.framework.file.dao.mapper.FileMapper;
import neatlogic.framework.file.dto.FileTypeVo;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
@AuthAction(action = FILE_MODIFY.class)
public class SearchFileTypeConfigListApi extends PrivateApiComponentBase {

    @Resource
    private FileMapper fileMapper;

    @Override
    public String getToken() {
        return "file/type/config/list";
    }

    @Override
    public String getName() {
        return "查询附件类型规则";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Output({
            @Param(name = "name", type = ApiParamType.STRING, desc = "附件类型"),
            @Param(name = "displayName", type = ApiParamType.STRING, desc = "附件类型名称"),
            @Param(name = "whiteList", type = ApiParamType.JSONARRAY, desc = "允许上传扩展名列表"),
            @Param(name = "blackList", type = ApiParamType.JSONARRAY, desc = "禁止上传扩展名列表"),
            @Param(name = "maxSize", type = ApiParamType.LONG, desc = "最大文件大小，单位为字节")
    })
    @Description(desc = "查询附件类型规则接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Map<String, FileTypeVo> activeFileTypeMap = new HashMap<>();
        for (FileTypeVo fileTypeVo : FileTypeHandlerFactory.getActiveFileTypeHandler()) {
            activeFileTypeMap.put(fileTypeVo.getName().toUpperCase(Locale.ENGLISH), fileTypeVo);
        }

        JSONArray returnList = new JSONArray();
        List<FileTypeVo> fileTypeConfigList = fileMapper.searchFileTypeConfigList();
        for (FileTypeVo fileTypeConfigVo : fileTypeConfigList) {
            FileTypeVo activeFileTypeVo = activeFileTypeMap.get(fileTypeConfigVo.getName().toUpperCase(Locale.ENGLISH));
            if (activeFileTypeVo == null) {
                continue;
            }
            JSONObject configObj = fileTypeConfigVo.getConfigObj();
            JSONObject resultObj = new JSONObject();
            resultObj.put("name", fileTypeConfigVo.getName());
            resultObj.put("displayName", activeFileTypeVo.getDisplayName());
            if (configObj != null) {
                // 前端规则表只展示 UploadFileApi 实际消费的三个字段，不暴露原始配置 JSON。
                resultObj.put("whiteList", configObj.getJSONArray("whiteList"));
                resultObj.put("blackList", configObj.getJSONArray("blackList"));
                long maxSize = configObj.getLongValue("maxSize");
                if (maxSize > 0) {
                    resultObj.put("maxSize", maxSize);
                }
            }
            returnList.add(resultObj);
        }
        return returnList;
    }
}

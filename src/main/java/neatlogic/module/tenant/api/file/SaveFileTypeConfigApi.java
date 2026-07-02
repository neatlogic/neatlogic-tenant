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
import neatlogic.framework.exception.file.FileTypeHandlerNotFoundException;
import neatlogic.framework.exception.type.ParamIrregularException;
import neatlogic.framework.file.core.FileTypeHandlerFactory;
import neatlogic.framework.file.dao.mapper.FileMapper;
import neatlogic.framework.file.dto.FileTypeVo;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Locale;

@Service
@Transactional
@OperationType(type = OperationTypeEnum.UPDATE)
@AuthAction(action = FILE_MODIFY.class)
public class SaveFileTypeConfigApi extends PrivateApiComponentBase {

    @Resource
    private FileMapper fileMapper;

    @Override
    public String getToken() {
        return "file/type/config/save";
    }

    @Override
    public String getName() {
        return "保存附件类型规则";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "name", type = ApiParamType.STRING, isRequired = true, desc = "附件类型"),
            @Param(name = "config", type = ApiParamType.JSONOBJECT, desc = "附件类型规则配置")
    })
    @Description(desc = "保存附件类型规则接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String name = jsonObj.getString("name");
        FileTypeVo activeFileTypeVo = FileTypeHandlerFactory.getActiveFileTypeHandlerByType(name);
        if (activeFileTypeVo == null) {
            throw new FileTypeHandlerNotFoundException(name);
        }

        JSONObject configObj = jsonObj.getJSONObject("config");
        JSONObject saveConfigObj = new JSONObject();
        if (configObj != null) {
            JSONArray whiteList = normalizeExtList(configObj.getJSONArray("whiteList"));
            if (!whiteList.isEmpty()) {
                saveConfigObj.put("whiteList", whiteList);
            }
            JSONArray blackList = normalizeExtList(configObj.getJSONArray("blackList"));
            if (!blackList.isEmpty()) {
                saveConfigObj.put("blackList", blackList);
            }
            Long maxSize = configObj.getLong("maxSize");
            if (maxSize != null) {
                if (maxSize < 0) {
                    throw new ParamIrregularException("maxSize", ">=0");
                }
                if (maxSize > 0) {
                    saveConfigObj.put("maxSize", maxSize);
                }
            }
        }

        FileTypeVo fileTypeVo = new FileTypeVo();
        fileTypeVo.setName(activeFileTypeVo.getName());
        fileTypeVo.setConfig(saveConfigObj.toJSONString());
        fileMapper.saveFileTypeConfig(fileTypeVo);
        return null;
    }

    private JSONArray normalizeExtList(JSONArray extList) {
        JSONArray returnList = new JSONArray();
        if (extList == null) {
            return returnList;
        }
        for (int i = 0; i < extList.size(); i++) {
            String ext = extList.getString(i);
            if (ext == null) {
                continue;
            }
            ext = ext.trim().toLowerCase(Locale.ENGLISH);
            if (ext.startsWith(".")) {
                ext = ext.substring(1);
            }
            // UploadFileApi 按扩展名逐项比对，这里提前去空去重，避免保存无效规则项。
            if (!ext.isEmpty() && !returnList.contains(ext)) {
                returnList.add(ext);
            }
        }
        return returnList;
    }
}

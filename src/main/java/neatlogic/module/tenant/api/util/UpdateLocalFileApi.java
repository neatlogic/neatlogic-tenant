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

package neatlogic.module.tenant.api.util;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.ADMIN;
import neatlogic.framework.common.config.Config;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.exception.file.FileStorageMediumHandlerNotFoundException;
import neatlogic.framework.file.core.FileStorageMediumFactory;
import neatlogic.framework.file.core.IFileStorageHandler;
import neatlogic.framework.file.dao.mapper.FileMapper;
import neatlogic.framework.file.dto.FileVo;
import neatlogic.framework.heartbeat.dao.mapper.ServerMapper;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.tenant.service.ServerService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Objects;

@Service
@AuthAction(action = ADMIN.class)
@OperationType(type = OperationTypeEnum.OPERATE)
public class UpdateLocalFileApi extends PrivateApiComponentBase {

    @Resource
    private ServerMapper serverMapper;

    @Resource
    private FileMapper fileMapper;

    @Resource
    private ServerService serverService;

    @Override
    public String getName() {
        return "更新服务器文件";
    }

    @Input({
            @Param(name = "serverId", type = ApiParamType.INTEGER, desc = "服务器ID"),
            @Param(name = "fileId", type = ApiParamType.LONG, isRequired = true, desc = "附件ID"),
            @Param(name = "path", type = ApiParamType.STRING, isRequired = true, desc = "需要更新的文件路径")
    })
    @Description(desc = "更新服务器文件")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        JSONObject resultObj = new JSONObject(new LinkedHashMap<>());
        Integer serverId = paramObj.getInteger("serverId");
        if (serverId == null) {
            serverId = Config.SCHEDULE_SERVER_ID;
        }
        if (Objects.equals(serverId, Config.SCHEDULE_SERVER_ID)) {
            Long fileId = paramObj.getLong("fileId");
            String path = paramObj.getString("path");
            FileVo fileVo = fileMapper.getFileById(fileId);
            String filePath = fileVo.getPath();
            String[] split = filePath.split(":", 2);
            IFileStorageHandler handler = FileStorageMediumFactory.getHandler(split[0].toUpperCase());
            if (handler == null) {
                throw new FileStorageMediumHandlerNotFoundException(split[0]);
            }
            try (InputStream inputStream = handler.getData(filePath)) {
                Path targetPath = Paths.get(path);
                File file = targetPath.toFile();
                resultObj.put("path", file.getPath());
                if (file.exists()) {
                    resultObj.put("文件是否已存在", "是");
                    long length = Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    resultObj.put("操作类型", "覆盖");
                    resultObj.put("文件大小", length);
                } else {
                    if (!file.getParentFile().exists()) {
                        file.getParentFile().mkdirs();
                    }
                    resultObj.put("文件是否已存在", "否");
                    long length = Files.copy(inputStream, targetPath);
                    resultObj.put("操作类型", "新增");
                    resultObj.put("文件大小", length);
                }
            }
            resultObj.put("serverId", serverId);
        } else {
            resultObj = serverService.postOtherServerApi(paramObj, serverId);
        }
        return resultObj;
    }


    @Override
    public String getToken() {
        return "util/localfile/update";
    }

    @Override
    public int needAudit() {
        return 1;
    }

}

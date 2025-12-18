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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.ADMIN;
import neatlogic.framework.common.config.Config;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.heartbeat.dao.mapper.ServerMapper;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.tenant.service.ServerService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Objects;

@Service
@AuthAction(action = ADMIN.class)
@OperationType(type = OperationTypeEnum.OPERATE)
public class SearchLocalFilePathApi extends PrivateApiComponentBase {

    @javax.annotation.Resource
    private ServerMapper serverMapper;

    @javax.annotation.Resource
    ServerService serverService;

    @Override
    public String getName() {
        return "查询服务器文件路径";
    }

    @Input({
            @Param(name = "serverId", type = ApiParamType.INTEGER, desc = "服务器ID"),
            @Param(name = "fileName", type = ApiParamType.STRING, isRequired = true, desc = "文件名"),
            @Param(name = "scanPathList", type = ApiParamType.JSONARRAY, desc = "扫描路径列表", help = "默认只扫描类路径，如果需要扫描其他路径，就在这里添加，但不可以从\"/\"根目录开始扫描")
    })
    @Description(desc = "查询服务器文件路径")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        JSONObject resultObj = new JSONObject(new LinkedHashMap<>());
        Integer serverId = paramObj.getInteger("serverId");
        if (serverId == null) {
            serverId = Config.SCHEDULE_SERVER_ID;
        }
        if (Objects.equals(serverId, Config.SCHEDULE_SERVER_ID)) {
            String fileName = paramObj.getString("fileName");
            JSONArray scanPathList = paramObj.getJSONArray("scanPathList");
            ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            JSONArray resultList = new JSONArray();
            {
                long startTime = System.currentTimeMillis();
                JSONObject jsonObj = new JSONObject(new LinkedHashMap<>());
                JSONArray filePathList = new JSONArray();
                JSONArray otherPathList = new JSONArray();
                String locationPattern = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + "**/" + fileName;
                Resource[] resources = resolver.getResources(locationPattern);
                for (Resource resource : resources) {
                    if (resource.exists()) {
                        if (resource.isFile()) {
                            File file = resource.getFile();
                            JSONObject pathObj = new JSONObject();
                            pathObj.put("path", file.getPath());
                            pathObj.put("canRead", file.canRead());
                            pathObj.put("canWrite", file.canWrite());
                            pathObj.put("isHidden", file.isHidden());
                            pathObj.put("length", file.length());
                            filePathList.add(pathObj);
                        } else {
                            JSONObject pathObj = new JSONObject();
                            pathObj.put("path", resource.getURL().toString());
                            pathObj.put("canRead", resource.isReadable());
                            otherPathList.add(pathObj);
                        }
                    }
                }
                jsonObj.put("locationPattern", locationPattern);
                jsonObj.put("timeCost", (System.currentTimeMillis() - startTime));
                jsonObj.put("filePathList", filePathList);
                if (CollectionUtils.isNotEmpty(otherPathList)) {
                    jsonObj.put("otherPathList", otherPathList);
                }
                resultList.add(jsonObj);
            }
            {
                if (CollectionUtils.isNotEmpty(scanPathList)) {
                    for (int i = 0; i < scanPathList.size(); i++) {
                        String scanPath = scanPathList.getString(i);
                        if (Objects.equals(scanPath, "/")) {
                            String locationPattern = "file:" + "/**/" + fileName;
                            JSONObject jsonObj = new JSONObject(new LinkedHashMap<>());
                            jsonObj.put("locationPattern", locationPattern);
                            jsonObj.put("message", "不可以从根目录开始扫描");
                            resultList.add(jsonObj);
                        } else {
                            long startTime = System.currentTimeMillis();
                            JSONObject jsonObj = new JSONObject(new LinkedHashMap<>());
                            JSONArray filePathList = new JSONArray();
                            JSONArray otherPathList = new JSONArray();
                            String locationPattern = "file:" + scanPath + "/**/" + fileName;
                            Resource[] resources = resolver.getResources(locationPattern);
                            for (Resource resource : resources) {
                                if (resource.exists()) {
                                    if (resource.isFile()) {
                                        File file = resource.getFile();
                                        JSONObject pathObj = new JSONObject();
                                        pathObj.put("path", file.getPath());
                                        pathObj.put("canRead", file.canRead());
                                        pathObj.put("canWrite", file.canWrite());
                                        pathObj.put("isHidden", file.isHidden());
                                        pathObj.put("length", file.length());
                                        filePathList.add(pathObj);
                                    } else {
                                        JSONObject pathObj = new JSONObject();
                                        pathObj.put("path", resource.getURL().toString());
                                        pathObj.put("canRead", resource.isReadable());
                                        otherPathList.add(pathObj);
                                    }
                                }
                            }
                            jsonObj.put("locationPattern", locationPattern);
                            jsonObj.put("timeCost", (System.currentTimeMillis() - startTime));
                            jsonObj.put("filePathList", filePathList);
                            if (CollectionUtils.isNotEmpty(otherPathList)) {
                                jsonObj.put("otherPathList", otherPathList);
                            }
                            resultList.add(jsonObj);
                        }
                    }
                }
            }
            resultObj.put("tbodyList", resultList);
            resultObj.put("serverId", serverId);
        } else {
            resultObj = serverService.postOtherServerApi(paramObj, serverId);
        }
        return resultObj;
    }

    @Override
    public String getToken() {
        return "util/localfile/path/search";
    }
}

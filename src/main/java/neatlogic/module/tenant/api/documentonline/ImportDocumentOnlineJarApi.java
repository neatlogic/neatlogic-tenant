/*
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package neatlogic.module.tenant.api.documentonline;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.DOCUMENTONLINE_CONFIG_MODIFY;
import neatlogic.framework.common.config.Config;
import neatlogic.framework.documentonline.exception.DocumentOnlineJarNameIllegalException;
import neatlogic.framework.documentonline.util.DocumentOnlineManager;
import neatlogic.framework.exception.core.ApiRuntimeException;
import neatlogic.framework.exception.file.FileNotUploadException;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateBinaryStreamApiComponentBase;
import neatlogic.module.tenant.service.ServerService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@AuthAction(action = DOCUMENTONLINE_CONFIG_MODIFY.class)
@OperationType(type = OperationTypeEnum.UPDATE)
public class ImportDocumentOnlineJarApi extends PrivateBinaryStreamApiComponentBase {

    private final String COMMERCIAL_JAR_NAME_PREFIX = "neatlogic-document-online-commercial";

    private final String COMMUNITY_JAR_NAME_PREFIX = "neatlogic-document-online";

    @Resource
    private ServerService serverService;

    @Override
    public String getName() {
        return "nmtad.importdocumentonlinejarapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({})
    @Output({})
    @Description(desc = "nmtad.importdocumentonlinejarapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj, HttpServletRequest request, HttpServletResponse response) throws Exception {
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        // 获取所有导入文件
        Map<String, MultipartFile> multipartFileMap = multipartRequest.getFileMap();
        // 如果没有导入文件，抛出异常
        if (multipartFileMap.isEmpty()) {
            throw new FileNotUploadException();
        }
        String documentOnlineHomeDirPath = Config.DATA_HOME() + DocumentOnlineManager.OUTSIDE_WAR_DOCUMENTS_ONLINE_JARS;
        File documentOnlineHome = new File(documentOnlineHomeDirPath);
        if (!documentOnlineHome.exists()) {
            if (!documentOnlineHome.mkdirs()) {
                throw new ApiRuntimeException("创建文件目录失败: " + documentOnlineHomeDirPath);
            }
        }
        JSONArray resultList = new JSONArray();
        // 遍历导入文件
        for (Map.Entry<String, MultipartFile> entry : multipartFileMap.entrySet()) {
            MultipartFile multipartFile = entry.getValue();
            // neatlogic-document-online-0.4.0.0-SNAPSHOT.jar neatlogic-document-online-commercial-0.4.0.0-SNAPSHOT.jar
            String oldFileName = multipartFile.getOriginalFilename();
            if (StringUtils.isNotBlank(oldFileName)
                    && (oldFileName.startsWith(COMMERCIAL_JAR_NAME_PREFIX) || oldFileName.startsWith(COMMUNITY_JAR_NAME_PREFIX))
                    && oldFileName.endsWith(".jar")) {
                JSONObject jsonObj = new JSONObject();
                File[] listFiles = documentOnlineHome.listFiles();
                if (listFiles != null) {
                    List<String> fileNameList = new ArrayList<>();
                    for (File file : listFiles) {
                        fileNameList.add(file.getName());
                    }
                    jsonObj.put("existsFileNameList", fileNameList);
                    if (oldFileName.startsWith(COMMERCIAL_JAR_NAME_PREFIX)) {
                        for (File file : listFiles) {
                            if (file.getName().startsWith(COMMERCIAL_JAR_NAME_PREFIX)) {
                                boolean delete = file.delete();
                                break;
                            }
                        }
                    } else {
                        for (File file : listFiles) {
                            if (file.getName().startsWith(COMMUNITY_JAR_NAME_PREFIX) && !file.getName().startsWith(COMMERCIAL_JAR_NAME_PREFIX)) {
                                boolean delete = file.delete();
                                break;
                            }
                        }
                    }
                }
                try (InputStream inputStream = multipartFile.getInputStream()) {
                    Path targetPath = Paths.get(documentOnlineHomeDirPath + "/" + oldFileName);
                    File file = targetPath.toFile();
                    jsonObj.put("path", file.getPath());
                    if (file.exists()) {
                        long length = Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
                        jsonObj.put("length", length);
                    } else {
                        long length = Files.copy(inputStream, targetPath);
                        jsonObj.put("length", length);
                    }
                }
                resultList.add(jsonObj);
            } else {
                throw new DocumentOnlineJarNameIllegalException(oldFileName);
            }
        }
        long startTime = System.currentTimeMillis();
        JSONObject resultObj = DocumentOnlineManager.LoadDocumentsOutsideWar();
        resultObj.put("timeCost", (System.currentTimeMillis() - startTime));
        if (MapUtils.isNotEmpty(resultObj)) {
            List<String> messageList = new ArrayList<>();
            JSONArray resultArray = serverService.postOtherServersApi(new JSONObject(), Config.SCHEDULE_SERVER_ID);
            if (CollectionUtils.isNotEmpty(resultArray)) {
                for (int i = 0; i < resultArray.size(); i++) {
                    JSONObject result = resultArray.getJSONObject(i);
                    messageList.add(result.getString("message"));
                }
            }
            resultObj.put("messageList", messageList);
        }
        resultObj.put("importFileList", resultList);
        return resultObj;
    }

    @Override
    public String getToken() {
        return "documentonline/jar/import";
    }

}

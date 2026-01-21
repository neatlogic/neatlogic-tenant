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

package neatlogic.module.tenant.api.importexport;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.NoAuth;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.constvalue.MimeType;
import neatlogic.framework.exception.type.ParamIrregularException;
import neatlogic.framework.importexport.core.ImportExportHandler;
import neatlogic.framework.importexport.core.ImportExportHandlerFactory;
import neatlogic.framework.importexport.dto.ImportExportBaseInfoVo;
import neatlogic.framework.importexport.dto.ImportExportVo;
import neatlogic.framework.importexport.exception.ExportNoAuthException;
import neatlogic.framework.importexport.exception.ImportExportHandlerNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateBinaryStreamApiComponentBase;
import neatlogic.framework.userexportfile.core.ExportFileManager;
import neatlogic.framework.userexportfile.core.IUserExportFileType;
import neatlogic.framework.userexportfile.core.UserExportFileTypeFactory;
import neatlogic.framework.userexportfile.exception.UserExportFileTypeNotFoundException;
import neatlogic.framework.userexportfile.exception.UserExportTimeCostTooLongException;
import neatlogic.framework.util.FileUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.DeferredFileOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
@AuthAction(action = NoAuth.class)
@OperationType(type = OperationTypeEnum.UPDATE)
public class ExportApi extends PrivateBinaryStreamApiComponentBase {

    private Logger logger = LoggerFactory.getLogger(ExportApi.class);

    @Override
    public String getName() {
        return "nmtai.exportapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "primaryKey", type = ApiParamType.NOAUTH, isRequired = true, desc = "common.primarykey"),
            @Param(name = "type", type = ApiParamType.STRING, isRequired = true, desc = "common.type")
    })
    @Output({})
    @Description(desc = "nmtai.exportapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj, HttpServletRequest request, HttpServletResponse response) throws Exception {
        Object primaryKeyObj = paramObj.get("primaryKey");
        String type = paramObj.getString("type");
        ImportExportHandler importExportHandler = ImportExportHandlerFactory.getHandler(type);
        if (importExportHandler == null) {
            throw new ImportExportHandlerNotFoundException(type);
        }
        IUserExportFileType userExportFileType = UserExportFileTypeFactory.getUserExportFileType(type);
        if (userExportFileType == null) {
            throw new UserExportFileTypeNotFoundException(type);
        }
        if (primaryKeyObj instanceof String) {
            String str = (String) primaryKeyObj;
            if (StringUtils.length(str) != 32) {
                try {
                    primaryKeyObj = Long.valueOf(str);
                } catch (NumberFormatException e) {
                    throw new ParamIrregularException("primaryKey");
                }
            }
        }
        final Object primaryKey = primaryKeyObj;
        if (!importExportHandler.checkExportAuth(primaryKey)) {
            throw new ExportNoAuthException();
        }

        String name = null;
        // 先检查导出对象及依赖对象有没有找不到数据，如果有就抛异常
        {
            List<ImportExportBaseInfoVo> dependencyBaseInfoList = new ArrayList<>();
            dependencyBaseInfoList.add(new ImportExportBaseInfoVo(type, primaryKey));
            ImportExportVo importExportVo = importExportHandler.exportData(primaryKey, dependencyBaseInfoList, null);
            name = importExportHandler.getType().getText() + "-" + importExportVo.getName() + "(" + importExportVo.getPrimaryKey() + ").pak";
        }
        ExportFileManager exportFileManager = new ExportFileManager(userExportFileType)
                .withName(name)
                .withMimeType(MimeType.XLS)
//                .withUniqueKey(RequestContext.get().getUrl())
                ;
        exportFileManager.generateData((outputStream) -> {
        // 上面代码检查没有异常再进行导出压缩到文件
        List<ImportExportBaseInfoVo> dependencyBaseInfoList = new ArrayList<>();
        try (ZipOutputStream zipos = new ZipOutputStream(outputStream)) {
            dependencyBaseInfoList.add(new ImportExportBaseInfoVo(type, primaryKey));
            ImportExportVo importExportVo = importExportHandler.exportData(primaryKey, dependencyBaseInfoList, zipos);
            dependencyBaseInfoList.remove(0);
            importExportVo.setDependencyBaseInfoList(dependencyBaseInfoList);
            zipos.putNextEntry(new ZipEntry(importExportVo.getPrimaryKey() + ".json"));
            zipos.write(JSONObject.toJSONBytes(importExportVo));
            zipos.closeEntry();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw e;
        }
        });
        try (DeferredFileOutputStream deferredFileOutputStream = exportFileManager.export(5, TimeUnit.SECONDS)) {
            if (deferredFileOutputStream != null) {
                try (OutputStream os = response.getOutputStream()) {
                    response.setContentType(exportFileManager.getMimeType().getValue());
                    String filename = FileUtil.getEncodedFileName(exportFileManager.getName());
                    response.setHeader("Content-Disposition", " attachment; filename=\"" + filename + "\"");
                    if (deferredFileOutputStream.isInMemory()) {
                        try (InputStream inputStream = new ByteArrayInputStream(deferredFileOutputStream.getData())) {
                            IOUtils.copyLarge(inputStream, os);
                        }
                    } else {
                        try (InputStream inputStream = new BufferedInputStream(new FileInputStream(deferredFileOutputStream.getFile()))) {
                            IOUtils.copyLarge(inputStream, os);
                        }
                    }
                } catch (Exception e) {
                    logger.warn(e.getMessage(), e);
                } finally {
                    File tempFile = deferredFileOutputStream.getFile();
                    if (tempFile.exists()) {
                        boolean delete = tempFile.delete();
                    }
                }
            } else {
                throw new UserExportTimeCostTooLongException();
            }
        }
        return null;
    }

    @Override
    public String getToken() {
        return "common/export";
    }
}

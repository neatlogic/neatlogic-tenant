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

package neatlogic.module.tenant.api.matrix;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.NoAuth;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.constvalue.ExportFileType;
import neatlogic.framework.common.constvalue.MimeType;
import neatlogic.framework.common.constvalue.ResponseCode;
import neatlogic.framework.matrix.core.IMatrixDataSourceHandler;
import neatlogic.framework.matrix.core.MatrixDataSourceHandlerFactory;
import neatlogic.framework.matrix.dao.mapper.MatrixMapper;
import neatlogic.framework.matrix.dto.MatrixVo;
import neatlogic.framework.matrix.exception.MatrixDataSourceHandlerNotFoundException;
import neatlogic.framework.matrix.exception.MatrixNotFoundException;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateBinaryStreamApiComponentBase;
import neatlogic.framework.userexportfile.constvalue.FrameworkUserExportFileType;
import neatlogic.framework.userexportfile.core.ExportFileManager;
import neatlogic.framework.util.FileUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.DeferredFileOutputStream;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.concurrent.TimeUnit;

/**
 * @program: neatlogic
 * @description:
 * @create: 2020-03-26 19:04
 **/
@Service
@AuthAction(action = NoAuth.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class MatrixExportApi extends PrivateBinaryStreamApiComponentBase {

    private final Logger logger = LoggerFactory.getLogger(MatrixExportApi.class);
    @Resource
    private MatrixMapper matrixMapper;

    @Override
    public String getToken() {
        return "matrix/data/export";
    }

    @Override
    public String getName() {
        return "nmtam.matrixexportapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @SuppressWarnings({"unchecked"})
    @Input({
            @Param(name = "matrixUuid", desc = "term.framework.matrixuuid", type = ApiParamType.STRING, isRequired = true),
            @Param(name = "fileType", desc = "common.filetype", type = ApiParamType.ENUM, rule = "excel,csv", isRequired = true)
    })
    @Description(desc = "nmtam.matrixexportapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj, HttpServletRequest request, HttpServletResponse response) throws Exception {
        String matrixUuid = paramObj.getString("matrixUuid");
        String fileType = paramObj.getString("fileType");
        MatrixVo matrixVo = matrixMapper.getMatrixByUuid(matrixUuid);
        if (matrixVo == null) {
            throw new MatrixNotFoundException(matrixUuid);
        }
        IMatrixDataSourceHandler matrixDataSourceHandler = MatrixDataSourceHandlerFactory.getHandler(matrixVo.getType());
        if (matrixDataSourceHandler == null) {
            throw new MatrixDataSourceHandlerNotFoundException(matrixVo.getType());
        }

        ExportFileManager exportFileManager = new ExportFileManager(FrameworkUserExportFileType.MATRIX_DATA);
        if (ExportFileType.CSV.getValue().equals(fileType)) {
            exportFileManager.withName(matrixVo.getName() + ".csv")
                    .withMimeType(MimeType.STREAM);
            exportFileManager.generateData((outputStream) -> {
            matrixDataSourceHandler.exportMatrix2CSV(matrixVo, outputStream);
            });
        } else if (ExportFileType.EXCEL.getValue().equals(fileType)) {
            exportFileManager.withName(matrixVo.getName() + ".xlsx")
                    .withMimeType(MimeType.XLS);
            exportFileManager.generateData((outputStream) -> {
            Workbook workbook = matrixDataSourceHandler.exportMatrix2Excel(matrixVo);
            if (workbook == null) {
                workbook = new HSSFWorkbook();
            }
                workbook.write(outputStream);
            });
        }
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
                response.setStatus(ResponseCode.EXPORT_TIMEOUT.getCode());
            }
        }
        return null;
    }

}

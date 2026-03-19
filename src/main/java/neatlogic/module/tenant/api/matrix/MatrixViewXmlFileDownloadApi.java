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
import neatlogic.framework.matrix.dao.mapper.MatrixMapper;
import neatlogic.framework.matrix.dto.MatrixViewVo;
import neatlogic.framework.matrix.dto.MatrixVo;
import neatlogic.framework.matrix.exception.MatrixNotFoundException;
import neatlogic.framework.matrix.exception.MatrixViewNotFoundException;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.binarystream.PrivateBinaryStreamApiComponentBase;
import neatlogic.framework.util.FileUtil;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Service
@AuthAction(action = NoAuth.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class MatrixViewXmlFileDownloadApi extends PrivateBinaryStreamApiComponentBase {

    @Resource
    private MatrixMapper matrixMapper;

    @Override
    public String getToken() {
        return "matrix/view/xmlfile/download";
    }

    @Override
    public String getName() {
        return "下载视图矩阵xml配置文件";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "matrixUuid", type = ApiParamType.STRING, isRequired = true, desc = "矩阵uuid")
    })
    @Description(desc = "下载视图矩阵xml配置文件")
    @Override
    public Object myDoService(JSONObject paramObj, HttpServletRequest request, HttpServletResponse response) throws Exception {
        String matrixUuid = paramObj.getString("matrixUuid");
        MatrixVo matrixVo = matrixMapper.getMatrixByUuid(matrixUuid);
        if (matrixVo == null) {
            throw new MatrixNotFoundException(matrixUuid);
        }
        MatrixViewVo matrixViewVo = matrixMapper.getMatrixViewByMatrixUuid(matrixUuid);
        if (matrixViewVo == null) {
            throw new MatrixViewNotFoundException(matrixVo.getName());
        }
        response.setContentType("text/xml");
        response.setHeader("Content-Disposition", " attachment; filename=\"" + FileUtil.getEncodedFileName(matrixViewVo.getFileName()) + "\"");
        try (InputStream in = IOUtils.toInputStream(matrixViewVo.getXml(), StandardCharsets.UTF_8);
             ServletOutputStream os = response.getOutputStream()) {
            IOUtils.copyLarge(in, os);
            os.flush();
        }
        return null;
    }
}

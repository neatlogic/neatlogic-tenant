/*
 * Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package neatlogic.module.tenant.api.matrix;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.constvalue.ExportFileType;
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
import neatlogic.framework.util.FileUtil;
import com.alibaba.fastjson.JSONObject;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;

/**
 * @program: neatlogic
 * @description:
 * @create: 2020-03-26 19:04
 **/
@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class MatrixExportApi extends PrivateBinaryStreamApiComponentBase {

    @Resource
    private MatrixMapper matrixMapper;

    @Override
    public String getToken() {
        return "matrix/export";
    }

    @Override
    public String getName() {
        return "矩阵导出接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @SuppressWarnings({"unchecked"})
    @Input({
            @Param(name = "matrixUuid", desc = "矩阵uuid", type = ApiParamType.STRING, isRequired = true),
            @Param(name = "fileType", desc = "文件类型", type = ApiParamType.ENUM, rule = "excel,csv", isRequired = true)
    })
    @Description(desc = "矩阵导出接口")
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
        OutputStream os = response.getOutputStream();
        if (ExportFileType.CSV.getValue().equals(fileType)) {
            String fileName = FileUtil.getEncodedFileName(matrixVo.getName() + ".csv");
            response.setContentType("application/text;charset=GBK");
            response.setHeader("Content-Disposition", " attachment; filename=\"" + fileName + "\"");
            matrixDataSourceHandler.exportMatrix2CSV(matrixVo, os);
            os.flush();
        } else if (ExportFileType.EXCEL.getValue().equals(fileType)) {
            Workbook workbook = matrixDataSourceHandler.exportMatrix2Excel(matrixVo);
            if (workbook == null) {
                workbook = new HSSFWorkbook();
            }
            String fileName = FileUtil.getEncodedFileName(matrixVo.getName() + ".xls");
            response.setContentType("application/vnd.ms-excel;charset=utf-8");
            response.setHeader("Content-Disposition", " attachment; filename=\"" + fileName + "\"");
            workbook.write(os);
            os.flush();
        }
        return null;
    }

}

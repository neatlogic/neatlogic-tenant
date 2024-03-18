/*Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

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

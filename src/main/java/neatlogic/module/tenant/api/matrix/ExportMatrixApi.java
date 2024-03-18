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

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.common.constvalue.ApiParamType;
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
import neatlogic.framework.util.$;
import neatlogic.framework.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
@OperationType(type = OperationTypeEnum.SEARCH)
public class ExportMatrixApi extends PrivateBinaryStreamApiComponentBase {

    private final static Logger logger = LoggerFactory.getLogger(ExportMatrixApi.class);

    @Resource
    private MatrixMapper matrixMapper;

    @Override
    public String getName() {
        return "nmtam.exportmatrixapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "uuid", type = ApiParamType.STRING, isRequired = true, desc = "common.uuid")
    })
    @Description(desc = "导出矩阵")
    @Override
    public Object myDoService(JSONObject paramObj, HttpServletRequest request, HttpServletResponse response) throws Exception {
        String uuid = paramObj.getString("uuid");
        MatrixVo matrixVo = matrixMapper.getMatrixByUuid(uuid);
        if (matrixVo == null) {
            throw new MatrixNotFoundException(uuid);
        }
        IMatrixDataSourceHandler matrixDataSourceHandler = MatrixDataSourceHandlerFactory.getHandler(matrixVo.getType());
        if (matrixDataSourceHandler == null) {
            throw new MatrixDataSourceHandlerNotFoundException(matrixVo.getType());
        }
        MatrixVo matrixDefinition = matrixDataSourceHandler.exportMatrix(matrixVo);
        String fileName = FileUtil.getEncodedFileName($.t("common.matrix") + "_" + matrixDefinition.getName() + ".pak");
        response.setContentType("application/octet-stream;charset=utf-8");
        response.setHeader("Content-Disposition", " attachment; filename=\"" + fileName + "\"");

        try (ZipOutputStream zipos = new ZipOutputStream(response.getOutputStream())) {
            zipos.putNextEntry(new ZipEntry(matrixDefinition.getName() + ".json"));
            zipos.write(JSONObject.toJSONBytes(matrixDefinition));
            zipos.closeEntry();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public String getToken() {
        return "matrix/export";
    }
}

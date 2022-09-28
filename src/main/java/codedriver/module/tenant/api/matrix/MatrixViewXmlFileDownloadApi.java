/*
 * Copyright(c) 2021 TechSureCo.,Ltd.AllRightsReserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.matrix;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.crossover.CrossoverServiceFactory;
import codedriver.framework.crossover.IFileCrossoverService;
import codedriver.framework.matrix.dao.mapper.MatrixMapper;
import codedriver.framework.matrix.dto.MatrixViewVo;
import codedriver.framework.matrix.dto.MatrixVo;
import codedriver.framework.matrix.exception.MatrixNotFoundException;
import codedriver.framework.matrix.exception.MatrixViewNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateBinaryStreamApiComponentBase;
import codedriver.framework.util.FileUtil;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Service
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
        response.setHeader("Content-Disposition", " attachment; filename=\"" + FileUtil.getEncodedFileName(request.getHeader("User-Agent"), matrixViewVo.getFileName()) + "\"");
        try (InputStream in = IOUtils.toInputStream(matrixViewVo.getXml(), StandardCharsets.UTF_8);
             ServletOutputStream os = response.getOutputStream()) {
            IOUtils.copyLarge(in, os);
            os.flush();
        }
        return null;
    }
}

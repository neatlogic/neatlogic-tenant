package codedriver.module.tenant.api.matrix;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.util.FileUtil;
import codedriver.framework.exception.file.FileNotFoundException;
import codedriver.framework.file.dao.mapper.FileMapper;
import codedriver.framework.file.dto.FileVo;
import codedriver.framework.matrix.constvalue.MatrixType;
import codedriver.framework.matrix.dao.mapper.MatrixMapper;
import codedriver.framework.matrix.dao.mapper.MatrixViewMapper;
import codedriver.framework.matrix.dto.MatrixAttributeVo;
import codedriver.framework.matrix.dto.MatrixViewVo;
import codedriver.framework.matrix.dto.MatrixVo;
import codedriver.framework.matrix.exception.*;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.tenant.auth.label.MATRIX_MODIFY;
import codedriver.module.tenant.service.matrix.MatrixService;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@Transactional
@AuthAction(action = MATRIX_MODIFY.class)
@OperationType(type = OperationTypeEnum.CREATE)
public class MatrixViewSaveApi extends PrivateApiComponentBase {

    @Resource
    private MatrixViewMapper viewMapper;

    @Resource
    private MatrixMapper matrixMapper;

    @Resource
    private FileMapper fileMapper;

    @Resource
    private MatrixService matrixService;

    @Override
    public String getToken() {
        return "matrix/view/save";
    }

    @Override
    public String getName() {
        return "视图矩阵数据源保存接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "matrixUuid", type = ApiParamType.STRING, isRequired = true, desc = "矩阵uuid"),
            @Param(name = "fileId", type = ApiParamType.STRING, isRequired = true, desc = "配置文件id")
    })
    @Description(desc = "视图矩阵数据源保存接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        MatrixViewVo matrixViewVo = JSONObject.toJavaObject(jsonObj, MatrixViewVo.class);
        MatrixVo matrixVo = matrixMapper.getMatrixByUuid(matrixViewVo.getMatrixUuid());
        if (matrixVo == null) {
            throw new MatrixNotFoundException(matrixViewVo.getMatrixUuid());
        }

        if (!MatrixType.VIEW.getValue().equals(matrixVo.getType())) {
            throw new MatrixViewNotFoundException(matrixVo.getName());
        }
        FileVo fileVo = fileMapper.getFileById(matrixViewVo.getFileId());
        if (fileVo == null) {
            throw new FileNotFoundException(matrixViewVo.getFileId());
        }
        String xml = IOUtils.toString(FileUtil.getData(fileVo.getPath()), StandardCharsets.UTF_8);
        if (StringUtils.isBlank(xml)) {
            throw new MatrixViewSettingFileNotFoundException();
        }
        List<MatrixAttributeVo> matrixAttributeVoList = matrixService.buildView(matrixVo.getUuid(), matrixVo.getName(), xml);
        JSONObject config = new JSONObject();
        config.put("attributeList", matrixAttributeVoList);
        matrixViewVo.setConfig(config.toJSONString());
        viewMapper.replaceMatrixView(matrixViewVo);
        return null;
    }

}

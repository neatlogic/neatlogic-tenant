/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.matrix;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.matrix.core.IMatrixDataSourceHandler;
import codedriver.framework.matrix.core.MatrixDataSourceHandlerFactory;
import codedriver.framework.matrix.dao.mapper.MatrixMapper;
import codedriver.framework.matrix.dto.MatrixViewVo;
import codedriver.framework.matrix.dto.MatrixVo;
import codedriver.framework.matrix.exception.*;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.auth.label.MATRIX_MODIFY;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Deprecated
@Service
@Transactional
@AuthAction(action = MATRIX_MODIFY.class)
@OperationType(type = OperationTypeEnum.CREATE)
public class MatrixViewSaveApi extends PrivateApiComponentBase {

    @Resource
    private MatrixMapper matrixMapper;

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
        IMatrixDataSourceHandler matrixDataSourceHandler = MatrixDataSourceHandlerFactory.getHandler(matrixVo.getType());
        if (matrixDataSourceHandler == null) {
            throw new MatrixDataSourceHandlerNotFoundException(matrixVo.getType());
        }
        matrixVo.setFileId(matrixViewVo.getFileId());
        matrixDataSourceHandler.saveMatrix(matrixVo);
//        if (!MatrixType.VIEW.getValue().equals(matrixVo.getType())) {
//            throw new MatrixViewNotFoundException(matrixVo.getName());
//        }
//        FileVo fileVo = fileMapper.getFileById(matrixViewVo.getFileId());
//        if (fileVo == null) {
//            throw new FileNotFoundException(matrixViewVo.getFileId());
//        }
//        String xml = IOUtils.toString(FileUtil.getData(fileVo.getPath()), StandardCharsets.UTF_8);
//        if (StringUtils.isBlank(xml)) {
//            throw new MatrixViewSettingFileNotFoundException();
//        }
//        List<MatrixAttributeVo> matrixAttributeVoList = matrixService.buildView(matrixVo.getUuid(), matrixVo.getName(), xml);
//        JSONObject config = new JSONObject();
//        config.put("attributeList", matrixAttributeVoList);
//        matrixViewVo.setConfig(config.toJSONString());
//        viewMapper.replaceMatrixView(matrixViewVo);
        return null;
    }

}

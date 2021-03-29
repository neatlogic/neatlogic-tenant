package codedriver.module.tenant.api.matrix;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dto.FieldValidResultVo;
import codedriver.framework.integration.core.IIntegrationHandler;
import codedriver.framework.integration.core.IntegrationHandlerFactory;
import codedriver.framework.integration.dao.mapper.IntegrationMapper;
import codedriver.framework.integration.dto.IntegrationResultVo;
import codedriver.framework.integration.dto.IntegrationVo;
import codedriver.framework.matrix.constvalue.MatrixType;
import codedriver.framework.matrix.dao.mapper.MatrixExternalMapper;
import codedriver.framework.matrix.dao.mapper.MatrixMapper;
import codedriver.framework.matrix.dto.MatrixExternalVo;
import codedriver.framework.matrix.dto.MatrixVo;
import codedriver.framework.matrix.exception.*;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.IValid;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.tenant.auth.label.MATRIX_MODIFY;
import codedriver.module.tenant.integration.handler.FrameworkRequestFrom;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

@Service
@Transactional
@AuthAction(action = MATRIX_MODIFY.class)
@OperationType(type = OperationTypeEnum.CREATE)
public class MatrixExternalSaveApi extends PrivateApiComponentBase {

    @Resource
    private MatrixExternalMapper externalMapper;

    @Resource
    private MatrixMapper matrixMapper;

    @Resource
    private IntegrationMapper integrationMapper;

    @Override
    public String getToken() {
        return "matrix/external/save";
    }

    @Override
    public String getName() {
        return "外部数据源矩阵保存接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "matrixUuid", type = ApiParamType.STRING, isRequired = true, desc = "矩阵uuid"),
            @Param(name = "integrationUuid", type = ApiParamType.STRING, isRequired = true, desc = "集成设置uuid")
    })
    @Description(desc = "外部数据源矩阵保存接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        MatrixExternalVo externalVo = JSON.toJavaObject(jsonObj, MatrixExternalVo.class);
        MatrixVo matrixVo = matrixMapper.getMatrixByUuid(externalVo.getMatrixUuid());
        if (matrixVo == null) {
            throw new MatrixNotFoundException(externalVo.getMatrixUuid());
        }

        if (MatrixType.EXTERNAL.getValue().equals(matrixVo.getType())) {
            if (externalMapper.getMatrixExternalIsExists(externalVo.getMatrixUuid()) == 0) {
                externalMapper.insertMatrixExternal(externalVo);
            } else {
                externalMapper.updateMatrixExternal(externalVo);
            }
        } else {
            throw new MatrixExternalException("矩阵:'" + externalVo.getMatrixUuid() + "'不是外部数据源类型");
        }
        return null;
    }

    public IValid integrationUuid(){
        return value -> {
            IntegrationVo integrationVo = integrationMapper.getIntegrationByUuid(value.getString("integrationUuid"));
            IIntegrationHandler handler = IntegrationHandlerFactory.getHandler(integrationVo.getHandler());
            if (handler == null) {
                return new FieldValidResultVo(new MatrixExternalIntegrationHandlerNotFoundException(integrationVo.getHandler()));
            }
            IntegrationResultVo resultVo = handler.sendRequest(integrationVo, FrameworkRequestFrom.TEST);
            if (StringUtils.isNotBlank(resultVo.getError())) {
                return new FieldValidResultVo(new MatrixExternalAccessException());
            } else if (StringUtils.isNotBlank(resultVo.getTransformedResult())) {
                JSONObject transformedResult = null;
                try {
                    transformedResult = JSONObject.parseObject(resultVo.getTransformedResult());
                }catch (Exception ex){
                    return new FieldValidResultVo(new MatrixExternalDataIsNotJsonException());
                }
                if (MapUtils.isNotEmpty(transformedResult)) {
                    Set<String> keys = transformedResult.keySet();
                    Set<String> keySet = new HashSet<>();
                    handler.getOutputPattern().stream().forEach(o -> keySet.add(o.getName()));
                    if(!CollectionUtils.containsAll(keys,keySet)){
                        return new FieldValidResultVo(new MatrixExternalDataNotFormattedException(JSON.toJSONString(CollectionUtils.removeAll(keySet, keys))));
                    }
                    JSONArray theadList = transformedResult.getJSONArray("theadList");
                    if(CollectionUtils.isNotEmpty(theadList)){
                        for(int i = 0; i < theadList.size();i++){
                            if(!theadList.getJSONObject(i).containsKey("key") || !theadList.getJSONObject(i).containsKey("title")){
                                return new FieldValidResultVo(new MatrixExternalDataNotFormattedException("key或title"));
                            }
                        }
                    }else{
                        return new FieldValidResultVo(new MatrixExternalDataNotFormattedException("theadList"));
                    }
                }
            }
            return new FieldValidResultVo();
        };
    }
}

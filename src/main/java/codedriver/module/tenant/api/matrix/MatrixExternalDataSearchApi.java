package codedriver.module.tenant.api.matrix;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.exception.integration.IntegrationHandlerNotFoundException;
import codedriver.framework.integration.core.IIntegrationHandler;
import codedriver.framework.integration.core.IntegrationHandlerFactory;
import codedriver.framework.integration.core.RequestFrom;
import codedriver.framework.integration.dao.mapper.IntegrationMapper;
import codedriver.framework.integration.dto.IntegrationResultVo;
import codedriver.framework.integration.dto.IntegrationVo;
import codedriver.framework.matrix.dao.mapper.MatrixExternalMapper;
import codedriver.framework.matrix.dao.mapper.MatrixMapper;
import codedriver.framework.matrix.dto.MatrixColumnVo;
import codedriver.framework.matrix.dto.MatrixExternalVo;
import codedriver.framework.matrix.dto.MatrixVo;
import codedriver.framework.matrix.exception.MatrixExternalException;
import codedriver.framework.matrix.exception.MatrixNotFoundException;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.tenant.service.matrix.MatrixService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.Map.Entry;

@Service
@Deprecated
@OperationType(type = OperationTypeEnum.SEARCH)
public class MatrixExternalDataSearchApi extends PrivateApiComponentBase {

    private final static Logger logger = LoggerFactory.getLogger(MatrixExternalDataSearchApi.class);

    @Resource
    private MatrixService matrixService;
    @Resource
    private IntegrationMapper integrationMapper;

    @Resource
    private MatrixMapper matrixMapper;

    @Resource
    private MatrixExternalMapper externalMapper;

    @Override
    public String getToken() {
        return "matrix/external/data/search";
    }

    @Override
    public String getName() {
        return "外部数据源数据检索接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "matrixUuid", desc = "矩阵uuid", type = ApiParamType.STRING, isRequired = true),
            @Param(name = "pageSize", desc = "显示条目数", type = ApiParamType.INTEGER),
            @Param(name = "currentPage", desc = "当前页", type = ApiParamType.INTEGER)
    })
    @Output({
            @Param(name = "tbodyList", desc = "矩阵数据集合"),
            @Param(name = "theadList", desc = "矩阵属性集合"),
            @Param(explode = BasePageVo.class)
    })
    @Description(desc = "矩阵数据检索接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String matrixUuid = jsonObj.getString("matrixUuid");
        List<MatrixColumnVo> sourceColumnList = new ArrayList<>();
        jsonObj.put("sourceColumnList", sourceColumnList); //防止集成管理 js length 异常
        MatrixVo matrixVo = matrixMapper.getMatrixByUuid(matrixUuid);
        if (matrixVo == null) {
            throw new MatrixNotFoundException(matrixUuid);
        }
        MatrixExternalVo externalVo = externalMapper.getMatrixExternalByMatrixUuid(matrixUuid);
        JSONObject returnObj = new JSONObject();
        if (externalVo != null) {
            IntegrationVo integrationVo = integrationMapper.getIntegrationByUuid(externalVo.getIntegrationUuid());
            IIntegrationHandler handler = IntegrationHandlerFactory.getHandler(integrationVo.getHandler());
            if (handler == null) {
                throw new IntegrationHandlerNotFoundException(integrationVo.getHandler());
            }

            integrationVo.getParamObj().putAll(jsonObj);
            IntegrationResultVo resultVo = handler.sendRequest(integrationVo, RequestFrom.MATRIX);
            if (StringUtils.isNotBlank(resultVo.getError())) {
                logger.error(resultVo.getError());
                throw new MatrixExternalException("外部接口访问异常");
            } else if (StringUtils.isNotBlank(resultVo.getTransformedResult())) {
                JSONObject transformedResult = null;
                try {
                    transformedResult = JSONObject.parseObject(resultVo.getTransformedResult());
                }catch (Exception ex){
                    throw new MatrixExternalException("外部接口返回结果不是JSON格式");
                }
                if (MapUtils.isNotEmpty(transformedResult)) {
                    Set<String> keys = transformedResult.keySet();
                    Set<String> keySet = new HashSet<>();
                    handler.getOutputPattern().stream().forEach(o -> keySet.add(o.getName()));
                    if(!CollectionUtils.containsAll(keys,keySet)){
                        throw new MatrixExternalException("外部接口返回结果不符合格式，缺少" + JSON.toJSONString(CollectionUtils.removeAll(keySet, keys)));
                    }
                    JSONArray theadList = transformedResult.getJSONArray("theadList");
                    if(CollectionUtils.isNotEmpty(theadList)){
                        for(int i = 0; i < theadList.size();i++){
                            if(!theadList.getJSONObject(i).containsKey("key") || !theadList.getJSONObject(i).containsKey("title")){
                                throw new MatrixExternalException("外部接口返回结果不符合格式,theadList缺少key或title");
                            }
                        }
                    }else{
                        throw new MatrixExternalException("外部接口返回结果不符合格式,缺少theadList");
                    }
                    returnObj.putAll(transformedResult);
                    JSONArray tbodyArray = transformedResult.getJSONArray("tbodyList");
                    if (CollectionUtils.isNotEmpty(tbodyArray)) {
                        List<Map<String, Object>> tbodyList = new ArrayList<>();
                        for (int i = 0; i < tbodyArray.size(); i++) {
                            JSONObject rowData = tbodyArray.getJSONObject(i);
                            Integer pageSize = jsonObj.getInteger("pageSize");
                            pageSize = pageSize == null ? 10 : pageSize;
                            if (MapUtils.isNotEmpty(rowData)) {
                                Map<String, Object> rowDataMap = new HashMap<>();
                                for (Entry<String, Object> entry : rowData.entrySet()) {
                                    rowDataMap.put(entry.getKey(), matrixService.matrixAttributeValueHandle(entry.getValue()));
                                }
                                tbodyList.add(rowDataMap);
                                if (tbodyList.size() >= pageSize) {
                                    break;
                                }
                            }
                        }
                        returnObj.put("tbodyList", tbodyList);
                    }
                }
            } else if(StringUtils.isBlank(resultVo.getTransformedResult())){
                throw new MatrixExternalException("外部接口无返回结果");
            }
        }else{
            throw new MatrixExternalException("没有配置外部数据源");
        }

        //TODO 暂时屏蔽引用，没考虑好怎么实现
        //List<ProcessMatrixDispatcherVo> dispatcherVoList = matrixMapper.getMatrixDispatcherByMatrixUuid(dataVo.getMatrixUuid());
        returnObj.put("dispatcherVoList", CollectionUtils.EMPTY_COLLECTION);//dispatcherVoList
        //List<ProcessMatrixFormComponentVo> componentVoList = matrixMapper.getMatrixFormComponentByMatrixUuid(dataVo.getMatrixUuid());
        returnObj.put("componentVoList", CollectionUtils.EMPTY_COLLECTION);//componentVoList
        returnObj.put("usedCount", 0);//dispatcherVoList.size() + componentVoList.size()
        return returnObj;
    }

}

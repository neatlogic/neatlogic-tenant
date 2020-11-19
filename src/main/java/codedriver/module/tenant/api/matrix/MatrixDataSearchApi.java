package codedriver.module.tenant.api.matrix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.exception.integration.IntegrationHandlerNotFoundException;
import codedriver.framework.integration.core.IIntegrationHandler;
import codedriver.framework.integration.core.IntegrationHandlerFactory;
import codedriver.framework.integration.core.RequestFrom;
import codedriver.framework.integration.dao.mapper.IntegrationMapper;
import codedriver.framework.integration.dto.IntegrationResultVo;
import codedriver.framework.integration.dto.IntegrationVo;
import codedriver.framework.matrix.constvalue.MatrixType;
import codedriver.framework.matrix.dao.mapper.MatrixAttributeMapper;
import codedriver.framework.matrix.dao.mapper.MatrixDataMapper;
import codedriver.framework.matrix.dao.mapper.MatrixExternalMapper;
import codedriver.framework.matrix.dao.mapper.MatrixMapper;
import codedriver.framework.matrix.dto.MatrixAttributeVo;
import codedriver.framework.matrix.dto.MatrixDataVo;
import codedriver.framework.matrix.dto.MatrixExternalVo;
import codedriver.framework.matrix.dto.MatrixVo;
import codedriver.framework.matrix.exception.MatrixExternalException;
import codedriver.framework.matrix.exception.MatrixNotFoundException;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.tenant.service.matrix.MatrixService;

/**
 * @program: codedriver
 * @description:
 * @create: 2020-03-30 16:34
 **/
@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class MatrixDataSearchApi extends PrivateApiComponentBase {

	private final static Logger logger = LoggerFactory.getLogger(MatrixDataSearchApi.class);
			
    @Autowired
    private MatrixService matrixService;

    @Autowired
    private MatrixMapper matrixMapper;

    @Autowired
    private MatrixAttributeMapper attributeMapper;

    @Autowired
    private MatrixDataMapper matrixDataMapper;
    
	@Autowired
	private IntegrationMapper integrationMapper;

    @Autowired
    private MatrixExternalMapper externalMapper;
    
    @Override
    public String getToken() {
        return "matrix/data/search";
    }

    @Override
    public String getName() {
        return "矩阵数据检索接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({ @Param( name = "keyword", desc = "关键字", type = ApiParamType.STRING),
             @Param( name = "matrixUuid", desc = "矩阵uuid", type = ApiParamType.STRING, isRequired = true),
             @Param( name = "needPage", desc = "是否分页", type = ApiParamType.BOOLEAN),
             @Param( name = "pageSize", desc = "显示条目数", type = ApiParamType.INTEGER),
             @Param( name = "currentPage", desc = "当前页", type = ApiParamType.INTEGER)
    })
    @Output({ @Param( name = "tbodyList", desc = "矩阵数据集合"),
              @Param( name = "theadList", desc = "矩阵属性集合"),
              @Param( explode = BasePageVo.class)})
    @Description( desc = "矩阵数据检索接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject returnObj = new JSONObject();
        MatrixDataVo dataVo = JSON.toJavaObject(jsonObj, MatrixDataVo.class);
        MatrixVo matrixVo = matrixMapper.getMatrixByUuid(dataVo.getMatrixUuid());
    	if(matrixVo == null) {
    		throw new MatrixNotFoundException(dataVo.getMatrixUuid());
    	}
    	List<Map<String, Object>> tbodyList = new ArrayList<>();
    	if(MatrixType.CUSTOM.getValue().equals(matrixVo.getType())) {
    		List<MatrixAttributeVo> attributeVoList = attributeMapper.getMatrixAttributeByMatrixUuid(dataVo.getMatrixUuid());
            if (CollectionUtils.isNotEmpty(attributeVoList)){
            	List<String> columnList = new ArrayList<>();
            	JSONArray headList = new JSONArray();
                JSONObject selectionObj = new JSONObject();
                selectionObj.put("key", "selection");
                selectionObj.put("width", 60);
                headList.add(selectionObj);
                for (MatrixAttributeVo attributeVo : attributeVoList){
                    columnList.add(attributeVo.getUuid());
                    JSONObject columnObj = new JSONObject();
                	columnObj.put("title", attributeVo.getName());
                	columnObj.put("key", attributeVo.getUuid());
                    headList.add(columnObj);
                }
                JSONObject actionObj = new JSONObject();
                actionObj.put("title", "");
                actionObj.put("key", "action");
                actionObj.put("align", "right");
                actionObj.put("width", 10);
                headList.add(actionObj);
                
                returnObj.put("theadList", headList);
                
                dataVo.setColumnList(columnList);
                if (dataVo.getNeedPage()){
                    int rowNum = matrixDataMapper.getDynamicTableDataCount(dataVo);
                    returnObj.put("pageCount", PageUtil.getPageCount(rowNum, dataVo.getPageSize()));
                    returnObj.put("rowNum", rowNum);
                    returnObj.put("pageSize", dataVo.getPageSize());
                    returnObj.put("currentPage", dataVo.getCurrentPage());
                }
                
                List<Map<String, String>> dataList = matrixDataMapper.searchDynamicTableData(dataVo);
                tbodyList = matrixService.matrixTableDataValueHandle(attributeVoList, dataList);
            }
    	}else {
    		MatrixExternalVo externalVo = externalMapper.getMatrixExternalByMatrixUuid(dataVo.getMatrixUuid());
            if(externalVo != null) {
            	IntegrationVo integrationVo = integrationMapper.getIntegrationByUuid(externalVo.getIntegrationUuid());
                IIntegrationHandler handler = IntegrationHandlerFactory.getHandler(integrationVo.getHandler());
        		if (handler == null) {
        			throw new IntegrationHandlerNotFoundException(integrationVo.getHandler());
        		}
        		
            	integrationVo.getParamObj().putAll(jsonObj);
        		IntegrationResultVo resultVo = handler.sendRequest(integrationVo,RequestFrom.MATRIX);
        		if(StringUtils.isNotBlank(resultVo.getError())) {
        			logger.error(resultVo.getError());
            		throw new MatrixExternalException("外部接口访问异常");
            	}else if(StringUtils.isNotBlank(resultVo.getTransformedResult())) {
        			JSONObject transformedResult = JSONObject.parseObject(resultVo.getTransformedResult());
        			if(MapUtils.isNotEmpty(transformedResult)) {
        				returnObj.putAll(transformedResult);
        				JSONArray tbodyArray = transformedResult.getJSONArray("tbodyList");
        				if(CollectionUtils.isNotEmpty(tbodyArray)) {
        					for(int i = 0; i < tbodyArray.size(); i++) {
        						JSONObject rowData = tbodyArray.getJSONObject(i);
        						Integer pageSize = jsonObj.getInteger("pageSize");
        						pageSize = pageSize == null ? 10 : pageSize;
        						if(MapUtils.isNotEmpty(rowData)) {
        							Map<String, Object> rowDataMap = new HashMap<>();
        							for(Entry<String, Object> entry : rowData.entrySet()) {
        								rowDataMap.put(entry.getKey(), matrixService.matrixAttributeValueHandle(entry.getValue()));
        							}
        							tbodyList.add(rowDataMap);
        							if(tbodyList.size() >= pageSize) {
            							break;
            						}
        						}
        					}
        				}
        			}
        		}
            }
    	}

		returnObj.put("tbodyList", tbodyList);
		//TODO 暂时屏蔽引用，没考虑好怎么实现
        //List<ProcessMatrixDispatcherVo> dispatcherVoList = matrixMapper.getMatrixDispatcherByMatrixUuid(dataVo.getMatrixUuid());
        returnObj.put("dispatcherVoList", CollectionUtils.EMPTY_COLLECTION);//dispatcherVoList
        //List<ProcessMatrixFormComponentVo> componentVoList = matrixMapper.getMatrixFormComponentByMatrixUuid(dataVo.getMatrixUuid());
        returnObj.put("componentVoList", CollectionUtils.EMPTY_COLLECTION);//componentVoList
        returnObj.put("usedCount", 0);//dispatcherVoList.size() + componentVoList.size()
        return returnObj;    	
    }
}
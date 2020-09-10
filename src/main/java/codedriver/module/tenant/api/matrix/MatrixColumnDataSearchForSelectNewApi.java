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
import com.alibaba.fastjson.TypeReference;
import com.google.common.base.Objects;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.constvalue.Expression;
import codedriver.framework.exception.integration.IntegrationHandlerNotFoundException;
import codedriver.framework.exception.type.ParamIrregularException;
import codedriver.framework.integration.core.IIntegrationHandler;
import codedriver.framework.integration.core.IntegrationHandlerFactory;
import codedriver.framework.integration.core.RequestFrom;
import codedriver.framework.integration.dao.mapper.IntegrationMapper;
import codedriver.framework.integration.dto.IntegrationResultVo;
import codedriver.framework.integration.dto.IntegrationVo;
import codedriver.framework.matrix.constvalue.MatrixType;
import codedriver.framework.matrix.dao.mapper.MatrixAttributeMapper;
import codedriver.framework.matrix.dao.mapper.MatrixExternalMapper;
import codedriver.framework.matrix.dao.mapper.MatrixMapper;
import codedriver.framework.matrix.dto.MatrixAttributeVo;
import codedriver.framework.matrix.dto.MatrixColumnVo;
import codedriver.framework.matrix.dto.MatrixDataVo;
import codedriver.framework.matrix.dto.MatrixExternalVo;
import codedriver.framework.matrix.dto.MatrixVo;
import codedriver.framework.matrix.exception.MatrixAttributeNotFoundException;
import codedriver.framework.matrix.exception.MatrixExternalException;
import codedriver.framework.matrix.exception.MatrixExternalNotFoundException;
import codedriver.framework.matrix.exception.MatrixNotFoundException;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.tenant.service.matrix.MatrixService;
@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class MatrixColumnDataSearchForSelectNewApi extends PrivateApiComponentBase {

	private final static Logger logger = LoggerFactory.getLogger(MatrixColumnDataSearchForSelectNewApi.class);
	/** 下拉列表value和text列的组合连接符 **/
    public final static String SELECT_COMPOSE_JOINER= "&=&";
	@Autowired
	private MatrixService matrixService;

	@Autowired
	private MatrixMapper matrixMapper;

	@Autowired
	private MatrixAttributeMapper matrixAttributeMapper;

	@Autowired
	private MatrixExternalMapper matrixExternalMapper;

	@Autowired
	private IntegrationMapper integrationMapper;

	@Override
	public String getToken() {
		return "matrix/column/data/search/forselect/new";
	}

	@Override
	public String getName() {
		return "矩阵属性数据查询-下拉级联接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({ @Param(name = "keyword", desc = "关键字", type = ApiParamType.STRING, xss = true), 
		@Param(name = "matrixUuid", desc = "矩阵Uuid", type = ApiParamType.STRING, isRequired = true), 
		@Param(name = "keywordColumn", desc = "关键字属性uuid", type = ApiParamType.STRING), 
		@Param(name = "columnList", desc = "属性uuid列表", type = ApiParamType.JSONARRAY, isRequired = true), 
		@Param(name = "sourceColumnList", desc = "源属性集合", type = ApiParamType.JSONARRAY),
		@Param(name = "pageSize", desc = "显示条目数", type = ApiParamType.INTEGER),
		@Param(name = "valueList", desc = "精确匹配回显数据参数", type = ApiParamType.JSONARRAY),
	    @Param(name = "filterList", desc = "根据列头uuid,搜索具体的列值，支持多个列分别搜索，注意仅支持静态列表  [{uuid:***,valueList:[]},{uuid:***,valueList:[]}]", type = ApiParamType.JSONARRAY) })
	@Description(desc = "矩阵属性数据查询-下拉级联接口")
	@Output({ @Param(name = "columnDataList", type = ApiParamType.JSONARRAY, desc = "属性数据集合") })
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
	    
		MatrixDataVo dataVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<MatrixDataVo>() {
		});
		MatrixVo matrixVo = matrixMapper.getMatrixByUuid(dataVo.getMatrixUuid());
		if (matrixVo == null) {
			throw new MatrixNotFoundException(dataVo.getMatrixUuid());
		}

		List<String> valueList = JSON.parseArray(JSON.toJSONString(jsonObj.getJSONArray("valueList")), String.class);
		JSONArray filterList = jsonObj.getJSONArray("filterList");
		
		List<String> columnList = dataVo.getColumnList();
		if (CollectionUtils.isEmpty(columnList)) {
			throw new ParamIrregularException("参数“columnList”不符合格式要求");
		}
		String keywordColumn = jsonObj.getString("keywordColumn");
		List<Map<String, JSONObject>> resultList = new ArrayList<>();
		JSONObject returnObj = new JSONObject();
		if (MatrixType.CUSTOM.getValue().equals(matrixVo.getType())) {
			List<MatrixAttributeVo> attributeList = matrixAttributeMapper.getMatrixAttributeByMatrixUuid(dataVo.getMatrixUuid());
			if (CollectionUtils.isNotEmpty(attributeList)) {
				Map<String, MatrixAttributeVo> processMatrixAttributeMap = new HashMap<>();
				for (MatrixAttributeVo processMatrixAttributeVo : attributeList) {
					processMatrixAttributeMap.put(processMatrixAttributeVo.getUuid(), processMatrixAttributeVo);
				}
				/** 属性集合去重 **/
				List<String> distinctColumList = new ArrayList<>();
				for (String column : columnList) {
					if (!processMatrixAttributeMap.containsKey(column)) {
						throw new MatrixAttributeNotFoundException(dataVo.getMatrixUuid(), column);
					}
					if (!distinctColumList.contains(column)) {
						distinctColumList.add(column);
					}
				}
				dataVo.setColumnList(distinctColumList);
				dataVo.setFilterList(filterList);
				List<Map<String, String>> dataMapList = null;
				if (CollectionUtils.isNotEmpty(valueList)) {
					for (String value : valueList) {
						if (value.contains(SELECT_COMPOSE_JOINER)) {
							List<MatrixColumnVo> sourceColumnList = new ArrayList<>();
							String[] split = value.split(SELECT_COMPOSE_JOINER);
							if (StringUtils.isNotBlank(columnList.get(0))) {
								MatrixColumnVo processMatrixColumnVo = new MatrixColumnVo(columnList.get(0), split[0]);
								processMatrixColumnVo.setExpression(Expression.EQUAL.getExpression());
								sourceColumnList.add(processMatrixColumnVo);
							}
							dataVo.setSourceColumnList(sourceColumnList);
							if (columnList.size() >= 2 && StringUtils.isNotBlank(columnList.get(1))) {
								MatrixAttributeVo processMatrixAttribute = processMatrixAttributeMap.get(columnList.get(1));
								if (processMatrixAttribute == null) {
									throw new MatrixAttributeNotFoundException(dataVo.getMatrixUuid(), columnList.get(1));
								}
								dataVo.setKeyword(split[1]);
								dataMapList = matrixService.matrixAttributeValueKeyWordSearch(processMatrixAttribute,dataVo);
								if (CollectionUtils.isNotEmpty(dataMapList)) {
									for (Map<String, String> dataMap : dataMapList) {
										Map<String, JSONObject> resultMap = new HashMap<>(dataMap.size());
										for (Entry<String, String> entry : dataMap.entrySet()) {
											String attributeUuid = entry.getKey();
											resultMap.put(attributeUuid, matrixService.matrixAttributeValueHandle(processMatrixAttributeMap.get(attributeUuid), entry.getValue()));
										}
										JSONObject textObj = resultMap.get(columnList.get(1));
										if (MapUtils.isNotEmpty(textObj) && Objects.equal(textObj.get("text"), split[1])) {
											resultList.add(resultMap);
											;
										}
									}
								} else {
									return returnObj;
								}

							} else {
								return returnObj;
							}
						}
					}
				} else {
				    MatrixAttributeVo processMatrixAttribute = null;
					if (StringUtils.isNotBlank(keywordColumn) && StringUtils.isNotBlank(dataVo.getKeyword())) {
						processMatrixAttribute = processMatrixAttributeMap.get(keywordColumn);
						if (processMatrixAttribute == null) {
							throw new MatrixAttributeNotFoundException(dataVo.getMatrixUuid(), keywordColumn);
						}
					}
					dataMapList = matrixService.matrixAttributeValueKeyWordSearch(processMatrixAttribute, dataVo);
					for (Map<String, String> dataMap : dataMapList) {
						Map<String, JSONObject> resultMap = new HashMap<>(dataMap.size());
						for (Entry<String, String> entry : dataMap.entrySet()) {
							String attributeUuid = entry.getKey();
							resultMap.put(attributeUuid, matrixService.matrixAttributeValueHandle(processMatrixAttributeMap.get(attributeUuid), entry.getValue()));
						}
						resultList.add(resultMap);
					}
				}
			}

		} else {
			MatrixExternalVo externalVo = matrixExternalMapper.getMatrixExternalByMatrixUuid(dataVo.getMatrixUuid());
			if (externalVo == null) {
				throw new MatrixExternalNotFoundException(dataVo.getMatrixUuid());
			}
			IntegrationVo integrationVo = integrationMapper.getIntegrationByUuid(externalVo.getIntegrationUuid());
			IIntegrationHandler handler = IntegrationHandlerFactory.getHandler(integrationVo.getHandler());
			if (handler == null) {
				throw new IntegrationHandlerNotFoundException(integrationVo.getHandler());
			}
			List<String> attributeList = new ArrayList<>();
			List<MatrixAttributeVo> processMatrixAttributeList = matrixService.getExternalMatrixAttributeList(dataVo.getMatrixUuid(), integrationVo);
			for (MatrixAttributeVo processMatrixAttributeVo : processMatrixAttributeList) {
				attributeList.add(processMatrixAttributeVo.getUuid());
			}

			for (String column : columnList) {
				if (!attributeList.contains(column)) {
					throw new MatrixAttributeNotFoundException(dataVo.getMatrixUuid(), column);
				}
			}
			List<MatrixColumnVo> sourceColumnList = new ArrayList<>();
			jsonObj.put("sourceColumnList", sourceColumnList); //防止集成管理 js length 异常
			if (CollectionUtils.isNotEmpty(valueList)) {
				for (String value : valueList) {
					if (value.contains(SELECT_COMPOSE_JOINER)) {
						
						String[] split = value.split(SELECT_COMPOSE_JOINER);
						for (int i = 0; i < split.length; i++) {
							String column = columnList.get(i);
							if (StringUtils.isNotBlank(column)) {
								MatrixColumnVo processMatrixColumnVo = new MatrixColumnVo(column, split[i]);
								processMatrixColumnVo.setExpression(Expression.EQUAL.getExpression());
								sourceColumnList.add(processMatrixColumnVo);
							}
						}
						// dataVo.setSourceColumnList(sourceColumnList);
						integrationVo.getParamObj().putAll(jsonObj);
						IntegrationResultVo resultVo = handler.sendRequest(integrationVo, RequestFrom.MATRIX);
						if (StringUtils.isNotBlank(resultVo.getError())) {
							logger.error(resultVo.getError());
							throw new MatrixExternalException("外部接口访问异常");
						} else {
							resultList.addAll(matrixService.getExternalDataTbodyList(resultVo, columnList, dataVo.getPageSize(), null));
						}
					}
				}
			} else {
				if (StringUtils.isNotBlank(keywordColumn) && StringUtils.isNotBlank(dataVo.getKeyword())) {
					if (!attributeList.contains(keywordColumn)) {
						throw new MatrixAttributeNotFoundException(dataVo.getMatrixUuid(), keywordColumn);
					}
				}
				if (StringUtils.isNotBlank(keywordColumn) && StringUtils.isNotBlank(dataVo.getKeyword())) {
					MatrixColumnVo processMatrixColumnVo = new MatrixColumnVo();
					processMatrixColumnVo.setColumn(keywordColumn);
					processMatrixColumnVo.setExpression(Expression.LIKE.getExpression());
					processMatrixColumnVo.setValue(dataVo.getKeyword());
					sourceColumnList = dataVo.getSourceColumnList();
					sourceColumnList.add(processMatrixColumnVo);
					jsonObj.put("sourceColumnList", sourceColumnList);
				}
				integrationVo.getParamObj().putAll(jsonObj);
				IntegrationResultVo resultVo = handler.sendRequest(integrationVo, RequestFrom.MATRIX);
				if (StringUtils.isNotBlank(resultVo.getError())) {
					logger.error(resultVo.getError());
					throw new MatrixExternalException("外部接口访问异常");
				} else {
					resultList = matrixService.getExternalDataTbodyList(resultVo, columnList, dataVo.getPageSize(), null);
				}
			}
		}
		if(columnList.size() == 2) {
			for(Map<String, JSONObject> resultObj : resultList) {
				JSONObject firstObj = resultObj.get(columnList.get(0));
				String firstValue = firstObj.getString("value");
				String firstText = firstObj.getString("text");
				JSONObject secondObj = resultObj.get(columnList.get(1));
				String secondText = secondObj.getString("text");
				firstObj.put("compose", firstValue + SELECT_COMPOSE_JOINER + secondText);
				secondObj.put("compose", secondText + "(" + firstText + ")");
			}
		}
		
		returnObj.put("columnDataList", resultList);
		return returnObj;
	}
}

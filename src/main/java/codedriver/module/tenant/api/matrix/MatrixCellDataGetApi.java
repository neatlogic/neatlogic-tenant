package codedriver.module.tenant.api.matrix;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.constvalue.Expression;
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
import codedriver.framework.matrix.dto.MatrixColumnVo;
import codedriver.framework.matrix.dto.MatrixExternalVo;
import codedriver.framework.matrix.dto.MatrixVo;
import codedriver.framework.matrix.exception.MatrixAttributeNotFoundException;
import codedriver.framework.matrix.exception.MatrixExternalException;
import codedriver.framework.matrix.exception.MatrixExternalNotFoundException;
import codedriver.framework.matrix.exception.MatrixNotFoundException;
import codedriver.framework.restful.core.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.tenant.service.matrix.MatrixService;
@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class MatrixCellDataGetApi extends PrivateApiComponentBase {

	private final static Logger logger = LoggerFactory.getLogger(MatrixCellDataGetApi.class);

	@Autowired
	private MatrixService matrixService;

	@Autowired
	private MatrixMapper matrixMapper;

	@Autowired
	private MatrixDataMapper matrixDataMapper;

	@Autowired
	private MatrixAttributeMapper matrixAttributeMapper;

	@Autowired
	private MatrixExternalMapper matrixExternalMapper;

	@Autowired
	private IntegrationMapper integrationMapper;

	@Override
	public String getToken() {
		return "matrix/celldata/get";
	}

	@Override
	public String getName() {
		return "矩阵单元格数据获取接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({ @Param(name = "matrixUuid", type = ApiParamType.STRING, isRequired = true, desc = "矩阵uuid"), @Param(name = "sourceColumn", type = ApiParamType.STRING, isRequired = true, desc = "源列属性uuid"), @Param(name = "sourceColumnValueList", type = ApiParamType.JSONARRAY, isRequired = true, desc = "源列属性值列表"), @Param(name = "targetColumn", type = ApiParamType.STRING, isRequired = true, desc = "目标列属性uuid") })
	@Output({ @Param(name = "Return", type = ApiParamType.JSONARRAY, desc = "目标列属性值列表") })
	@Description(desc = "矩阵单元格数据获取接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String matrixUuid = jsonObj.getString("matrixUuid");
		MatrixVo matrixVo = matrixMapper.getMatrixByUuid(matrixUuid);
		if (matrixVo == null) {
			throw new MatrixNotFoundException(matrixUuid);
		}

		List<String> resultObj = new ArrayList<>();
		List<String> sourceColumnValueList = JSON.parseArray(jsonObj.getString("sourceColumnValueList"), String.class);
		if (CollectionUtils.isNotEmpty(sourceColumnValueList)) {
			String sourceColumn = jsonObj.getString("sourceColumn");
			String targetColumn = jsonObj.getString("targetColumn");
			if (MatrixType.CUSTOM.getValue().equals(matrixVo.getType())) {
				List<MatrixAttributeVo> attributeList = matrixAttributeMapper.getMatrixAttributeByMatrixUuid(matrixUuid);
				List<String> attributeUuidList = attributeList.stream().map(MatrixAttributeVo::getUuid).collect(Collectors.toList());
				if (!attributeUuidList.contains(sourceColumn)) {
					throw new MatrixAttributeNotFoundException(matrixUuid, sourceColumn);
				}

				if (!attributeUuidList.contains(targetColumn)) {
					throw new MatrixAttributeNotFoundException(matrixUuid, targetColumn);
				}
				MatrixColumnVo sourceColumnVo = new MatrixColumnVo();
				sourceColumnVo.setColumn(sourceColumn);
				for (String sourceColumnValue : sourceColumnValueList) {
					sourceColumnVo.setValue(sourceColumnValue);
					String targetColumnValue = null;
					List<String> targetColumnValueList = matrixDataMapper.getDynamicTableCellData(matrixUuid, sourceColumnVo, targetColumn);
					if (CollectionUtils.isNotEmpty(targetColumnValueList)) {
						targetColumnValue = targetColumnValueList.get(0);
					}
					resultObj.add(targetColumnValue);
				}
			} else {
				MatrixExternalVo externalVo = matrixExternalMapper.getMatrixExternalByMatrixUuid(matrixUuid);
				if (externalVo == null) {
					throw new MatrixExternalNotFoundException(matrixUuid);
				}
				IntegrationVo integrationVo = integrationMapper.getIntegrationByUuid(externalVo.getIntegrationUuid());
				IIntegrationHandler handler = IntegrationHandlerFactory.getHandler(integrationVo.getHandler());
				if (handler == null) {
					throw new IntegrationHandlerNotFoundException(integrationVo.getHandler());
				}
				List<String> attributeUuidList = new ArrayList<>();
				List<MatrixAttributeVo> processMatrixAttributeList = matrixService.getExternalMatrixAttributeList(matrixUuid, integrationVo);
				for (MatrixAttributeVo processMatrixAttributeVo : processMatrixAttributeList) {
					attributeUuidList.add(processMatrixAttributeVo.getUuid());
				}
				if (!attributeUuidList.contains(sourceColumn)) {
					throw new MatrixAttributeNotFoundException(matrixUuid, sourceColumn);
				}

				if (!attributeUuidList.contains(targetColumn)) {
					throw new MatrixAttributeNotFoundException(matrixUuid, targetColumn);
				}

				List<MatrixColumnVo> sourceColumnList = new ArrayList<>();
				MatrixColumnVo sourceColumnVo = new MatrixColumnVo();
				sourceColumnVo.setColumn(sourceColumn);
				List<String> columnList = new ArrayList<>();
				columnList.add(targetColumn);
				for (String sourceColumnValue : sourceColumnValueList) {
					sourceColumnVo.setValue(sourceColumnValue);
					sourceColumnVo.setExpression(Expression.EQUAL.getExpression());
					String targetColumnValue = null;
					sourceColumnList.clear();
					sourceColumnList.add(sourceColumnVo);
					integrationVo.getParamObj().put("sourceColumnList", sourceColumnList);
					IntegrationResultVo resultVo = handler.sendRequest(integrationVo, RequestFrom.MATRIX);
					if (StringUtils.isNotBlank(resultVo.getError())) {
						logger.error(resultVo.getError());
						throw new MatrixExternalException("外部接口访问异常");
					} else {
						List<Map<String, JSONObject>> tbodyList = matrixService.getExternalDataTbodyList(resultVo, columnList, 1, null);
						if (CollectionUtils.isNotEmpty(tbodyList)) {
							targetColumnValue = tbodyList.get(0).get(targetColumn).getString("value");
						}
					}
					resultObj.add(targetColumnValue);
				}
			}
		}

		return resultObj;
	}

}

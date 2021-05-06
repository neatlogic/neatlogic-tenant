package codedriver.module.tenant.api.matrix;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.auth.label.FRAMEWOKR_BASE;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.matrix.constvalue.MatrixAttributeType;
import codedriver.framework.matrix.constvalue.MatrixType;
import codedriver.framework.matrix.dao.mapper.MatrixAttributeMapper;
import codedriver.framework.matrix.dao.mapper.MatrixMapper;
import codedriver.framework.matrix.dto.MatrixAttributeVo;
import codedriver.framework.matrix.dto.MatrixVo;
import codedriver.framework.matrix.exception.MatrixExternalException;
import codedriver.framework.matrix.exception.MatrixNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateBinaryStreamApiComponentBase;
import codedriver.framework.util.ExcelUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @program: codedriver
 * @description:
 * @create: 2020-03-26 19:05
 **/
@Service
@AuthAction(action = FRAMEWOKR_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class MatrixAttributeExportApi extends PrivateBinaryStreamApiComponentBase {

	@Resource
	private MatrixMapper matrixMapper;

	@Resource
	private MatrixAttributeMapper attributeMapper;

	@Override
	public String getToken() {
		return "matrix/attribute/export";
	}

	@Override
	public String getName() {
		return "矩阵模板导出接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({ @Param(name = "matrixUuid", desc = "矩阵Uuid", type = ApiParamType.STRING, isRequired = true) })
	@Description(desc = "矩阵模板导出接口")
	@Override
	public Object myDoService(JSONObject paramObj, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String matrixUuid = paramObj.getString("matrixUuid");
		MatrixVo matrixVo = matrixMapper.getMatrixByUuid(matrixUuid);
		if (matrixVo == null) {
			throw new MatrixNotFoundException(matrixUuid);
		}

		if (MatrixType.CUSTOM.getValue().equals(matrixVo.getType())) {
			List<MatrixAttributeVo> attributeVoList = attributeMapper.getMatrixAttributeByMatrixUuid(matrixUuid);
			if (CollectionUtils.isNotEmpty(attributeVoList)) {
				List<String> headerList = new ArrayList<>();
				List<List<String>> columnSelectValueList = new ArrayList<>();
				headerList.add("uuid");
				columnSelectValueList.add(new ArrayList<>());
				for (MatrixAttributeVo attributeVo : attributeVoList) {
					headerList.add(attributeVo.getName());
					List<String> selectValueList = new ArrayList<>();
					decodeDataConfig(attributeVo, selectValueList);
					columnSelectValueList.add(selectValueList);
				}
				String fileNameEncode = matrixVo.getName() + "_模板.xls";
				Boolean flag = request.getHeader("User-Agent").indexOf("Gecko") > 0;
				if (request.getHeader("User-Agent").toLowerCase().indexOf("msie") > 0 || flag) {
					fileNameEncode = URLEncoder.encode(fileNameEncode, "UTF-8");// IE浏览器
				} else {
					fileNameEncode = new String(fileNameEncode.replace(" ", "").getBytes(StandardCharsets.UTF_8), "ISO8859-1");
				}
				response.setContentType("application/vnd.ms-excel;charset=utf-8");
				response.setHeader("Content-Disposition", "attachment;fileName=\"" + fileNameEncode + "\"");
				ExcelUtil.exportExcelHeaders(headerList, columnSelectValueList, response.getOutputStream());
			}
		} else {
			throw new MatrixExternalException("矩阵外部数据源没有导出模板操作");
		}

		return null;
	}

	// 解析config，抽取属性下拉框值
	private void decodeDataConfig(MatrixAttributeVo attributeVo, List<String> selectValueList) {
		if (StringUtils.isNotBlank(attributeVo.getConfig())) {
			String config = attributeVo.getConfig();
			JSONObject configObj = JSONObject.parseObject(config);
			if (MatrixAttributeType.SELECT.getValue().equals(configObj.getString("handler"))) {
				if (configObj.containsKey("config")) {
					JSONArray configArray = configObj.getJSONArray("config");
					for (int i = 0; i < configArray.size(); i++) {
						JSONObject param = configArray.getJSONObject(i);
						selectValueList.add(param.getString("value"));
					}
				}
			}
		}
	}
}

/*
 * Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package neatlogic.module.tenant.api.matrix;

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
import neatlogic.framework.util.ExcelUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @program: neatlogic
 * @description:
 * @create: 2020-03-26 19:05
 **/
@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class MatrixAttributeExportApi extends PrivateBinaryStreamApiComponentBase {

	@Resource
	private MatrixMapper matrixMapper;

	@Override
	public String getToken() {
		return "matrix/attribute/export";
	}

	@Override
	public String getName() {
		return "????????????????????????";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({ @Param(name = "matrixUuid", desc = "??????Uuid", type = ApiParamType.STRING, isRequired = true) })
	@Description(desc = "????????????????????????")
	@Override
	public Object myDoService(JSONObject paramObj, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String matrixUuid = paramObj.getString("matrixUuid");
		MatrixVo matrixVo = matrixMapper.getMatrixByUuid(matrixUuid);
		if (matrixVo == null) {
			throw new MatrixNotFoundException(matrixUuid);
		}
		IMatrixDataSourceHandler matrixDataSourceHandler = MatrixDataSourceHandlerFactory.getHandler(matrixVo.getType());
		if (matrixDataSourceHandler == null) {
			throw new MatrixDataSourceHandlerNotFoundException(matrixVo.getType());
		}
		JSONObject resultObj = matrixDataSourceHandler.exportAttribute(matrixVo);
		if (MapUtils.isEmpty(resultObj)) {
			return null;
		}
		JSONArray headerArray = resultObj.getJSONArray("headerList");
		if (CollectionUtils.isEmpty(headerArray)) {
			return null;
		}
		List<String> headerList = headerArray.toJavaList(String.class);
		JSONArray columnSelectValueArray = resultObj.getJSONArray("columnSelectValueList");
		if (CollectionUtils.isEmpty(columnSelectValueArray)) {
			return null;
		}
		List<List<String>> columnSelectValueList = new ArrayList<>();
		for (int i = 0; i < columnSelectValueArray.size(); i++) {
			JSONArray columnSelectValue = columnSelectValueArray.getJSONArray(i);
			if (columnSelectValue != null) {
				columnSelectValueList.add(columnSelectValue.toJavaList(String.class));
			}
		}
		String fileNameEncode = matrixVo.getName() + "_??????.xls";
		Boolean flag = request.getHeader("User-Agent").indexOf("Gecko") > 0;
		if (request.getHeader("User-Agent").toLowerCase().indexOf("msie") > 0 || flag) {
			fileNameEncode = URLEncoder.encode(fileNameEncode, "UTF-8");// IE?????????
		} else {
			fileNameEncode = new String(fileNameEncode.replace(" ", "").getBytes(StandardCharsets.UTF_8), "ISO8859-1");
		}
		response.setContentType("application/vnd.ms-excel;charset=utf-8");
		response.setHeader("Content-Disposition", " attachment; filename=\"" + fileNameEncode + "\"");
		ExcelUtil.exportExcelHeaders(headerList, columnSelectValueList, response.getOutputStream());
//		if (MatrixType.CUSTOM.getValue().equals(matrixVo.getType())) {
//			List<MatrixAttributeVo> attributeVoList = attributeMapper.getMatrixAttributeByMatrixUuid(matrixUuid);
//			if (CollectionUtils.isNotEmpty(attributeVoList)) {
//				List<String> headerList = new ArrayList<>();
//				List<List<String>> columnSelectValueList = new ArrayList<>();
//				headerList.add("uuid");
//				columnSelectValueList.add(new ArrayList<>());
//				for (MatrixAttributeVo attributeVo : attributeVoList) {
//					headerList.add(attributeVo.getName());
//					List<String> selectValueList = new ArrayList<>();
//					decodeDataConfig(attributeVo, selectValueList);
//					columnSelectValueList.add(selectValueList);
//				}
//				String fileNameEncode = matrixVo.getName() + "_??????.xls";
//				Boolean flag = request.getHeader("User-Agent").indexOf("Gecko") > 0;
//				if (request.getHeader("User-Agent").toLowerCase().indexOf("msie") > 0 || flag) {
//					fileNameEncode = URLEncoder.encode(fileNameEncode, "UTF-8");// IE?????????
//				} else {
//					fileNameEncode = new String(fileNameEncode.replace(" ", "").getBytes(StandardCharsets.UTF_8), "ISO8859-1");
//				}
//				response.setContentType("application/vnd.ms-excel;charset=utf-8");
//				response.setHeader("Content-Disposition", " attachment; filename=\"" + fileNameEncode + "\"");
//				ExcelUtil.exportExcelHeaders(headerList, columnSelectValueList, response.getOutputStream());
//			}
//		} else if (MatrixType.EXTERNAL.getValue().equals(matrixVo.getType()))  {
//			throw new MatrixExternalExportTemplateException();
//		} else if (MatrixType.VIEW.getValue().equals(matrixVo.getType())) {
//			throw new MatrixViewExportTemplateException();
//		}

		return null;
	}

	// ??????config???????????????????????????
//	private void decodeDataConfig(MatrixAttributeVo attributeVo, List<String> selectValueList) {
//		if (StringUtils.isNotBlank(attributeVo.getConfig())) {
//			String config = attributeVo.getConfig();
//			JSONObject configObj = JSONObject.parseObject(config);
//			if (MatrixAttributeType.SELECT.getValue().equals(configObj.getString("handler"))) {
//				if (configObj.containsKey("config")) {
//					JSONArray configArray = configObj.getJSONArray("config");
//					for (int i = 0; i < configArray.size(); i++) {
//						JSONObject param = configArray.getJSONObject(i);
//						selectValueList.add(param.getString("value"));
//					}
//				}
//			}
//		}
//	}
}

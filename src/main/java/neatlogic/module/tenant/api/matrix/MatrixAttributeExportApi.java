/*
 *
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 */

package neatlogic.module.tenant.api.matrix;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.NoAuth;
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
import neatlogic.framework.restful.core.privateapi.binarystream.PrivateBinaryStreamApiComponentBase;
import neatlogic.framework.util.FileUtil;
import neatlogic.framework.util.excel.ExcelBuilder;
import neatlogic.framework.util.excel.SheetBuilder;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @program: neatlogic
 * @description:
 * @create: 2020-03-26 19:05
 **/
@Service
@AuthAction(action = NoAuth.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class MatrixAttributeExportApi extends PrivateBinaryStreamApiComponentBase {

	private static Logger logger = LoggerFactory.getLogger(MatrixAttributeExportApi.class);

	@Resource
	private MatrixMapper matrixMapper;

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
		String fileNameEncode = matrixVo.getName() + "_模板.xlsx";
		fileNameEncode = FileUtil.getEncodedFileName(fileNameEncode);
		response.setContentType("application/vnd.ms-excel;charset=utf-8");
		response.setHeader("Content-Disposition", " attachment; filename=\"" + fileNameEncode + "\"");
		ExcelBuilder builder = new ExcelBuilder(SXSSFWorkbook.class);
		SheetBuilder sheetBuilder = builder.withBorderColor(HSSFColor.HSSFColorPredefined.GREY_40_PERCENT)
				.withHeadFontColor(HSSFColor.HSSFColorPredefined.WHITE)
				.withHeadBgColor(HSSFColor.HSSFColorPredefined.DARK_BLUE)
				.withColumnWidth(30)
				.addSheet("sheet01")
				.withHeaderList(headerList)
				;
		try (Workbook workbook = builder.build();
			 OutputStream os = response.getOutputStream()) {
			Sheet sheet = workbook.getSheet("sheet01");
			if (CollectionUtils.isNotEmpty(columnSelectValueList)) {
				for (int i = 0; i < columnSelectValueList.size(); i++) {
					List<String> defaultValueList = columnSelectValueList.get(i);
					//行添加下拉框
					if (CollectionUtils.isNotEmpty(defaultValueList)) {
						// 1. 创建下拉值数组
						String[] values = new String[defaultValueList.size()];
						defaultValueList.toArray(values);
						// 2. 设置下拉框作用范围（注意SXSSF的行限制）
						CellRangeAddressList regions = new CellRangeAddressList(
								1, // 首行（从第2行开始）
								SXSSFWorkbook.DEFAULT_WINDOW_SIZE - 1, // 末行（最大行数-1）
								i,  // 列号
								i   // 同一列
						);
						// 3. 创建约束（SXSSF需用XSSFDataValidationHelper）
						DataValidationHelper dvHelper = sheet.getDataValidationHelper();
						DataValidationConstraint constraint = dvHelper.createExplicitListConstraint(values);

						// 4. 创建并应用数据验证
						DataValidation validation = dvHelper.createValidation(constraint, regions);

						// 5. 设置Excel的兼容性选项
						validation.setSuppressDropDownArrow(true); // 是否显示下拉箭头
						validation.setShowErrorBox(true); // 输入错误时显示提示
						//将有效性验证添加到表单
						sheet.addValidationData(validation);
					}
				}
			}
			workbook.write(os);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}
}

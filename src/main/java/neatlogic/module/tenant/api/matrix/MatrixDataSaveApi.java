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
import neatlogic.framework.auth.core.AuthActionChecker;
import neatlogic.framework.auth.label.MATRIX_MODIFY;
import neatlogic.framework.auth.label.NoAuth;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.exception.type.ParamNotExistsException;
import neatlogic.framework.exception.type.PermissionDeniedException;
import neatlogic.framework.matrix.constvalue.MatrixType;
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
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @program: neatlogic
 * @description:
 * @create: 2020-03-30 15:26
 **/
@Service
@Transactional
@AuthAction(action = NoAuth.class)
@OperationType(type = OperationTypeEnum.CREATE)
public class MatrixDataSaveApi extends PrivateApiComponentBase {

    @Resource
    private MatrixMapper matrixMapper;

    @Override
    public String getToken() {
        return "matrix/data/save";
    }

    @Override
    public String getName() {
        return "矩阵数据保存接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "matrixUuid", desc = "矩阵uuid", type = ApiParamType.STRING, isRequired = true),
            @Param(name = "rowData", desc = "矩阵数据中的一行数据", type = ApiParamType.JSONOBJECT, isRequired = true)

    })
    @Description(desc = "矩阵数据保存接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String matrixUuid = jsonObj.getString("matrixUuid");
        MatrixVo matrixVo = matrixMapper.getMatrixByUuid(matrixUuid);
        if (matrixVo == null) {
            throw new MatrixNotFoundException(matrixUuid);
        }
        //表单下拉框支持动态添加数据到自定义矩阵
        if (!Objects.equals(MatrixType.CUSTOM.getValue(), matrixVo.getType()) && Boolean.FALSE.equals(AuthActionChecker.check(MATRIX_MODIFY.class))) {
            throw new PermissionDeniedException(MATRIX_MODIFY.class);
        }
        JSONObject rowDataObj = jsonObj.getJSONObject("rowData");
        if (MapUtils.isEmpty(rowDataObj)) {
            throw new ParamNotExistsException("rowData");
        }
        IMatrixDataSourceHandler matrixDataSourceHandler = MatrixDataSourceHandlerFactory.getHandler(matrixVo.getType());
        if (matrixDataSourceHandler == null) {
            throw new MatrixDataSourceHandlerNotFoundException(matrixVo.getType());
        }
        //通过笛卡尔积平铺数据
        JSONArray rowDataList = getRowDataList(rowDataObj);
        for (int i = 0; i < rowDataList.size(); i++) {
            JSONObject rowData = rowDataList.getJSONObject(i);
            matrixDataSourceHandler.saveTableRowData(matrixUuid, rowData);
        }
        return null;
    }

    /**
     * 获取最终的rowData列表
     *
     * @param rowDataObject 入参中的rowData json数据
     * @return 通过笛卡尔积平铺的rowDataArray数据
     */
    private JSONArray getRowDataList(JSONObject rowDataObject) {
        JSONArray result = new JSONArray();
        Set<String> keys = rowDataObject.keySet();
        cartesianProductHelper(rowDataObject, keys.toArray(new String[0]), new JSONObject(), result, 0);
        return result;
    }

    /**
     * @param rowDataObject 入参中的rowData json数据
     * @param keys          入参中的rowData json数据的keys
     * @param current       当前的row
     * @param rowDataArray  返回结果
     * @param index         key的下标
     */
    private void cartesianProductHelper(JSONObject rowDataObject, String[] keys,
                                        JSONObject current, JSONArray rowDataArray, int index) {
        if (index == keys.length) {
            rowDataArray.add(current.clone());
            return;
        }

        String key = keys[index];
        Object valuesObject = rowDataObject.get(key);
        List<Object> values;
        if (valuesObject instanceof JSONArray) {
            values = (JSONArray) valuesObject;
            if (CollectionUtils.isEmpty(values)) {
                values = Collections.singletonList(null);
            }
        } else {
            values = Collections.singletonList(valuesObject);
        }

        for (Object value : values) {
            current.put(key, value);
            cartesianProductHelper(rowDataObject, keys, current, rowDataArray, index + 1);
        }
    }
}

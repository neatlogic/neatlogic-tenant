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

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.MATRIX_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.matrix.constvalue.MatrixType;
import neatlogic.framework.matrix.dao.mapper.MatrixAttributeMapper;
import neatlogic.framework.matrix.dao.mapper.MatrixDataMapper;
import neatlogic.framework.matrix.dao.mapper.MatrixMapper;
import neatlogic.framework.matrix.dto.MatrixVo;
import neatlogic.framework.matrix.exception.MatrixNotFoundException;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@AuthAction(action = MATRIX_MODIFY.class)
@OperationType(type = OperationTypeEnum.UPDATE)
public class BatchAddSortFieldApi extends PrivateApiComponentBase {

    private final Logger logger = LoggerFactory.getLogger(BatchAddSortFieldApi.class);

    @Resource
    private MatrixMapper matrixMapper;

    @Resource
    private MatrixAttributeMapper matrixAttributeMapper;

    @Resource
    private MatrixDataMapper matrixDataMapper;

    @Override
    public String getToken() {
        return "matrix/batch/add/sortfield";
    }

    @Override
    public String getName() {
        return "批量添加排序字段";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "matrixUuid", type = ApiParamType.STRING, desc = "矩阵uuid")
    })
    @Description(desc = "批量添加排序字段")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        List<String> matrixUuidList = new ArrayList<>();
        String matrixUuid = paramObj.getString("matrixUuid");
        if (StringUtils.isNotBlank(matrixUuid)) {
            MatrixVo matrixVo = matrixMapper.getMatrixByUuid(matrixUuid);
            if (matrixVo == null) {
                throw new MatrixNotFoundException(matrixUuid);
            }
            matrixUuidList.add(matrixUuid);
        } else {
            MatrixVo searchVo = new MatrixVo();
            searchVo.setType(MatrixType.CUSTOM.getValue());
            int rowNum = matrixMapper.searchMatrixCount(searchVo);
            if (rowNum > 0) {
                searchVo.setRowNum(rowNum);
                searchVo.setPageSize(100);
                int pageCount = searchVo.getPageCount();
                for (int currentPage = 1; currentPage <= pageCount; currentPage++) {
                    searchVo.setCurrentPage(currentPage);
                    List<MatrixVo> matrixList = matrixMapper.searchMatrix(searchVo.getKeyword(), searchVo.getType(), searchVo.getStartNum(), searchVo.getPageSize());
                    matrixUuidList.addAll(matrixList.stream().map(MatrixVo::getUuid).collect(Collectors.toList()));
                }
            }
        }
        for (String uuid : matrixUuidList) {
            try {
                matrixAttributeMapper.addMatrixDynamicTableColumnSort(uuid);
                matrixDataMapper.batchUpdateSortequalsId(uuid);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        return null;
    }
}

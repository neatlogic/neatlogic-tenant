/*Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

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

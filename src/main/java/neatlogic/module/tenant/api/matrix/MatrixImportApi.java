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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.MATRIX_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.matrix.core.IMatrixDataSourceHandler;
import neatlogic.framework.matrix.core.MatrixDataSourceHandlerFactory;
import neatlogic.framework.matrix.dao.mapper.MatrixMapper;
import neatlogic.framework.matrix.dto.MatrixVo;
import neatlogic.framework.matrix.exception.MatrixDataSourceHandlerNotFoundException;
import neatlogic.framework.matrix.exception.MatrixFileNotFoundException;
import neatlogic.framework.matrix.exception.MatrixNotFoundException;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateBinaryStreamApiComponentBase;
import neatlogic.framework.util.$;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Service
@Transactional
@AuthAction(action = MATRIX_MODIFY.class)
@OperationType(type = OperationTypeEnum.CREATE)
public class MatrixImportApi extends PrivateBinaryStreamApiComponentBase {

    @Resource
    private MatrixMapper matrixMapper;

    @Override
    public String getToken() {
        return "matrix/data/import";
    }

    @Override
    public String getName() {
        return "nmtam.matriximportapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "matrixUuid", desc = "term.framework.matrixuuid", type = ApiParamType.STRING, isRequired = true)
    })
    @Description(desc = "nmtam.matriximportapi.getname")
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
        JSONObject returnObj = new JSONObject();
        int update = 0;
        int insert = 0;
        int unExist = 0;
        int failed = 0;
        JSONArray failureReasonList = new JSONArray();
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        //获取所有导入文件
        Map<String, MultipartFile> multipartFileMap = multipartRequest.getFileMap();
        if (multipartFileMap.isEmpty()) {
            throw new MatrixFileNotFoundException();
        }
        for (Map.Entry<String, MultipartFile> entry : multipartFileMap.entrySet()) {
            MultipartFile multipartFile = entry.getValue();
            JSONObject resultObj = matrixDataSourceHandler.importMatrix(matrixVo, multipartFile);
            Integer insertCount = resultObj.getInteger("insert");
            if (insertCount != null) {
                insert += insertCount;
            }
            Integer updateCount = resultObj.getInteger("update");
            if (updateCount != null) {
                update += updateCount;
            }
            Integer unExistCount = resultObj.getInteger("unExist");
            if (unExistCount != null) {
                unExist += unExistCount;
            }
            Integer failedCount = resultObj.getInteger("failed");
            if (failedCount != null) {
                failed += failedCount;
            }
            JSONObject invalidData = resultObj.getJSONObject("invalidDataMap");
            JSONObject repeatData = resultObj.getJSONObject("repeatDataMap");
            if (MapUtils.isNotEmpty(invalidData) || MapUtils.isNotEmpty(repeatData)) {
                JSONObject failureReason = new JSONObject();
                JSONArray failureList = new JSONArray();
                failureReasonList.add(failureReason);
                failureReason.put("list", failureList);
                failureReason.put("item", $.t("nmtam.matriximportapi.mydoservice.filevalidtip", multipartFile.getOriginalFilename()));
                for (Map.Entry<String, Object> entryInvalid : invalidData.entrySet()) {
                    int rowIndex = Integer.parseInt(entryInvalid.getKey()) + 1;
                    JSONObject col = JSON.parseObject(JSON.toJSONString(entryInvalid.getValue()));
                    for (Map.Entry<String, Object> colEntry : col.entrySet()) {
                        int colIndex = Integer.parseInt(colEntry.getKey()) + 1;
                        Object value = colEntry.getValue();
                        failureList.add($.t("nmtam.matriximportapi.mydoservice.validtip", rowIndex, colIndex, value == null ? "" : value));
                    }
                }
                for (Map.Entry<String, Object> entryRepeat : repeatData.entrySet()) {
                    int rowIndex = Integer.parseInt(entryRepeat.getKey()) + 1;
                    JSONObject col = JSON.parseObject(JSON.toJSONString(entryRepeat.getValue()));
                    for (Map.Entry<String, Object> colEntry : col.entrySet()) {
                        int colIndex = Integer.parseInt(colEntry.getKey()) + 1;
                        Object value = colEntry.getValue();
                        failureList.add($.t("nmtam.matriximportapi.mydoservice.validrepeattip", rowIndex, colIndex, value == null ? "" : value));
                    }
                }
            }
        }
        returnObj.put("failureReasonList", failureReasonList);
        returnObj.put("insert", insert);
        returnObj.put("update", update);
        returnObj.put("unExist", unExist);
        returnObj.put("failed", failed);
        return returnObj;
    }
}

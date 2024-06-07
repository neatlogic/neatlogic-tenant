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
        int update = 0, insert = 0, unExist = 0;
        JSONObject invalidDataMap = new JSONObject();
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
            JSONObject invalidData = resultObj.getJSONObject("invalidDataMap");
            invalidDataMap.putAll(invalidData);
            if (MapUtils.isNotEmpty(invalidDataMap)) {
                JSONObject failureReason = new JSONObject();
                JSONArray failureList = new JSONArray();
                failureReasonList.add(failureReason);
                failureReason.put("list",failureList);
                failureReason.put("item",$.t("nmtam.matriximportapi.mydoservice.filevalidtip",multipartFile.getOriginalFilename()));
                for (Map.Entry<String, Object> entryInvalid : invalidDataMap.entrySet()) {
                    int rowIndex = Integer.parseInt(entryInvalid.getKey());
                    JSONObject col = JSON.parseObject(JSON.toJSONString(entryInvalid.getValue()));
                    for (Map.Entry<String, Object> colEntry : col.entrySet()) {
                        int colIndex = Integer.parseInt(colEntry.getKey());
                        Object value = colEntry.getValue();
                        failureList.add($.t("nmtam.matriximportapi.mydoservice.validtip", rowIndex,colIndex,value == null ? "" : value));
                    }
                }
            }
        }
        returnObj.put("failureReasonList", failureReasonList);
        returnObj.put("insert", insert);
        returnObj.put("update", update);
        returnObj.put("unExist", unExist);
        return returnObj;
    }
}

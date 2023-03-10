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

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.exception.type.ParamNotExistsException;
import neatlogic.framework.matrix.core.IMatrixDataSourceHandler;
import neatlogic.framework.matrix.core.MatrixDataSourceHandlerFactory;
import neatlogic.framework.matrix.dao.mapper.MatrixMapper;
import neatlogic.framework.matrix.dto.MatrixAttributeVo;
import neatlogic.framework.matrix.dto.MatrixVo;
import neatlogic.framework.matrix.exception.MatrixDataSourceHandlerNotFoundException;
import neatlogic.framework.matrix.exception.MatrixNotFoundException;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.auth.label.MATRIX_MODIFY;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * @program: neatlogic
 * @description:
 * @create: 2020-03-26 19:07
 **/
@Service
@Transactional
@AuthAction(action = MATRIX_MODIFY.class)
@OperationType(type = OperationTypeEnum.CREATE)
public class MatrixAttributeSaveApi extends PrivateApiComponentBase {

    @Resource
    private MatrixMapper matrixMapper;

    @Override
    public String getToken() {
        return "matrix/attribute/save";
    }

    @Override
    public String getName() {
        return "????????????????????????";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "matrixUuid", desc = "??????uuid", type = ApiParamType.STRING, isRequired = true),
            @Param(name = "matrixAttributeList", desc = "??????????????????", type = ApiParamType.JSONARRAY, isRequired = true),
            @Param(name = "matrixAttributeList[x].uuid", desc = "??????uuid", type = ApiParamType.STRING),
            @Param(name = "matrixAttributeList[x].name", desc = "?????????", type = ApiParamType.STRING),
            @Param(name = "matrixAttributeList[x].type", desc = "??????", type = ApiParamType.STRING),
            @Param(name = "matrixAttributeList[x].isRequired", desc = "????????????", type = ApiParamType.ENUM, rule = "0,1"),
            @Param(name = "matrixAttributeList[x].sort", desc = "??????", type = ApiParamType.INTEGER),
            @Param(name = "matrixAttributeList[x].config", desc = "????????????", type = ApiParamType.JSONOBJECT)
    })
    @Description(desc = "????????????????????????")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String matrixUuid = jsonObj.getString("matrixUuid");
        MatrixVo matrixVo = matrixMapper.getMatrixByUuid(matrixUuid);
        if (matrixVo == null) {
            throw new MatrixNotFoundException(matrixUuid);
        }
        IMatrixDataSourceHandler matrixDataSourceHandler = MatrixDataSourceHandlerFactory.getHandler(matrixVo.getType());
        if (matrixDataSourceHandler == null) {
            throw new MatrixDataSourceHandlerNotFoundException(matrixVo.getType());
        }
        JSONArray matrixAttributeArray = jsonObj.getJSONArray("matrixAttributeList");
        if (CollectionUtils.isEmpty(matrixAttributeArray)) {
            throw new ParamNotExistsException("matrixAttributeList");
        }
        List<MatrixAttributeVo> attributeVoList = matrixAttributeArray.toJavaList(MatrixAttributeVo.class);
        matrixDataSourceHandler.saveAttributeList(matrixUuid, attributeVoList);
//        if (MatrixType.CUSTOM.getValue().equals(matrixVo.getType())) {
//            List<MatrixAttributeVo> attributeVoList = jsonObj.getJSONArray("matrixAttributeList").toJavaList(MatrixAttributeVo.class);
//            List<MatrixAttributeVo> oldMatrixAttributeList = attributeMapper.getMatrixAttributeByMatrixUuid(matrixUuid);
//            boolean dataExist = CollectionUtils.isNotEmpty(oldMatrixAttributeList);
//            if (dataExist) {
//                attributeMapper.deleteAttributeByMatrixUuid(matrixUuid);
//            }
//            String schemaName = TenantContext.get().getDataDbName();
//            if (CollectionUtils.isNotEmpty(attributeVoList)) {
//                //?????????
//                if (dataExist) {
//                    //????????????
//                    //????????????
//                    //?????????
//                    List<String> oldAttributeUuidList = oldMatrixAttributeList.stream().map(MatrixAttributeVo::getUuid).collect(Collectors.toList());
//                    List<String> addAttributeUuidList = new ArrayList<>();
//                    List<String> existedAttributeUuidList = new ArrayList<>();
//                    for (MatrixAttributeVo attributeVo : attributeVoList) {
//                        attributeVo.setMatrixUuid(matrixUuid);
//                        if (oldAttributeUuidList.contains(attributeVo.getUuid())) {
//                            attributeMapper.insertMatrixAttribute(attributeVo);
//                            existedAttributeUuidList.add(attributeVo.getUuid());
//                        } else {
//                            //??????????????????uuid
//                            attributeMapper.insertMatrixAttribute(attributeVo);
//                            addAttributeUuidList.add(attributeVo.getUuid());
//                        }
//                    }
//
//                    //??????????????????
//                    for (String attributeUuid : addAttributeUuidList) {
//                        attributeMapper.addMatrixDynamicTableColumn(attributeUuid, matrixUuid, schemaName);
//                    }
//                    //???????????????????????????uuid??????
//                    oldAttributeUuidList.removeAll(existedAttributeUuidList);
//                    for (String attributeUuid : oldAttributeUuidList) {
//                        attributeMapper.dropMatrixDynamicTableColumn(attributeUuid, matrixUuid, schemaName);
//                    }
//                } else {
//                    for (MatrixAttributeVo attributeVo : attributeVoList) {
//                        attributeVo.setMatrixUuid(matrixUuid);
//                        attributeVo.setUuid(UuidUtil.randomUuid());
//                        attributeMapper.insertMatrixAttribute(attributeVo);
//                    }
//                    attributeMapper.createMatrixDynamicTable(attributeVoList, matrixUuid, schemaName);
//                }
//            } else {
//                //?????????
//                if (dataExist) {
//                    // ???????????????
//                    attributeMapper.dropMatrixDynamicTable(matrixUuid, schemaName);
//                }
//            }
//        } else if (MatrixType.EXTERNAL.getValue().equals(matrixVo.getType())) {
//            throw new MatrixExternalSaveAttributeException();
//        } else if (MatrixType.VIEW.getValue().equals(matrixVo.getType())) {
//            throw new MatrixViewSaveAttributeException();
//        }

        return null;
    }
}

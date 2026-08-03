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
import neatlogic.framework.common.constvalue.CacheControlType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.exception.type.ParamNotExistsException;
import neatlogic.framework.matrix.constvalue.SearchExpression;
import neatlogic.framework.matrix.core.IMatrixDataSourceHandler;
import neatlogic.framework.matrix.core.MatrixDataSourceHandlerFactory;
import neatlogic.framework.matrix.core.MatrixPrivateDataSourceHandlerFactory;
import neatlogic.framework.matrix.dao.mapper.MatrixMapper;
import neatlogic.framework.matrix.dto.*;
import neatlogic.framework.matrix.exception.MatrixAttributeNotFoundException;
import neatlogic.framework.matrix.exception.MatrixDataSourceHandlerNotFoundException;
import neatlogic.framework.matrix.exception.MatrixNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.tenant.service.MatrixService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service
@AuthAction(action = NoAuth.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class MatrixColumnDataSearchForSelectApi extends PrivateApiComponentBase {

    @Resource
    private MatrixMapper matrixMapper;

    @Resource
    private MatrixService matrixService;

    @Override
    public String getToken() {
        return "matrix/column/data/search/forselect";
    }

    @Override
    public String getName() {
        return "矩阵属性数据查询-下拉接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Override
    public boolean disableReturnCircularReferenceDetect() {
        return true;
    }

    /**
     * 获取参数范例
     *
     * @return 参数范例json
     */
    @Override
    public JSONObject example() {
        return new JSONObject()
                .fluentPut("matrixUuid", "8d8daee892404f08b6aeb3fac52031c8")
                .fluentPut("keyword", "系统")
                .fluentPut("keywordColumn", "67f6055224d233cba5c2833035d66101")
                .fluentPut("valueField", "b7fa5b8306513b74acd8a6a3bf4b502e")
                .fluentPut("textField", "67f6055224d233cba5c2833035d66101")
                .fluentPut("hiddenFieldList", new JSONArray()
                        .fluentAdd("ed0f56f7ae7b38c88ac85624b32ecdc0")
                        .fluentAdd("4051f6a4cd993a1ea1952219050abda0")
                )
                .fluentPut("currentPage", 1)
                .fluentPut("pageSize", 20)
                .fluentPut("needPage", true)
                .fluentPut("defaultValue", new JSONArray().fluentAdd(new JSONObject()
                        .fluentPut("value", "481894860644358")
                        .fluentPut("text", "个贷系统")
//                        .fluentPut("ed0f56f7ae7b38c88ac85624b32ecdc0", "ALS")
//                        .fluentPut("4051f6a4cd993a1ea1952219050abda0", "管理员,杨-志")
                ))
                .fluentPut("filterList", new JSONArray().fluentAdd(
                        new JSONObject()
                                .fluentPut("uuid", "4051f6a4cd993a1ea1952219050abda0")
                                .fluentPut("uniqueIdentifier", "owner")
                                .fluentPut("expression", "equal")
                                .fluentPut("description", "uuid与uniqueIdentifier字段二选一，expression选项有like|notlike|equal|unequal|include|exclude|between|greater-than|less-than|is-null|match|is-not-null")
                                .fluentPut("valueList", new JSONArray().fluentAdd("管理员"))
                ))
                .fluentPut("matrixLabel", "cmdbAppOwner")
                .fluentPut("keywordColumnUniqueIdentifier", "name")
                .fluentPut("valueFieldUniqueIdentifier", "_id")
                .fluentPut("textFieldUniqueIdentifier", "name")
                .fluentPut("hiddenFieldUniqueIdentifierList", new JSONArray()
                        .fluentAdd("abbrName")
                        .fluentAdd("owner")
                )
                ;
    }

    @CacheControl(cacheControlType = CacheControlType.MAXAGE, maxAge = 30000)
    @Input({
            @Param(name = "keyword", desc = "关键字", type = ApiParamType.STRING, xss = true),
            @Param(name = "matrixUuid", desc = "矩阵Uuid", type = ApiParamType.STRING),
            @Param(name = "keywordColumn", desc = "关键字属性uuid", type = ApiParamType.STRING),
            @Param(name = "valueField", desc = "value属性uuid", type = ApiParamType.STRING),
            @Param(name = "textField", desc = "text属性uuid", type = ApiParamType.STRING),
            @Param(name = "hiddenFieldList", desc = "隐藏属性uuid列表", type = ApiParamType.JSONARRAY),
            @Param(name = "currentPage", desc = "当前页", type = ApiParamType.INTEGER),
            @Param(name = "pageSize", desc = "显示条目数", type = ApiParamType.INTEGER),
            @Param(name = "needPage", desc = "是否需要分页", type = ApiParamType.BOOLEAN),
            @Param(name = "defaultValue", desc = "精确匹配回显数据参数", type = ApiParamType.JSONARRAY),
            @Param(name = "filterList", desc = "过滤条件集合", type = ApiParamType.JSONARRAY),

            @Param(name = "matrixLabel", desc = "矩阵名", type = ApiParamType.STRING),
            @Param(name = "keywordColumnUniqueIdentifier", desc = "关键字属性唯一标识", type = ApiParamType.STRING),
            @Param(name = "valueFieldUniqueIdentifier", desc = "value属性唯一标识", type = ApiParamType.STRING),
            @Param(name = "textFieldUniqueIdentifier", desc = "text属性唯一标识", type = ApiParamType.STRING),
            @Param(name = "hiddenFieldUniqueIdentifierList", desc = "隐藏属性唯一标识列表", type = ApiParamType.JSONARRAY)
    })
    @Output({
            @Param(name = "dataList", type = ApiParamType.JSONARRAY, desc = "属性数据集合"),
            @Param(explode = BasePageVo.class)
    })
    @Description(desc = "矩阵属性数据查询-下拉级联接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        boolean fieldKeyUseUniqueIdentifier = false;
        String keyword = jsonObj.getString("keyword");
        String matrixUuid = jsonObj.getString("matrixUuid");
        String matrixLabel = jsonObj.getString("matrixLabel");
        if (StringUtils.isBlank(matrixUuid) && StringUtils.isBlank(matrixLabel)) {
            throw new ParamNotExistsException("matrixUuid", "matrixLabel");
        }
        MatrixVo matrixVo = null;
        if (StringUtils.isNotBlank(matrixUuid)) {
            matrixVo = MatrixPrivateDataSourceHandlerFactory.getMatrixVo(matrixUuid);
            if (matrixVo == null) {
                matrixVo = matrixMapper.getMatrixByUuid(matrixUuid);
            }
        } else if (StringUtils.isNotBlank(matrixLabel)) {
            matrixVo = MatrixPrivateDataSourceHandlerFactory.getMatrixVoByLabel(matrixLabel);
            if (matrixVo == null) {
                matrixVo = matrixMapper.getMatrixByLabel(matrixLabel);
            }
        }
        if (matrixVo == null) {
            throw new MatrixNotFoundException(StringUtils.defaultIfBlank(matrixUuid, matrixLabel));
        } else {
            matrixUuid = matrixVo.getUuid();
        }
        IMatrixDataSourceHandler matrixDataSourceHandler = MatrixDataSourceHandlerFactory.getHandler(matrixVo.getType());
        if (matrixDataSourceHandler == null) {
            throw new MatrixDataSourceHandlerNotFoundException(matrixVo.getType());
        }

        List<MatrixAttributeVo> matrixAttributeList = matrixDataSourceHandler.getAttributeList(matrixVo);
        if (CollectionUtils.isEmpty(matrixAttributeList)) {
            return new JSONObject();
        }
        Map<String, String> uuidToUniqueIdentifierMap = new HashMap<>();
        Map<String, String> uniqueIdentifierToUuidMap = new HashMap<>();
        for (MatrixAttributeVo matrixAttributeVo : matrixAttributeList) {
            String uniqueIdentifier = matrixAttributeVo.getUniqueIdentifier();
            if (StringUtils.isBlank(uniqueIdentifier)) {
                continue;
            }
            uniqueIdentifierToUuidMap.put(uniqueIdentifier, matrixAttributeVo.getUuid());
            uuidToUniqueIdentifierMap.put(matrixAttributeVo.getUuid(), uniqueIdentifier);
        }
        List<MatrixFilterVo> filterList = new ArrayList<>();
        JSONArray filterArray = jsonObj.getJSONArray("filterList");
        if (CollectionUtils.isNotEmpty(filterArray)) {
            for (int i = 0; i < filterArray.size(); i++) {
                MatrixFilterVo matrixFilterVo = filterArray.getObject(i, MatrixFilterVo.class);
                if (matrixFilterVo != null) {
                    String uuid = matrixFilterVo.getUuid();
                    if (StringUtils.isBlank(uuid)) {
                        if (StringUtils.isNotBlank(matrixFilterVo.getUniqueIdentifier())) {
                            uuid = uniqueIdentifierToUuidMap.get(matrixFilterVo.getUniqueIdentifier());
                        }
                    }
                    if (StringUtils.isNotBlank(uuid)) {
                        if (CollectionUtils.isNotEmpty(matrixFilterVo.getValueList())
                                || Objects.equals(matrixFilterVo.getExpression(), SearchExpression.NULL.getExpression())
                                || Objects.equals(matrixFilterVo.getExpression(), SearchExpression.NOTNULL.getExpression())
                        ) {
                            filterList.add(new MatrixFilterVo(uuid, matrixFilterVo.getType(), matrixFilterVo.getExpression(), matrixFilterVo.getValueList()));
                        }
                    }
                }
            }
        }

        List<String> attributeList = matrixAttributeList.stream().map(MatrixAttributeVo::getUuid).toList();
        String valueField = jsonObj.getString("valueField");
        String valueFieldUniqueIdentifier = jsonObj.getString("valueFieldUniqueIdentifier");
        if (StringUtils.isNotBlank(valueField)) {
            if (!attributeList.contains(valueField)) {
                throw new MatrixAttributeNotFoundException(matrixVo.getName(), valueField);
            }
        } else {
            if (StringUtils.isBlank(valueFieldUniqueIdentifier)) {
                throw new ParamNotExistsException("valueField", "valueFieldUniqueIdentifier");
            }
            String attrUuid = uniqueIdentifierToUuidMap.get(valueFieldUniqueIdentifier);
            if (StringUtils.isBlank(attrUuid)) {
                throw new MatrixAttributeNotFoundException(matrixVo.getName(), valueFieldUniqueIdentifier);
            }
            fieldKeyUseUniqueIdentifier = true;
            valueField = attrUuid;
        }

        String textField = jsonObj.getString("textField");
        String textFieldUniqueIdentifier = jsonObj.getString("textFieldUniqueIdentifier");
        if (StringUtils.isNotBlank(textField)) {
            if (!attributeList.contains(textField)) {
                throw new MatrixAttributeNotFoundException(matrixVo.getName(), textField);
            }
        } else {
            if (StringUtils.isBlank(textFieldUniqueIdentifier)) {
                throw new ParamNotExistsException("textField", "textFieldUniqueIdentifier");
            }
            String attrUuid = uniqueIdentifierToUuidMap.get(textFieldUniqueIdentifier);
            if (StringUtils.isBlank(attrUuid)) {
                throw new MatrixAttributeNotFoundException(matrixVo.getName(), textFieldUniqueIdentifier);
            }
            fieldKeyUseUniqueIdentifier = true;
            textField = attrUuid;
        }

        List<String> hiddenFieldList = new ArrayList<>();
        JSONArray hiddenFieldArray = jsonObj.getJSONArray("hiddenFieldList");
        JSONArray hiddenFieldUniqueIdentifierList = jsonObj.getJSONArray("hiddenFieldUniqueIdentifierList");
        if (CollectionUtils.isNotEmpty(hiddenFieldArray)) {
            for (int i = 0; i < hiddenFieldArray.size(); i++) {
                String hiddenField = hiddenFieldArray.getString(i);
                if (StringUtils.isNotBlank(hiddenField)) {
                    if (!attributeList.contains(hiddenField)) {
                        throw new MatrixAttributeNotFoundException(matrixVo.getName(), hiddenField);
                    }
                    if (!hiddenFieldList.contains(hiddenField)) {
                        hiddenFieldList.add(hiddenField);
                    }
                }
            }
        } else if (CollectionUtils.isNotEmpty(hiddenFieldUniqueIdentifierList)) {
            for (int i = 0; i < hiddenFieldUniqueIdentifierList.size(); i++) {
                String hiddenFieldUniqueIdentifier = hiddenFieldUniqueIdentifierList.getString(i);
                if (StringUtils.isBlank(hiddenFieldUniqueIdentifier)) {
                    continue;
                }
                String hiddenField = uniqueIdentifierToUuidMap.get(hiddenFieldUniqueIdentifier);
                if (StringUtils.isBlank(hiddenField)) {
                    throw new MatrixAttributeNotFoundException(matrixVo.getName(), hiddenFieldUniqueIdentifier);
                }
                if (!hiddenFieldList.contains(hiddenField)) {
                    fieldKeyUseUniqueIdentifier = true;
                    hiddenFieldList.add(hiddenField);
                }
            }
        }
        String keywordColumn = jsonObj.getString("keywordColumn");
        String keywordColumnUniqueIdentifier = jsonObj.getString("keywordColumnUniqueIdentifier");
        if (StringUtils.isNotBlank(keywordColumn)) {
            if (!attributeList.contains(keywordColumn)) {
                throw new MatrixAttributeNotFoundException(matrixVo.getName(), keywordColumn);
            }
        } else if (StringUtils.isNotBlank(keywordColumnUniqueIdentifier)) {
            String attrUuid = uniqueIdentifierToUuidMap.get(keywordColumnUniqueIdentifier);
            if (StringUtils.isBlank(attrUuid)) {
                throw new MatrixAttributeNotFoundException(matrixVo.getName(), keywordColumnUniqueIdentifier);
            }
            fieldKeyUseUniqueIdentifier = true;
            keywordColumn = attrUuid;
        }
        Boolean needPage = jsonObj.getBoolean("needPage");
        needPage = needPage != null ? needPage : true;
        Integer currentPage = jsonObj.getInteger("currentPage");
        currentPage = currentPage == null || currentPage < 1 ? 1 : currentPage;
        Integer pageSize = jsonObj.getInteger("pageSize");
        pageSize = pageSize == null || pageSize < 0? 20 : pageSize;
        JSONArray defaultValue = jsonObj.getJSONArray("defaultValue");
        JSONObject resultObj = matrixService.searchMatrixColumnDataForSelect(
                matrixUuid,
                keywordColumn,
                valueField,
                textField,
                hiddenFieldList,
                filterList,
                keyword,
                currentPage,
                pageSize,
                needPage,
                defaultValue);
        if (fieldKeyUseUniqueIdentifier) {
            JSONArray dataList = resultObj.getJSONArray("dataList");
            if (CollectionUtils.isNotEmpty(dataList)) {
                for (int i = 0; i < dataList.size(); i++) {
                    JSONObject dataObj = dataList.getJSONObject(i);
                    List<String> keyList = new ArrayList<>(dataObj.keySet());
                    for (String key : keyList) {
                        if (!Objects.equals(key, "value") && !Objects.equals(key, "text")) {
                            String uniqueIdentifier = uuidToUniqueIdentifierMap.get(key);
                            if (StringUtils.isBlank(uniqueIdentifier)) {
                                throw new MatrixAttributeNotFoundException(matrixVo.getName(), uniqueIdentifier);
                            }
                            Object obj = dataObj.get(key);
                            dataObj.put(uniqueIdentifier, obj);
                            dataObj.remove(key);
                        }
                    }
                }
            }
        }
        return resultObj;
    }
}

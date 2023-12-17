package neatlogic.module.tenant.api.matrix;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.ValueTextVo;
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
import neatlogic.framework.util.TableResultUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class MatrixColumnDataSearchForBatchApi extends PrivateApiComponentBase {

    @Resource
    private MatrixMapper matrixMapper;

    @Override
    public String getName() {
        return "nmtam.matrixcolumndatasearchforbatchapi.getname";
    }


    @Override
    public boolean disableReturnCircularReferenceDetect() {
        return true;
    }

    @Input({
            @Param(name = "searchParamList", type = ApiParamType.JSONARRAY, isRequired = true, minSize = 1, desc = "nmtam.matrixcolumndatasearchforbatchapi.input.param.desc.searchparamlist")
    })
    @Output({
            @Param(name = "tbodyList", type = ApiParamType.JSONARRAY, desc = "common.tbodylist")
    })
    @Description(desc = "nmtam.matrixcolumndatasearchforbatchapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        JSONArray resultList = new JSONArray();
        JSONArray searchParamList = paramObj.getJSONArray("searchParamList");
        for (int i = 0; i < searchParamList.size(); i++) {
            JSONObject searchParamObj = searchParamList.getJSONObject(i);
            if (MapUtils.isEmpty(searchParamObj)) {
                continue;
            }
            String matrixUuid = searchParamObj.getString("matrixUuid");
            if (StringUtils.isBlank(matrixUuid)) {
                throw new ParamNotExistsException("searchParamList[" + i + "].matrixUuid");
            }
            String valueField = searchParamObj.getString("valueField");
            if (StringUtils.isBlank(valueField)) {
                throw new ParamNotExistsException("searchParamList[" + i + "].valueField");
            }
            String textField = searchParamObj.getString("textField");
            if (StringUtils.isBlank(textField)) {
                throw new ParamNotExistsException("searchParamList[" + i + "].textField");
            }
            JSONArray defaultValue = searchParamObj.getJSONArray("defaultValue");
            if (CollectionUtils.isEmpty(defaultValue)) {
                throw new ParamNotExistsException("searchParamList[" + i + "].defaultValue");
            }
            MatrixVo matrixVo = MatrixPrivateDataSourceHandlerFactory.getMatrixVo(matrixUuid);
            if (matrixVo == null) {
                matrixVo = matrixMapper.getMatrixByUuid(matrixUuid);
                if (matrixVo == null) {
                    throw new MatrixNotFoundException(matrixUuid);
                }
            }
            IMatrixDataSourceHandler matrixDataSourceHandler = MatrixDataSourceHandlerFactory.getHandler(matrixVo.getType());
            if (matrixDataSourceHandler == null) {
                throw new MatrixDataSourceHandlerNotFoundException(matrixVo.getType());
            }

            List<MatrixAttributeVo> matrixAttributeList = matrixDataSourceHandler.getAttributeList(matrixVo);
            if (CollectionUtils.isEmpty(matrixAttributeList)) {
                continue;
            }
            Set<String> notNullColumnSet = new HashSet<>();
            List<String> attributeList = matrixAttributeList.stream().map(MatrixAttributeVo::getUuid).collect(Collectors.toList());
            if (!attributeList.contains(valueField)) {
                throw new MatrixAttributeNotFoundException(matrixVo.getName(), valueField);
            }
            notNullColumnSet.add(valueField);
            if (!attributeList.contains(textField)) {
                throw new MatrixAttributeNotFoundException(matrixVo.getName(), textField);
            }
            notNullColumnSet.add(textField);
            List<String> columnList = new ArrayList<>();
            columnList.add(valueField);
            columnList.add(textField);
            JSONObject resultObj = new JSONObject();
            resultObj.put("matrixUuid", matrixUuid);
            resultObj.put("valueField", valueField);
            resultObj.put("textField", textField);
            List<ValueTextVo> dataList = new ArrayList<>();
            List<String> textList = new ArrayList<>();
            List<MatrixDefaultValueFilterVo> defaultValueFilterList = new ArrayList<>();
            for (int j = 0; j < defaultValue.size(); j++) {
                Object defaultValueObject = defaultValue.get(j);
                if (defaultValueObject instanceof JSONObject) {
                    JSONObject defaultValueObj = (JSONObject) defaultValueObject;
                    String value = defaultValueObj.getString("value");
                    String text = defaultValueObj.getString("text");
                    dataList.add(new ValueTextVo(value, text));
                } else if (defaultValueObject instanceof String) {
                    String defaultValueStr = (String) defaultValueObject;
                    MatrixDefaultValueFilterVo matrixDefaultValueFilterVo = new MatrixDefaultValueFilterVo(
                            null,
                            new MatrixKeywordFilterVo(textField, SearchExpression.EQ.getExpression(), defaultValueStr)
                    );
                    defaultValueFilterList.add(matrixDefaultValueFilterVo);
                    textList.add(defaultValueStr);
                }
            }
            if (CollectionUtils.isNotEmpty(defaultValueFilterList)) {
                MatrixDataVo dataVo = new MatrixDataVo();
                dataVo.setMatrixUuid(matrixUuid);
                dataVo.setColumnList(columnList);
                dataVo.setNotNullColumnList(new ArrayList<>(notNullColumnSet));
                dataVo.setDefaultValueFilterList(defaultValueFilterList);
                List<Map<String, JSONObject>> list = matrixDataSourceHandler.searchTableDataNew(dataVo);
                for (String text : textList) {
                    for (Map<String, JSONObject> e : list) {
                        String textStr = null;
                        JSONObject textObj = e.get(textField);
                        if (MapUtils.isNotEmpty(textObj)) {
                            textStr = textObj.getString("text");
                        }
                        if (!Objects.equals(textStr, text)) {
                            continue;
                        }
                        String valueStr = null;
                        JSONObject valueObj = e.get(valueField);
                        if (MapUtils.isNotEmpty(valueObj)) {
                            valueStr = valueObj.getString("value");
                        }
                        dataList.add(new ValueTextVo(valueStr, textStr));
                        break;
                    }
                }
            }
            resultObj.put("dataList", dataList);
            resultList.add(resultObj);
        }
        return TableResultUtil.getResult(resultList);
    }

    @Override
    public String getToken() {
        return "matrix/column/data/search/forbatch";
    }
}

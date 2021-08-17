package codedriver.module.tenant.service.matrix;

import java.util.List;
import java.util.Map;

import codedriver.framework.exception.core.ApiRuntimeException;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.exception.util.FreemarkerTransformException;
import codedriver.framework.integration.dto.IntegrationResultVo;
import codedriver.framework.integration.dto.IntegrationVo;
import codedriver.framework.matrix.dto.MatrixAttributeVo;
import codedriver.framework.matrix.dto.MatrixDataVo;

public interface MatrixService {

	public List<MatrixAttributeVo> getExternalMatrixAttributeList(String matrixUuid, IntegrationVo integrationVo) throws FreemarkerTransformException;
    
    public List<Map<String, Object>> matrixTableDataValueHandle(List<MatrixAttributeVo> attributeVoList, List<Map<String, String>> valueList);
    
    public JSONObject matrixAttributeValueHandle(MatrixAttributeVo matrixAttributeVo, Object value);
    
    public JSONObject matrixAttributeValueHandle(Object value);
    
    public List<Map<String, String>> matrixAttributeValueKeyWordSearch(MatrixAttributeVo matrixAttributeVo, MatrixDataVo dataVo);
    
    public List<Map<String, JSONObject>> getExternalDataTbodyList(IntegrationResultVo resultVo, List<String> columnList, JSONObject resultObj);
    /**
     * 
    * @Time:2020年7月8日
    * @Description: 将arrayColumnList包含的属性值转成数组
    * @param arrayColumnList 需要将值转化成数组的属性集合
    * @param tbodyList 表格数据
    * @return void
     */
    public void arrayColumnDataConversion(List<String> arrayColumnList, JSONArray tbodyList);
    /**
     * 
    * @Time:2020年12月1日
    * @Description: 矩阵属性值合法性校验 
    * @param matrixAttributeVo
    * @param value
    * @return boolean
     */
    public boolean matrixAttributeValueVerify(MatrixAttributeVo matrixAttributeVo, String value);

    /**
     * 校验集成接口数据是否符合矩阵格式
     * @param integrationUuid 集成配置uuid
     * @throws ApiRuntimeException
     */
    public void validateMatrixExternalData(String integrationUuid) throws ApiRuntimeException;

    List<MatrixAttributeVo> buildView(String matrixUuid, String matrixName, String xml);
}

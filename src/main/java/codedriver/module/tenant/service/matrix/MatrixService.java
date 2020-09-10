package codedriver.module.tenant.service.matrix;

import java.util.List;
import java.util.Map;

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
    
    public JSONObject matrixAttributeValueHandle(MatrixAttributeVo processMatrixAttributeVo, Object value);
    
    public JSONObject matrixAttributeValueHandle(Object value);
    
    public List<Map<String, String>> matrixAttributeValueKeyWordSearch(MatrixAttributeVo processMatrixAttributeVo, MatrixDataVo dataVo);
    
    public List<Map<String, JSONObject>> getExternalDataTbodyList(IntegrationResultVo resultVo, List<String> columnList, int pageSize, JSONObject resultObj);
    /**
     * 
    * @Time:2020年7月8日
    * @Description: 将arrayColumnList包含的属性值转成数组
    * @param arrayColumnList 需要将值转化成数组的属性集合
    * @param tbodyList 表格数据
    * @return void
     */
    public void arrayColumnDataConversion(List<String> arrayColumnList, JSONArray tbodyList);

}

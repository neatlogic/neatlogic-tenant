package codedriver.module.tenant.api.apiaudit;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.restful.core.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.ExcelField;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateBinaryStreamApiComponentBase;
import codedriver.framework.restful.dto.ApiAuditVo;
import codedriver.framework.util.ExcelUtil;
import codedriver.module.tenant.service.apiaudit.ApiAuditService;

/**
 * 操作审计导出接口
 * 基本思路：
 * 1、利用反射获取VO中标识为可导出的字段
 * 2、利用反射筛选VO中的getter方法
 * 3、遍历apiAuditVoList，截取每个getter方法中的属性名，与fieldMapList中的key匹配
 * 如果匹配上，则调用当前getter获取对应属性值，并且放置在一个map中，将每一个这样的map存放在
 * List中，最后用此List作为导出Excel的结果集
 */

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class ApiAuditExportApi extends PrivateBinaryStreamApiComponentBase {

	@Autowired
	private ApiAuditService apiAuditService;

	@Override
	public String getToken() {
		return "apiaudit/export";
	}

	@Override
	public String getName() {
		return "导出操作审计";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
			@Param(name = "moduleGroup", type = ApiParamType.STRING, desc = "API所属模块"),
			@Param(name = "funcId", type = ApiParamType.STRING, desc = "API所属功能"),
			@Param(name = "userUuid", type = ApiParamType.STRING, desc = "访问者UUID"),
			@Param(name = "operationType", type = ApiParamType.STRING, desc = "操作类型"),
			@Param(name = "timeRange", type = ApiParamType.INTEGER, desc="时间跨度"),
			@Param(name = "timeUnit", type = ApiParamType.STRING, desc="时间跨度单位(day|month)"),
			@Param(name = "orderType", type = ApiParamType.STRING, desc="排序类型(asc|desc)"),
			@Param(name = "startTime", type = ApiParamType.LONG, desc="开始时间"),
			@Param(name = "endTime", type = ApiParamType.LONG, desc="结束时间"),
			@Param(name = "keyword", type = ApiParamType.STRING, desc="搜索关键词")
	})
	@Description(desc = "导出操作审计")
	@Override
	public Object myDoService(JSONObject jsonObj, HttpServletRequest request, HttpServletResponse response) throws Exception {
		ApiAuditVo apiAuditVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<ApiAuditVo>() {});
		List<ApiAuditVo> apiAuditVoList = apiAuditService.searchApiAuditForExport(apiAuditVo);

		if(CollectionUtils.isNotEmpty(apiAuditVoList)){
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			/**
			 * 利用反射把ApiAuditVo中标识为excel列的字段保存在map中
			 */
			Field[] declaredFields = ApiAuditVo.class.getDeclaredFields();
			List<Map<String, String>> fieldMapList = new ArrayList<>();
			for(int i = 0;i < declaredFields.length;i++){
				Map<String, String> map = new LinkedHashMap<>();
				ExcelField entityField = declaredFields[i].getAnnotation(ExcelField.class);
				if(entityField != null){
					map.put(declaredFields[i].getName(),entityField.name());
					fieldMapList.add(map);
				}
			}
			/**
			 * 利用筛选出的getter方法与fieldMapList中的字段匹配
			 * 匹配上之后用反射获取对应getter的调用结果
			 * 把与当前getter对应的字段与调用结果保存在map中
			 */
			List<Map<String, Object>> resultList = new ArrayList<>();

			for(ApiAuditVo vo : apiAuditVoList){
				Map<String, Object> map = new LinkedHashMap<>();
				for(Map<String, String> fieldMap : fieldMapList){
					Set<String> fieldSet = fieldMap.keySet();
					Collection<String> fieldNameSet = fieldMap.values();
					Object[] fieldNameArray = fieldNameSet.toArray();
					Object[] fieldArray = fieldSet.toArray();
					String field = fieldArray[0].toString().substring(0, 1).toUpperCase() + fieldArray[0].toString().substring(1);
					Method method = ApiAuditVo.class.getDeclaredMethod("get" + field);
					Object result = method.invoke(vo);
					if(result instanceof Date){
						result = sdf.format(result);
					}
					map.put(fieldNameArray[0].toString(),result);
				}
				resultList.add(map);
			}


			List<String> headerList = new ArrayList<>();
			List<String> columnList = new ArrayList<>();
			if(CollectionUtils.isNotEmpty(resultList)){
				Map<String, Object> map = resultList.get(0);
				for(String key : map.keySet()){
					headerList.add(key);
					columnList.add(key);
				}
				SXSSFWorkbook workbook = new SXSSFWorkbook();

				ExcelUtil.exportData(workbook,headerList,columnList,resultList,new Integer(30),0);
				String fileNameEncode = "操作审计.xlsx";
				Boolean flag = request.getHeader("User-Agent").indexOf("Gecko") > 0;
				if (request.getHeader("User-Agent").toLowerCase().indexOf("msie") > 0 || flag) {
					fileNameEncode = URLEncoder.encode(fileNameEncode, "UTF-8");// IE浏览器
				} else {
					fileNameEncode = new String(fileNameEncode.replace(" ", "").getBytes(StandardCharsets.UTF_8), "ISO8859-1");
				}
				response.setContentType("application/vnd.ms-excel;charset=utf-8");
				response.setHeader("Content-Disposition", "attachment;fileName=\"" + fileNameEncode + "\"");
				try (OutputStream os = response.getOutputStream()){
					workbook.write(os);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return null;
	}
}

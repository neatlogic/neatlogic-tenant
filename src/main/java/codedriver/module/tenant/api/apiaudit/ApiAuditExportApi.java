package codedriver.module.tenant.api.apiaudit;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.BinaryStreamApiComponentBase;
import codedriver.framework.restful.dto.ApiAuditVo;
import codedriver.framework.util.ExcelUtil;
import codedriver.module.tenant.service.apiaudit.ApiAuditService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 操作审计导出接口
 */

@Service
public class ApiAuditExportApi extends BinaryStreamApiComponentBase {

	@Autowired
	private ApiAuditService apiAuditService;

	@Override
	public String getToken() {
		return "apiaudit/export";
	}

	@Override
	public String getName() {
		return "操作审计导出接口";
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
	@Description(desc = "操作审计导出接口")
	@Override
	public Object myDoService(JSONObject jsonObj, HttpServletRequest request, HttpServletResponse response) throws Exception {
		ApiAuditVo apiAuditVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<ApiAuditVo>() {});

		List<Map<String, String>> apiAuditMapList = apiAuditService.searchApiAuditMapList(apiAuditVo);
		HSSFWorkbook workbook = null;
		//TODO 表头待定|自定义表头
		List<String> headerList = new ArrayList<>();
		List<String> columnList = new ArrayList<>();
		Map<String, String> map = apiAuditMapList.get(0);
		for(String key : map.keySet()){
			headerList.add(key);
			columnList.add(key);
		}
		List<Map<String, String>> dataMapList = apiAuditMapList;
		workbook = ExcelUtil.createExcel(workbook, headerList, columnList, null, dataMapList);

		if(workbook != null){
			throw new RuntimeException("导出操作审计错误");
		}

		String fileNameEncode = "apiAudits.xls";
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

		return null;
	}
}

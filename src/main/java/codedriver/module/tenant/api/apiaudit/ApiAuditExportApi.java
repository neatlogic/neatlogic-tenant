package codedriver.module.tenant.api.apiaudit;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateBinaryStreamApiComponentBase;
import codedriver.framework.restful.dto.ApiAuditVo;
import codedriver.module.tenant.service.apiaudit.ApiAuditService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

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
		String fileNameEncode = "操作审计.csv";
		Boolean flag = request.getHeader("User-Agent").indexOf("Gecko") > 0;
		if (request.getHeader("User-Agent").toLowerCase().indexOf("msie") > 0 || flag) {
			fileNameEncode = URLEncoder.encode(fileNameEncode, "UTF-8");// IE浏览器
		} else {
			fileNameEncode = new String(fileNameEncode.replace(" ", "").getBytes(StandardCharsets.UTF_8), "ISO8859-1");
		}
		response.setContentType("application/text;charset=GBK");
		response.setHeader("Content-Disposition", "attachment;fileName=\"" + fileNameEncode + "\"");
		ServletOutputStream os = response.getOutputStream();
		ApiAuditVo apiAuditVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<ApiAuditVo>() {});
		apiAuditService.exportApiAudit(apiAuditVo,os);
		os.close();
		return null;
	}
}

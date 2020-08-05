package codedriver.module.tenant.api.apimanage;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.BinaryStreamApiComponentBase;
import codedriver.module.tenant.service.apiaudit.ApiAuditService;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class ApiAuditDetailDownLoadApi extends BinaryStreamApiComponentBase {

	@Autowired
	private ApiAuditService apiAuditService;

	@Override
	public String getToken() {
		return "apimanage/audit/detail/download";
	}

	@Override
	public String getName() {
		return "下载接口调用记录";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({@Param(name = "filePath", type = ApiParamType.STRING, desc = "调用记录文件路径", isRequired = true)})
	@Output({})
	@Description(desc = "下载接口调用记录")
	@Override
	public Object myDoService(JSONObject jsonObj, HttpServletRequest request, HttpServletResponse response) throws Exception {

		String filePath = jsonObj.getString("filePath");
		System.out.println("读取文件前：" + System.currentTimeMillis());
		String result = apiAuditService.getAuditContentOnFile(filePath);
		System.out.println("读取文件后：" + System.currentTimeMillis());

		if(StringUtils.isNotBlank(result)){
			String fileNameEncode = "API_AUDIT.log";
			Boolean flag = request.getHeader("User-Agent").indexOf("Gecko") > 0;
			if (request.getHeader("User-Agent").toLowerCase().indexOf("msie") > 0 || flag) {
				fileNameEncode = URLEncoder.encode(fileNameEncode, "UTF-8");// IE浏览器
			} else {
				fileNameEncode = new String(fileNameEncode.getBytes(StandardCharsets.UTF_8), "ISO8859-1");
			}
			response.setContentType("aplication/x-msdownload;charset=utf-8");
			response.setHeader("Content-Disposition", "attachment;fileName=\"" + fileNameEncode + "\"");
			try (OutputStream os = response.getOutputStream()){
				os.write(result.getBytes(StandardCharsets.UTF_8));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return null;
	}

}

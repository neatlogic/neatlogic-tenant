package codedriver.module.tenant.api.apimanage;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.util.FileUtil;
import codedriver.framework.exception.file.FilePathIllegalException;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.BinaryStreamApiComponentBase;
import codedriver.framework.restful.dto.ApiAuditVo;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class ApiAuditDetailDownLoadApi extends BinaryStreamApiComponentBase {

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

		if(!filePath.contains("?") || !filePath.contains("&") || !filePath.contains("=")){
			throw new FilePathIllegalException("文件路径格式错误");
		}

		String path = filePath.split("\\?")[0];
		String[] indexs = filePath.split("\\?")[1].split("&");
		Long startIndex = Long.parseLong(indexs[0].split("=")[1]);
		Long offset = Long.parseLong(indexs[1].split("=")[1]);

		InputStream in = null;
		try {
			in = FileUtil.getData(path);
			if(in != null){
				in.skip(startIndex);

				String fileNameEncode = "API_AUDIT.log";
				Boolean flag = request.getHeader("User-Agent").indexOf("Gecko") > 0;
				if (request.getHeader("User-Agent").toLowerCase().indexOf("msie") > 0 || flag) {
					fileNameEncode = URLEncoder.encode(fileNameEncode, "UTF-8");// IE浏览器
				} else {
					fileNameEncode = new String(fileNameEncode.getBytes(StandardCharsets.UTF_8), "ISO8859-1");
				}
				response.setContentType("aplication/x-msdownload;charset=utf-8");
				response.setHeader("Content-Disposition", "attachment;fileName=\"" + fileNameEncode + "\"");
				OutputStream os = response.getOutputStream();

				byte[] buff = new byte[(int)ApiAuditVo.maxFileSize];
				int len = 0;
				long endPoint = 0;
				while((len = in.read(buff)) != -1){
					/**
					 * endPoint用来记录累计读取到的字节数
					 * 如果大于偏移量，说明实际读到的数据超过了需要的数据
					 * 那么就需要减掉多读出来的数据
					 */
					endPoint += len;
					if(endPoint > offset){
						len = (int)(len - (endPoint - offset));
					}
					os.write(buff,0,len);
					os.flush();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if(in != null){
				in.close();
			}
		}

		return null;
	}

}

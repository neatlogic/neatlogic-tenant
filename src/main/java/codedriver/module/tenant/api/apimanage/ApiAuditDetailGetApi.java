package codedriver.module.tenant.api.apimanage;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.util.FileUtil;
import codedriver.framework.file.dao.mapper.FileMapper;
import codedriver.framework.file.dto.FileVo;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.ApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.charset.Charset;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class ApiAuditDetailGetApi extends ApiComponentBase {

	@Autowired
	private FileMapper fileMapper;
	
	@Override
	public String getToken() {
		return "apimanage/audit/detail/get";
	}

	@Override
	public String getName() {
		return "获取接口调用记录";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({ @Param(name = "fileId", type = ApiParamType.LONG, desc = "调用记录文件ID", isRequired = true),
			@Param(name = "type", type = ApiParamType.STRING, desc = "类型(param|result|error)", isRequired = true)
	})
	@Output({})
	@Description(desc = "获取接口调用记录")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JSONObject result = null;
		Long fileId = jsonObj.getLong("fileId");
		String type = jsonObj.getString("type");
		FileVo fileVo = fileMapper.getFileById(fileId);
		if(fileVo != null){
			String path = fileVo.getPath();
			InputStream stream = FileUtil.getData(path);
			if(stream != null){
				String data = IOUtils.toString(stream, Charset.forName("UTF-8"));
				JSONObject jsonObject = JSONObject.parseObject(data);
				result = jsonObject.getJSONObject(type);
			}
		}

		return result;
	}

}

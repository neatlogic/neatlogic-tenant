/*Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

package neatlogic.module.tenant.api.apiaudit;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateBinaryStreamApiComponentBase;
import neatlogic.framework.restful.dto.ApiAuditVo;
import neatlogic.module.tenant.service.apiaudit.ApiAuditService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
		response.setHeader("Content-Disposition", " attachment; filename=\"" + fileNameEncode + "\"");
		ServletOutputStream os = response.getOutputStream();
		if(os != null){
			ApiAuditVo apiAuditVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<ApiAuditVo>() {});
			apiAuditService.exportApiAudit(apiAuditVo,os);
			os.close();
		}
		return null;
	}
}

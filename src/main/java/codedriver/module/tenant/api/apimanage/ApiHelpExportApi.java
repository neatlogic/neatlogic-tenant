/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.apimanage;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.auth.label.INTERFACE_MODIFY;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.exception.type.ApiNotFoundException;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.IApiComponent;
import codedriver.framework.restful.core.IBinaryStreamApiComponent;
import codedriver.framework.restful.core.IJsonStreamApiComponent;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentFactory;
import codedriver.framework.restful.core.privateapi.PrivateBinaryStreamApiComponentBase;
import codedriver.framework.restful.dao.mapper.ApiMapper;
import codedriver.framework.restful.dto.ApiVo;
import codedriver.framework.restful.enums.ApiType;
import codedriver.framework.util.ExportUtil;
import codedriver.framework.util.FreemarkerUtil;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Service
@AuthAction(action = INTERFACE_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ApiHelpExportApi extends PrivateBinaryStreamApiComponentBase {
	static String template;
	static Logger logger = LoggerFactory.getLogger(ApiHelpExportApi.class);

	@Autowired
	private ApiMapper apiMapper;
	
	@Override
	public String getToken() {
		return "api/help/export";
	}

	@Override
	public String getName() {
		return "导出接口帮助";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "token", type = ApiParamType.STRING, desc = "接口token")
	})
	@Output({
		@Param(name = "Return", explode = ApiVo.class, isRequired = true, desc = "接口配置信息")
	})
	@Description(desc = "导出接口帮助接口")
	@Override
	public Object myDoService(JSONObject jsonObj, HttpServletRequest request, HttpServletResponse response) throws Exception {
		JSONObject helpJson = new JSONObject();
		try {
			InputStreamReader reader = new InputStreamReader(Objects.requireNonNull(ApiHelpExportApi.class.getClassLoader()
					.getResourceAsStream("template/api-help-template.ftl")), StandardCharsets.UTF_8.name());
			template = IOUtils.toString(reader);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		String token = jsonObj.getString("token");
		ApiVo interfaceVo = PrivateApiComponentFactory.getApiByToken(token);
		if (interfaceVo == null) {
			interfaceVo = apiMapper.getApiByToken(token);
			if (interfaceVo == null || !interfaceVo.getIsActive().equals(1)) {
				throw new ApiNotFoundException(token);
			}
		}
		if (interfaceVo.getType().equals(ApiType.OBJECT.getValue())) {
			IApiComponent restComponent = PrivateApiComponentFactory.getInstance(interfaceVo.getHandler());
			helpJson = restComponent.help();
		}else if (interfaceVo.getType().equals(ApiType.STREAM.getValue())) {
			IJsonStreamApiComponent restComponent = PrivateApiComponentFactory.getStreamInstance(interfaceVo.getHandler());
			helpJson = restComponent.help();
		}else if (interfaceVo.getType().equals(ApiType.BINARY.getValue())) {
			IBinaryStreamApiComponent restComponent = PrivateApiComponentFactory.getBinaryInstance(interfaceVo.getHandler());
			helpJson = restComponent.help();
		}
		String content = FreemarkerUtil.transform(helpJson, template);
		try (OutputStream os = response.getOutputStream()) {
			response.setCharacterEncoding("utf-8");
			response.setContentType("application/pdf");
			response.setHeader("Content-Disposition",
					" attachment; filename=\"" + URLEncoder.encode("werw", StandardCharsets.UTF_8.name()) + ".pdf\"");
			ExportUtil.savePdf(content, os, false);
			os.flush();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	static {

	}

}
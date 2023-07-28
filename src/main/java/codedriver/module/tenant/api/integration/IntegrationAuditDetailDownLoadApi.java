/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.integration;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.auth.label.INTEGRATION_MODIFY;
import codedriver.framework.common.config.Config;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.crossover.CrossoverServiceFactory;
import codedriver.framework.crossover.IFileCrossoverService;
import codedriver.framework.exception.file.FilePathIllegalException;
import codedriver.framework.file.dto.AuditFilePathVo;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateBinaryStreamApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

@Component
@AuthAction(action = INTEGRATION_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class IntegrationAuditDetailDownLoadApi extends PrivateBinaryStreamApiComponentBase {

	@Override
	public String getToken() {
		return "integration/audit/detail/download";
	}

	@Override
	public String getName() {
		return "下载集成管理调用记录";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
			@Param(name = "filePath", type = ApiParamType.STRING, desc = "文件路径", isRequired = true)
	})
	@Output({})
	@Description(desc = "下载集成管理调用记录")
	@Override
	public Object myDoService(JSONObject paramObj, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String filePath = paramObj.getString("filePath");
		if (!filePath.contains("integrationaudit")) {
			throw new FilePathIllegalException(filePath);
		}
		AuditFilePathVo auditFilePathVo = new AuditFilePathVo(filePath);
		IFileCrossoverService fileCrossoverService = CrossoverServiceFactory.getApi(IFileCrossoverService.class);
		if (Objects.equals(auditFilePathVo.getServerId(), Config.SCHEDULE_SERVER_ID)) {
			fileCrossoverService.downloadLocalFile(auditFilePathVo.getPath(), auditFilePathVo.getStartIndex(), auditFilePathVo.getOffset(), response);
		} else {
			fileCrossoverService.downloadRemoteFile(paramObj, auditFilePathVo.getServerId(), request, response);
		}
		return null;
	}

}

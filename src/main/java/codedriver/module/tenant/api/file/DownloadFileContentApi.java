package codedriver.module.tenant.api.file;

import codedriver.framework.common.config.Config;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.crossover.CrossoverServiceFactory;
import codedriver.framework.crossover.IFileCrossoverService;
import codedriver.framework.file.dto.AuditFilePathVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateBinaryStreamApiComponentBase;
import com.alibaba.fastjson.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

@Deprecated
@OperationType(type = OperationTypeEnum.SEARCH)
public class DownloadFileContentApi extends PrivateBinaryStreamApiComponentBase {

    @Override
    public String getName() {
        return "下载文件内容";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "filePath", type = ApiParamType.STRING, isRequired = true, desc = "文件路径")
    })
    @Description(desc = "下载文件内容")
    @Override
    public Object myDoService(JSONObject paramObj, HttpServletRequest request, HttpServletResponse response) throws Exception {
        String filePath = paramObj.getString("filePath");
        AuditFilePathVo auditFilePathVo = new AuditFilePathVo(filePath);
        IFileCrossoverService fileCrossoverService = CrossoverServiceFactory.getApi(IFileCrossoverService.class);
        if (Objects.equals(auditFilePathVo.getServerId(), Config.SCHEDULE_SERVER_ID)) {
            fileCrossoverService.downloadLocalFile(auditFilePathVo.getPath(), auditFilePathVo.getStartIndex(), auditFilePathVo.getOffset(), response);
        } else {
            fileCrossoverService.downloadRemoteFile(paramObj, auditFilePathVo.getServerId(), request, response);
        }
        return null;
    }

    @Override
    public String getToken() {
        return "file/content/download";
    }

}

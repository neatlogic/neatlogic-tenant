package codedriver.module.tenant.api.file;

import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.common.config.Config;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.util.FileUtil;
import codedriver.framework.exception.file.FilePathIllegalException;
import codedriver.framework.heartbeat.dao.mapper.ServerMapper;
import codedriver.framework.heartbeat.dto.ServerClusterVo;
import codedriver.framework.integration.authentication.enums.AuthenticateType;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateBinaryStreamApiComponentBase;
import codedriver.framework.util.HttpRequestUtil;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class DownloadFileContentApi extends PrivateBinaryStreamApiComponentBase {

    private final Logger logger = LoggerFactory.getLogger(DownloadFileContentApi.class);

    @Resource
    private ServerMapper serverMapper;

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
        Map<String, String> paramMap = new HashMap<>();
        String filePath = paramObj.getString("filePath");
        String[] split = filePath.split("\\?");
        String path = split[0];
        if (split.length >= 2) {
            String[] paramArray = split[1].split("&");
            for (String param : paramArray) {
                if (param.contains("=")) {
                    String[] paramKeyValue = param.split("=");
                    paramMap.put(paramKeyValue[0], paramKeyValue[1]);
                }
            }
        }
        int startIndex = 0;
        int offset = 0;
        int serverId = 0;
        String startIndexStr = paramMap.get("startIndex");
        if (StringUtils.isNotBlank(startIndexStr)) {
            startIndex = Integer.parseInt(startIndexStr);
        }
        String offsetStr = paramMap.get("offset");
        if (StringUtils.isNotBlank(offsetStr)) {
            offset = Integer.parseInt(offsetStr);
        }
        String serverIdStr = paramMap.get("serverId");
        if (StringUtils.isNotBlank(serverIdStr)) {
            serverId = Integer.parseInt(serverIdStr);
        }
        if (Objects.equals(serverId, Config.SCHEDULE_SERVER_ID)) {
            downloadLocalFile(path, startIndex, offset, response);
        } else {
            downloadRemoteFile(paramObj, serverId, request, response);
        }
        return null;
    }

    @Override
    public String getToken() {
        return "file/content/download";
    }

    /**
     * 下载当前服务器文件
     * @param path
     * @param startIndex
     * @param offset
     * @param response
     */
    private void downloadLocalFile(String path, int startIndex, int offset, HttpServletResponse response) {
        String dataHome = Config.DATA_HOME() + TenantContext.get().getTenantUuid();
        String prefix = "${home}";
        if (path.startsWith(prefix)) {
            path = path.substring(prefix.length());
            path = dataHome + path;
        }else{
            throw new FilePathIllegalException(path);
        }
        if (!path.startsWith("file:")) {
            path = "file:" + path;
        }
        try (InputStream in = FileUtil.getData(path)) {
            if (in != null) {
                in.skip(startIndex);
                String fileNameEncode = codedriver.framework.util.FileUtil.getEncodedFileName("AUDIT_DETAIL.log");
                response.setContentType("application/x-msdownload;charset=utf-8");
                response.setHeader("Content-Disposition", " attachment; filename=\"" + fileNameEncode + "\"");
                OutputStream os = response.getOutputStream();

                byte[] buff = new byte[1024];
                int len;
                int endPoint = 0;
                while ((len = in.read(buff)) != -1) {
                    endPoint += len;
                    if (endPoint >= offset) {
                        len = (len - (endPoint - offset));
                    }
                    os.write(buff, 0, len);
                    os.flush();
                    if (endPoint >= offset) {
                        break;
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * 下载另一个服务器文件
     * @param paramObj
     * @param serverId
     * @param request
     * @param response
     * @throws IOException
     */
    private void downloadRemoteFile(JSONObject paramObj, Integer serverId, HttpServletRequest request, HttpServletResponse response) throws IOException {
        String host = null;
        TenantContext.get().setUseDefaultDatasource(true);
        ServerClusterVo serverClusterVo = serverMapper.getServerByServerId(serverId);
        if (serverClusterVo != null) {
            host = serverClusterVo.getHost();
        }
        TenantContext.get().setUseDefaultDatasource(false);
        if (StringUtils.isBlank(host)) {
            return;
        }
        String url = host + request.getRequestURI();
        HttpRequestUtil httpRequestUtil = HttpRequestUtil.download(url, "POST", response.getOutputStream()).setPayload(paramObj.toJSONString()).setAuthType(AuthenticateType.BUILDIN).sendRequest();
        String error = httpRequestUtil.getError();
        if (StringUtils.isNotBlank(error)) {
            throw new RuntimeException(error);
        }
    }
}

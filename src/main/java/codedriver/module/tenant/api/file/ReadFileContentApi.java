/*
 * Copyright(c) 2021 TechSureCo.,Ltd.AllRightsReserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.file;

import codedriver.framework.asynchronization.threadlocal.RequestContext;
import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.common.config.Config;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.util.FileUtil;
import codedriver.framework.heartbeat.dao.mapper.ServerMapper;
import codedriver.framework.heartbeat.dto.ServerClusterVo;
import codedriver.framework.integration.authentication.enums.AuthenticateType;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.util.HttpRequestUtil;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class ReadFileContentApi extends PrivateApiComponentBase {

    private final Logger logger = LoggerFactory.getLogger(ReadFileContentApi.class);

    @Resource
    private ServerMapper serverMapper;

    /*查看审计记录时可显示的最大字节数，超过此数需要下载文件后查看*/
    public final static int MAX_FILE_SIZE = 1024 * 1024;

    @Override
    public String getToken() {
        return "file/content/read";
    }

    @Override
    public String getName() {
        return "读取文件内容";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "path", type = ApiParamType.STRING, isRequired = true, desc = "文件路径"),
            @Param(name = "startIndex", type = ApiParamType.INTEGER, isRequired = true, desc = "开始下标"),
            @Param(name = "offset", type = ApiParamType.INTEGER, isRequired = true, desc = "偏移量"),
            @Param(name = "serverId", type = ApiParamType.INTEGER, isRequired = true, desc = "服务器ID"),
    })
    @Output({
            @Param(name = "content", type = ApiParamType.STRING, desc = "内容"),
            @Param(name = "hasMore", type = ApiParamType.BOOLEAN, desc = "是否还有更多内容")
    })
    @Description(desc = "读取文件内容")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        Integer serverId = paramObj.getInteger("serverId");
        if (Objects.equals(serverId, Config.SCHEDULE_SERVER_ID)) {
            return readLocalFile(paramObj);
        } else {
            return readRemoteFile(paramObj, serverId);
        }
    }

    /**
     * 读取服务器本地文件内容
     * @param paramObj 入参
     * @return 文件内容
     */
    private JSONObject readLocalFile(JSONObject paramObj) {
        String path = paramObj.getString("path");
        if (!path.startsWith("file:")) {
            path = "file:" + path;
        }
        Integer startIndex = paramObj.getInteger("startIndex");
        Integer offset = paramObj.getInteger("offset");
        JSONObject resultObj = new JSONObject();
        boolean hasMore = false;
        /*
         * 如果偏移量大于最大字节数限制，那么就只截取最大字节数长度的数据
         */
        if (offset > MAX_FILE_SIZE) {
            offset = MAX_FILE_SIZE;
            hasMore = true;
        }
        resultObj.put("hasMore", hasMore);
        try (InputStream in = FileUtil.getData(path)) {
            if (in != null) {
                in.skip(startIndex);
                byte[] buff = new byte[1024];
                StringBuilder sb = new StringBuilder();
                int len;
                int endPoint = 0;
                while ((len = in.read(buff)) != -1) {
                    endPoint += len;
                    if (endPoint >= offset) {
                        len = (len - (endPoint - offset));
                        sb.append(new String(buff, 0, len, StandardCharsets.UTF_8));
                        break;
                    } else {
                        sb.append(new String(buff, 0, len, StandardCharsets.UTF_8));
                    }
                }
                resultObj.put("content", sb.toString());
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return resultObj;
    }

    /**
     * 读取其他服务器文件内容
     * @param paramObj 入参
     * @param serverId 服务器ID
     * @return 文件内容
     */
    private JSONObject readRemoteFile(JSONObject paramObj, Integer serverId) {
        JSONObject resultObj = new JSONObject();
        String host = null;
        TenantContext.get().setUseDefaultDatasource(true);
        ServerClusterVo serverClusterVo = serverMapper.getServerByServerId(serverId);
        if (serverClusterVo != null) {
            host = serverClusterVo.getHost();
        }
        TenantContext.get().setUseDefaultDatasource(false);
        if (StringUtils.isBlank(host)) {
            return resultObj;
        }
        HttpServletRequest request = RequestContext.get().getRequest();
        String url = host + request.getRequestURI();
        HttpRequestUtil httpRequestUtil = HttpRequestUtil.post(url)
                .setPayload(paramObj.toJSONString())
                .setAuthType(AuthenticateType.BUILDIN)
                .setConnectTimeout(5000)
                .setReadTimeout(5000)
                .sendRequest();
        String error = httpRequestUtil.getError();
        if(StringUtils.isNotBlank(error)){
            throw new RuntimeException(error);
        }
        JSONObject resultJson = httpRequestUtil.getResultJson();
        if (MapUtils.isNotEmpty(resultJson)) {
            String status = resultJson.getString("Status");
            if (!"OK".equals(status)) {
                throw new RuntimeException(resultJson.getString("Message"));
            }
            resultObj = resultJson.getJSONObject("Return");
        }
        return resultObj;
    }
}

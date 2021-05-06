/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.file;

import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.auth.label.FRAMEWOKR_BASE;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.constvalue.CacheControlType;
import codedriver.framework.common.util.FileUtil;
import codedriver.framework.exception.file.FileAccessDeniedException;
import codedriver.framework.exception.file.FileNotFoundException;
import codedriver.framework.exception.file.FileTypeHandlerNotFoundException;
import codedriver.framework.exception.user.NoTenantException;
import codedriver.framework.file.core.FileTypeHandlerFactory;
import codedriver.framework.file.core.IFileTypeHandler;
import codedriver.framework.file.dao.mapper.FileMapper;
import codedriver.framework.file.dto.FileVo;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateBinaryStreamApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@AuthAction(action = FRAMEWOKR_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class FileDownloadApi extends PrivateBinaryStreamApiComponentBase {

    @Autowired
    private FileMapper fileMapper;

    @Override
    public String getToken() {
        return "file/download";
    }

    @Override
    public String getName() {
        return "附件下载接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @CacheControl(cacheControlType = CacheControlType.MAXAGE, maxAge = 30000)
    @Input({@Param(name = "id", type = ApiParamType.LONG, desc = "附件id", isRequired = true)})
    @Description(desc = "附件下载接口")
    @Override
    public Object myDoService(JSONObject paramObj, HttpServletRequest request, HttpServletResponse response) throws Exception {
        Long id = paramObj.getLong("id");
        FileVo fileVo = fileMapper.getFileById(id);
        String tenantUuid = TenantContext.get().getTenantUuid();
        if (StringUtils.isBlank(tenantUuid)) {
            throw new NoTenantException();
        }
        if (fileVo != null) {
            IFileTypeHandler fileTypeHandler = FileTypeHandlerFactory.getHandler(fileVo.getType());
            if (fileTypeHandler != null) {
                if (fileTypeHandler.valid(UserContext.get().getUserUuid(), fileVo, paramObj)) {
                    ServletOutputStream os;
                    InputStream in;
                    in = FileUtil.getData(fileVo.getPath());
                    if (in != null) {
                        String fileNameEncode;
                        boolean flag = request.getHeader("User-Agent").indexOf("Gecko") > 0;
                        if (request.getHeader("User-Agent").toLowerCase().indexOf("msie") > 0 || flag) {
                            fileNameEncode = URLEncoder.encode(fileVo.getName(), "UTF-8");// IE浏览器
                            /** chrome、firefox、edge浏览器下载文件时，文件名包含~@#$&+=;这八个英文字符时会变成乱码_%40%23%24%26%2B%3D%3B，下面是对@#$&+=;这七个字符做特殊处理，对于~这个字符还是会出现乱码，暂无法处理 **/
                            fileNameEncode = fileNameEncode.replace("%40", "@");
                            fileNameEncode = fileNameEncode.replace("%23", "#");
                            fileNameEncode = fileNameEncode.replace("%24", "$");
                            fileNameEncode = fileNameEncode.replace("%26", "&");
                            fileNameEncode = fileNameEncode.replace("%2B", "+");
                            fileNameEncode = fileNameEncode.replace("%3D", "=");
                            fileNameEncode = fileNameEncode.replace("%3B", ";");
                        } else {
                            fileNameEncode = new String(fileVo.getName().replace(" ", "").getBytes(StandardCharsets.UTF_8), "ISO8859-1");
                        }

                        if (StringUtils.isBlank(fileVo.getContentType())) {
                            response.setContentType("application/octet-stream");
                        } else {
                            response.setContentType(fileVo.getContentType());
                        }
                        response.setHeader("Content-Disposition", "attachment;filename=\"" + fileNameEncode + "\"");
                        os = response.getOutputStream();
                        IOUtils.copyLarge(in, os);
                        if (os != null) {
                            os.flush();
                            os.close();
                        }
                        in.close();
                    }
                } else {
                    throw new FileAccessDeniedException(fileVo.getName(), "下载");
                }
            } else {
                throw new FileTypeHandlerNotFoundException(fileVo.getType());
            }
        } else {
            throw new FileNotFoundException(id);
        }
        return null;
    }
}

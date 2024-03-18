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

package neatlogic.module.tenant.api.file;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.constvalue.CacheControlType;
import neatlogic.framework.common.util.FileUtil;
import neatlogic.framework.exception.file.FileAccessDeniedException;
import neatlogic.framework.exception.file.FileNotFoundException;
import neatlogic.framework.exception.file.FileTypeHandlerNotFoundException;
import neatlogic.framework.exception.user.NoTenantException;
import neatlogic.framework.file.core.FileOperationType;
import neatlogic.framework.file.core.FileTypeHandlerFactory;
import neatlogic.framework.file.core.IFileTypeHandler;
import neatlogic.framework.file.dao.mapper.FileMapper;
import neatlogic.framework.file.dto.FileVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateBinaryStreamApiComponentBase;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.math.BigDecimal;

@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class DownloadFileApi extends PrivateBinaryStreamApiComponentBase {

    @Resource
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
    @Input({@Param(name = "id", type = ApiParamType.LONG, desc = "附件id", isRequired = true),
            @Param(name = "lastModified", type = ApiParamType.DOUBLE, desc = "最后修改时间（秒，支持小数位）")})
    @Description(desc = "附件下载接口")
    @Override
    public Object myDoService(JSONObject paramObj, HttpServletRequest request, HttpServletResponse response) throws Exception {
        Long id = paramObj.getLong("id");
        long lastModifiedLong = 0L;
        boolean isNeedDownLoad = false;
        FileVo fileVo = fileMapper.getFileById(id);
        String tenantUuid = TenantContext.get().getTenantUuid();
        if (StringUtils.isBlank(tenantUuid)) {
            throw new NoTenantException();
        }
        if (fileVo != null) {
            if (paramObj.getDouble("lastModified") != null) {
                BigDecimal lastModifiedDec = new BigDecimal(Double.toString(paramObj.getDouble("lastModified")));
                lastModifiedLong = lastModifiedDec.multiply(new BigDecimal("1000")).longValue();
            }
            if (lastModifiedLong == 0L || lastModifiedLong < fileVo.getUploadTime().getTime()) {
                String userUuid = UserContext.get().getUserUuid();
                IFileTypeHandler fileTypeHandler = FileTypeHandlerFactory.getHandler(fileVo.getType());
                if (fileTypeHandler != null) {
                    if (StringUtils.equals(userUuid, fileVo.getUserUuid()) || fileTypeHandler.valid(userUuid, fileVo, paramObj)) {
                        ServletOutputStream os;
                        InputStream in;
                        in = FileUtil.getData(fileVo.getPath());
                        if (in != null) {
                            if (StringUtils.isBlank(fileVo.getContentType())) {
                                response.setContentType("application/octet-stream");
                            } else {
                                response.setContentType(fileVo.getContentType());
                            }
                            response.setHeader("Content-Disposition", " attachment; filename=\"" + neatlogic.framework.util.FileUtil.getEncodedFileName(fileVo.getName()) + "\"");
                            os = response.getOutputStream();
                            IOUtils.copyLarge(in, os);
                            os.flush();
                            os.close();
                            in.close();
                        }
                        isNeedDownLoad = true;
                    } else {
                        throw new FileAccessDeniedException(fileVo.getName(), FileOperationType.DOWNLOAD.getText());
                    }
                } else {
                    throw new FileTypeHandlerNotFoundException(fileVo.getType());
                }
            }
            if (!isNeedDownLoad) {
                if (response != null) {
                    response.setHeader("Content-Disposition", " attachment; filename=\"" + neatlogic.framework.util.FileUtil.getEncodedFileName(fileVo.getName()) + "\"");
                    response.setStatus(204);
                    response.getWriter().print(StringUtils.EMPTY);
                }
            }
        } else {
            throw new FileNotFoundException(id);
        }
        return null;
    }
}

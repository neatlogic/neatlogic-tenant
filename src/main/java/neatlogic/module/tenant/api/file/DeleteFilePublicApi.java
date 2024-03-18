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
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.publicapi.PublicApiComponentBase;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@Transactional

@OperationType(type = OperationTypeEnum.DELETE)
public class DeleteFilePublicApi extends PublicApiComponentBase {

    @Resource
    private FileMapper fileMapper;

    @Override
    public String getName() {
        return "删除附件(供第三方使用)";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "fileId", type = ApiParamType.LONG, desc = "附件id", isRequired = true)
    })
    @Description(desc = "删除附件(供第三方使用)")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        Long fileId = paramObj.getLong("fileId");
        FileVo fileVo = fileMapper.getFileById(fileId);
        String tenantUuid = TenantContext.get().getTenantUuid();
        if (StringUtils.isBlank(tenantUuid)) {
            throw new NoTenantException();
        }
        if (fileVo != null) {
            IFileTypeHandler fileTypeHandler = FileTypeHandlerFactory.getHandler(fileVo.getType());
            if (fileTypeHandler != null) {
                if (fileTypeHandler.valid(UserContext.get().getUserUuid(), fileVo, paramObj)) {
                    fileMapper.deleteFile(fileVo.getId());
                    FileUtil.deleteData(fileVo.getPath());
                } else {
                    throw new FileAccessDeniedException(fileVo.getName(), FileOperationType.DELETE.getText());
                }
            } else {
                throw new FileTypeHandlerNotFoundException(fileVo.getType());
            }
        } else {
            throw new FileNotFoundException(fileId);
        }
        return null;
    }
}

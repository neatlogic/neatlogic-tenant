/*Copyright (C) $today.year  深圳极向量科技有限公司 All Rights Reserved.

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

import neatlogic.framework.common.config.Config;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.crossover.CrossoverServiceFactory;
import neatlogic.framework.crossover.IFileCrossoverService;
import neatlogic.framework.file.dto.AuditFilePathVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;

import java.util.Objects;

@Deprecated
@OperationType(type = OperationTypeEnum.SEARCH)
public class ReadFileContentApi extends PrivateApiComponentBase {

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
            @Param(name = "filePath", type = ApiParamType.STRING, isRequired = true, desc = "文件路径")
    })
    @Output({
            @Param(name = "content", type = ApiParamType.STRING, desc = "内容"),
            @Param(name = "hasMore", type = ApiParamType.BOOLEAN, desc = "是否还有更多内容")
    })
    @Description(desc = "读取文件内容")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        String filePath = paramObj.getString("filePath");
        AuditFilePathVo auditFilePathVo = new AuditFilePathVo(filePath);
        IFileCrossoverService fileCrossoverService = CrossoverServiceFactory.getApi(IFileCrossoverService.class);
        if (Objects.equals(auditFilePathVo.getServerId(), Config.SCHEDULE_SERVER_ID)) {
            return fileCrossoverService.readLocalFile(auditFilePathVo.getPath(), auditFilePathVo.getStartIndex(), auditFilePathVo.getOffset());
        } else {
            return fileCrossoverService.readRemoteFile(paramObj, auditFilePathVo.getServerId());
        }
    }

}

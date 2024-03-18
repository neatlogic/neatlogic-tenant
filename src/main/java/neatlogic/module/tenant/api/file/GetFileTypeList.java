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

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.FILE_MODIFY;
import neatlogic.framework.file.core.FileTypeHandlerFactory;
import neatlogic.framework.file.dto.FileTypeVo;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
@AuthAction(action = FILE_MODIFY.class)
public class GetFileTypeList extends PrivateApiComponentBase {


    @Override
    public String getName() {
        return "获取附件归属列表";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Override
    @Output({@Param(explode = FileTypeVo[].class)})
    @Description(desc = "获取附件归属列表接口")
    public Object myDoService(JSONObject paramObj) throws Exception {
        return FileTypeHandlerFactory.getActiveFileTypeHandler();
    }

    @Override
    public String getToken() {
        return "file/type/list";
    }
}

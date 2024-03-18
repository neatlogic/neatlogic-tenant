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

package neatlogic.module.tenant.api.documentonline;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.RequestContext;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.documentonline.dto.DocumentOnlineDirectoryVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.TableResultUtil;
import neatlogic.module.framework.startup.DocumentOnlineInitializeIndexHandler;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class GetDocumentOnlineDirectoryApi extends PrivateApiComponentBase {
    @Override
    public String getName() {
        return "nmtad.getdocumentonlinedirectoryapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({})
    @Output({
            @Param(name = "tbodyList", type = ApiParamType.JSONARRAY, desc = "common.tbodylist")
    })
    @Description(desc = "nmtad.getdocumentonlinedirectoryapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        List<DocumentOnlineDirectoryVo> tbodyList = new ArrayList<>();
        Locale locale = RequestContext.get() != null ? RequestContext.get().getLocale() : Locale.getDefault();
        for (DocumentOnlineDirectoryVo child : DocumentOnlineInitializeIndexHandler.DOCUMENT_ONLINE_DIRECTORY_ROOT.getChildren()) {
            if (Objects.equals(child.getName(), locale.getLanguage())) {
                tbodyList = child.getChildren();
            }
        }
        return TableResultUtil.getResult(tbodyList);
    }

    @Override
    public String getToken() {
        return "documentonline/directory";
    }
}

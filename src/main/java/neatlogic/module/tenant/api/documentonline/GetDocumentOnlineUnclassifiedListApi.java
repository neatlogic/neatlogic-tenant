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
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.common.util.PageUtil;
import neatlogic.framework.documentonline.dto.DocumentOnlineDirectoryVo;
import neatlogic.framework.documentonline.dto.DocumentOnlineVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.TableResultUtil;
import neatlogic.module.framework.startup.DocumentOnlineInitializeIndexHandler;
import neatlogic.module.tenant.service.documentonline.DocumentOnlineService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class GetDocumentOnlineUnclassifiedListApi extends PrivateApiComponentBase {

    @Resource
    private DocumentOnlineService documentOnlineService;

    @Override
    public String getName() {
        return "nmtad.getdocumentonlineunclassifiedlistapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "common.currentpage"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "common.pagesize")
    })
    @Output({
            @Param(explode = BasePageVo.class),
            @Param(name = "tbodyList", explode = DocumentOnlineVo[].class, desc = "common.tbodylist")
    })
    @Description(desc = "nmtad.getdocumentonlineunclassifiedlistapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        BasePageVo basePageVo = paramObj.toJavaObject(BasePageVo.class);
        List<DocumentOnlineVo> tbodyList = new ArrayList<>();
        DocumentOnlineDirectoryVo directory = null;
        Locale locale = RequestContext.get() != null ? RequestContext.get().getLocale() : Locale.getDefault();
        for (DocumentOnlineDirectoryVo localeLevel : DocumentOnlineInitializeIndexHandler.DOCUMENT_ONLINE_DIRECTORY_ROOT.getChildren()) {
            if (Objects.equals(localeLevel.getName(), locale.getLanguage())) {
                directory = localeLevel;
            }
        }
        if (directory == null) {
            return TableResultUtil.getResult(tbodyList, basePageVo);
        }

        tbodyList = documentOnlineService.getAllFileList(directory);
        if (tbodyList.size() == 0) {
            return TableResultUtil.getResult(tbodyList, basePageVo);
        }
        Iterator<DocumentOnlineVo> iterator = tbodyList.iterator();
        while (iterator.hasNext()) {
            DocumentOnlineVo documentOnlineVo = iterator.next();
            if (CollectionUtils.isNotEmpty(documentOnlineVo.getConfigList())) {
                iterator.remove();
            }
        }
        basePageVo.setRowNum(tbodyList.size());
        tbodyList = PageUtil.subList(tbodyList, basePageVo);
        return TableResultUtil.getResult(tbodyList, basePageVo);
    }

    @Override
    public String getToken() {
        return "documentonline/unclassified/list";
    }

}

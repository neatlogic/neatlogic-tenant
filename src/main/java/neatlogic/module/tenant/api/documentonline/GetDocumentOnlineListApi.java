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

import com.alibaba.fastjson.JSONArray;
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
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class GetDocumentOnlineListApi extends PrivateApiComponentBase {

    @Resource
    private DocumentOnlineService documentOnlineService;

    @Override
    public String getName() {
        return "nmtad.getdocumentonlinelistapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "upwardNameList", type = ApiParamType.JSONARRAY, desc = "common.upwardnamelist"),
            @Param(name = "moduleGroup", type = ApiParamType.STRING, desc = "common.modulegroup"),
            @Param(name = "menu", type = ApiParamType.STRING, desc = "common.menu"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "common.currentpage"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "common.pagesize")
    })
    @Output({
            @Param(explode = BasePageVo.class),
            @Param(name = "tbodyList", explode = DocumentOnlineVo[].class, desc = "common.tbodylist")
    })
    @Description(desc = "nmtad.getdocumentonlinelistapi.getname")
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
        JSONArray upwardNameArray = paramObj.getJSONArray("upwardNameList");
        if (CollectionUtils.isNotEmpty(upwardNameArray)) {
            List<String> upwardNameList = upwardNameArray.toJavaList(String.class);
            for (String upwardName : upwardNameList) {
                for (DocumentOnlineDirectoryVo child : directory.getChildren()) {
                    if (child.getIsFile()) {
                        continue;
                    }
                    if (Objects.equals(upwardName, child.getName())) {
                        directory = child;
                        break;
                    }
                }
                if (directory == null) {
                    break;
                }
            }
            if (directory == null) {
                return TableResultUtil.getResult(tbodyList, basePageVo);
            }
        }
        String moduleGroup = paramObj.getString("moduleGroup");
        String menu = paramObj.getString("menu");
        if (menu == null) {
            menu = StringUtils.EMPTY;
        }
        tbodyList = documentOnlineService.getAllFileList(directory, moduleGroup, menu);
        if (tbodyList.size() == 0) {
            return TableResultUtil.getResult(tbodyList, basePageVo);
        }
        basePageVo.setRowNum(tbodyList.size());
        tbodyList = PageUtil.subList(tbodyList, basePageVo);
        // 遍历当前页中列表的所有文档，加载文档前120个字符内容
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        for (DocumentOnlineVo tbody : tbodyList) {
            org.springframework.core.io.Resource resource = resolver.getResource("classpath:" + tbody.getFilePath());
            if (!resource.exists()) {
                continue;
            }
            String content = documentOnlineService.interceptsSpecifiedNumberOfCharacters(resource.getInputStream(), 0, 120);
            tbody.setContent(content);
        }
        return TableResultUtil.getResult(tbodyList, basePageVo);
    }

    @Override
    public String getToken() {
        return "documentonline/list";
    }

}

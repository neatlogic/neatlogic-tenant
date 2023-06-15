/*
 * Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package neatlogic.module.tenant.api.documentonline;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.RequestContext;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.common.util.PageUtil;
import neatlogic.framework.documentonline.dto.DocumentOnlineDirectoryVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.TableResultUtil;
import neatlogic.module.framework.startup.DocumentOnlineInitializeIndexHandler;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.text.TextContentRenderer;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class getDocumentOnlineListApi extends PrivateApiComponentBase {

    private final Pattern PATTERN = Pattern.compile("!\\[\\w*\\]\\((\\.\\./)*([\u4E00-\u9FA5_\\w]+/)*[\u4E00-\u9FA5_\\w]+\\.\\w+\\)");

    @Override
    public String getName() {
        return "查询在线帮助文档列表";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "upwardNameList", type = ApiParamType.JSONARRAY, desc = "上层目录名称列表"),
            @Param(name = "moduleGroup", type = ApiParamType.STRING, desc = "模块组标识"),
            @Param(name = "menu", type = ApiParamType.STRING, desc = "菜单标识"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页数据条目")
    })
    @Output({
            @Param(name = "tbodyList", type = ApiParamType.JSONARRAY, desc = "文档列表")
    })
    @Description(desc = "查询在线帮助文档")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        BasePageVo basePageVo = paramObj.toJavaObject(BasePageVo.class);
        List<JSONObject> tbodyList = new ArrayList<>();
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
        tbodyList = getAllFileList(directory, moduleGroup, menu);
        if (tbodyList.size() == 0) {
            return TableResultUtil.getResult(tbodyList, basePageVo);
        }
        basePageVo.setRowNum(tbodyList.size());
        tbodyList = PageUtil.subList(tbodyList, basePageVo);
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        for (JSONObject tbody : tbodyList) {
            String filePath = tbody.getString("filePath");
            Resource resource = resolver.getResource("classpath:" + filePath);
            if (resource == null) {
                continue;
            }
            StringBuilder stringBuilder = new StringBuilder(150);
            InputStreamReader inputStreamReader = new InputStreamReader(resource.getInputStream());
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            while(stringBuilder.length() < 120) {
                String lineContent = bufferedReader.readLine();
                if (StringUtils.isBlank(lineContent)) {
                    continue;
                }

                lineContent = replaceImagePath(lineContent);
                if (StringUtils.isBlank(lineContent)) {
                    continue;
                }
                Parser parser = Parser.builder().build();
                Node document = parser.parse(lineContent);
                TextContentRenderer textContentRenderer = TextContentRenderer.builder().build();
                lineContent = textContentRenderer.render(document);
                if (StringUtils.isBlank(lineContent)) {
                    continue;
                }
                stringBuilder.append(lineContent);
            }
            tbody.put("content", stringBuilder.toString());
        }
        return TableResultUtil.getResult(tbodyList, basePageVo);
    }

    @Override
    public String getToken() {
        return "documentonline/list";
    }

    /**
     * 通过递归，获取某个目录下的所有文件
     * @param directory
     * @return
     */
    private List<JSONObject> getAllFileList(DocumentOnlineDirectoryVo directory, String moduleGroup, String menu) {
        List<JSONObject> list = new ArrayList<>();
        for (DocumentOnlineDirectoryVo child : directory.getChildren()) {
            if (child.getIsFile()) {
                if (StringUtils.isBlank(moduleGroup) || child.belongToModuleGroup(moduleGroup)) {
                    if (StringUtils.isBlank(menu) || child.belongToMenu(menu)) {
                        JSONObject fileInfo = new JSONObject();
                        fileInfo.put("upwardNameList", child.getUpwardNameList());
                        fileInfo.put("filePath", child.getFilePath());
                        fileInfo.put("fileName", child.getName());
                        list.add(fileInfo);
                    }
                }
            } else {
                list.addAll(getAllFileList(child, moduleGroup, menu));
            }
        }
        return list;
    }

    /**
     * 将文档内容中图片相对路径转化为http请求url
     * @param content 文档内容
     * @return
     */
    private String replaceImagePath(String content) {
        StringBuilder stringBuilder = new StringBuilder();
        int beginIndex = 0;
        Matcher figureMatcher = PATTERN.matcher(content);
        while (figureMatcher.find()) {
            String group = figureMatcher.group();
            int index = content.indexOf(group, beginIndex);
            String subStr = content.substring(beginIndex, index);
            stringBuilder.append(subStr);
            beginIndex = index + group.length();
        }
        stringBuilder.append(content.substring(beginIndex));
        return stringBuilder.toString();
    }
}

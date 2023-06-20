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

package neatlogic.module.tenant.service.documentonline;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.documentonline.dto.DocumentOnlineDirectoryVo;
import neatlogic.framework.documentonline.dto.DocumentOnlineVo;
import neatlogic.framework.util.HtmlUtil;
import neatlogic.framework.util.RegexUtils;
import org.apache.commons.lang3.StringUtils;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

@Service
public class DocumentOnlineServiceImpl implements DocumentOnlineService {

    @Override
    public String interceptsSpecifiedNumberOfCharacters(InputStream inputStream, int skip, int number) throws IOException {

        Parser parser = Parser.builder().build();
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        StringBuilder stringBuilder = new StringBuilder();
        try (InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
            bufferedReader.skip(skip);
            while (stringBuilder.length() < number) {
                String lineContent = bufferedReader.readLine();
                if (lineContent == null) {
                    break;
                }
                if (StringUtils.isBlank(lineContent)) {
                    continue;
                }
                // 1.先把这行内容中HTML标签去掉，因为第2步中将markdown语法转换成HTML标签时，会把原有的HTML标签中的尖括号转成实体字符
                // 例如：<img src="https://img.shields.io/badge/License-Apache%202.0-blue.svg" /></a>
                // 转换成 <pre><code>    &lt;img src=&quot;https://img.shields.io/badge/License-Apache%202.0-blue.svg&quot; /&gt;&lt;/a&gt;</code></pre>
                lineContent = HtmlUtil.removeHtml(lineContent);
                if (StringUtils.isBlank(lineContent)) {
                    continue;
                }
                // 2.把这行内容中markdown语法转换成HTML标签
                Node document = parser.parse(lineContent);
                String html = renderer.render(document);
                // 3.再次把这行内容中HTML标签去掉
                lineContent = HtmlUtil.removeHtml(html);
                if (StringUtils.isBlank(lineContent)) {
                    continue;
                }
                stringBuilder.append(" ");
                if (lineContent.length() > number - stringBuilder.length()) {
                    stringBuilder.append(lineContent.substring(0, number - stringBuilder.length()));
                } else {
                    stringBuilder.append(lineContent);
                }
            }
        }
        return stringBuilder.toString();
    }

    @Override
    public List<DocumentOnlineVo> getAllFileList(DocumentOnlineDirectoryVo directory, String moduleGroup, String menu) {
        List<DocumentOnlineVo> list = new ArrayList<>();
        for (DocumentOnlineDirectoryVo child : directory.getChildren()) {
            if (child.getIsFile()) {
                JSONObject returnObj = new JSONObject();
                if (StringUtils.isBlank(moduleGroup) || child.belongToOwner(moduleGroup, menu, returnObj)) {
                    DocumentOnlineVo documentOnlineVo = new DocumentOnlineVo();
                    documentOnlineVo.setUpwardNameList(child.getUpwardNameList());
                    documentOnlineVo.setFileName(child.getName());
                    documentOnlineVo.setFilePath(child.getFilePath());
                    String anchorPoint = returnObj.getString("anchorPoint");
                    documentOnlineVo.setAnchorPoint(anchorPoint);
                    list.add(documentOnlineVo);
                }
            } else {
                list.addAll(getAllFileList(child, moduleGroup, menu));
            }
        }
        return list;
    }

    @Override
    public List<DocumentOnlineVo> getAllFileList(DocumentOnlineDirectoryVo directory) {
        return getAllFileList(directory, null, null);
    }
}

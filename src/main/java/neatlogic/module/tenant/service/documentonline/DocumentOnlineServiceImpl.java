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

package neatlogic.module.tenant.service.documentonline;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.crossover.CrossoverServiceFactory;
import neatlogic.framework.documentonline.crossover.IDocumentOnlineCrossoverMapper;
import neatlogic.framework.documentonline.dto.DocumentOnlineConfigVo;
import neatlogic.framework.documentonline.dto.DocumentOnlineDirectoryVo;
import neatlogic.framework.documentonline.dto.DocumentOnlineVo;
import neatlogic.framework.util.HtmlUtil;
import neatlogic.module.framework.startup.DocumentOnlineInitializeIndexHandler;
import org.apache.commons.lang3.StringUtils;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

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
        return getAllFileListAndSort(directory, moduleGroup, menu);
    }

    /**
     * 通过递归，获取某个目录下的指定模块、指定菜单下的文件，并且排序
     * @param directory 目录
     * @param moduleGroup 指定模块
     * @param menu 指定菜单
     * @return 返回文件列表
     */
    private List<DocumentOnlineVo> getAllFileListAndSort(DocumentOnlineDirectoryVo directory, String moduleGroup, String menu) {
        List<DocumentOnlineVo> list = getAllFileListUseRecurve(directory, moduleGroup, menu);
        list.sort((o1, o2) -> {
            if (o1.getPrefix() != null && o2.getPrefix() != null) {
                return Integer.compare(o1.getPrefix(), o2.getPrefix());
            } else if (o1.getPrefix() != null && o2.getPrefix() == null) {
                return -1;
            } else if (o1.getPrefix() == null && o2.getPrefix() != null) {
                return 1;
            } else {
                return String.CASE_INSENSITIVE_ORDER.compare(o1.getFileName(), o2.getFileName());
            }
        });
        return list;
    }

    /**
     * 通过递归，获取某个目录下的指定模块、指定菜单下的文件
     * @param directory 目录
     * @param moduleGroup 指定模块
     * @param menu 指定菜单
     * @return 返回文件列表
     */
    private List<DocumentOnlineVo> getAllFileListUseRecurve(DocumentOnlineDirectoryVo directory, String moduleGroup, String menu) {
        List<DocumentOnlineVo> list = new ArrayList<>();
        for (DocumentOnlineDirectoryVo child : directory.getChildren()) {
            if (child.getIsFile()) {
                JSONObject returnObj = new JSONObject();
                if (StringUtils.isBlank(moduleGroup) || child.belongToOwner(moduleGroup, menu, returnObj)) {
                    DocumentOnlineVo documentOnlineVo = new DocumentOnlineVo();
                    documentOnlineVo.setUpwardNameList(child.getUpwardNameList());
                    documentOnlineVo.setPrefix(child.getPrefix());
                    documentOnlineVo.setFileName(child.getName());
                    documentOnlineVo.setFilePath(child.getFilePath());
                    String anchorPoint = returnObj.getString("anchorPoint");
                    documentOnlineVo.setAnchorPoint(anchorPoint);
                    documentOnlineVo.setConfigList(child.getConfigList());
                    list.add(documentOnlineVo);
                }
            } else {
                list.addAll(getAllFileListUseRecurve(child, moduleGroup, menu));
            }
        }
        return list;
    }

    @Override
    public List<DocumentOnlineVo> getAllFileList(DocumentOnlineDirectoryVo directory) {
        return getAllFileListAndSort(directory, null, null);
    }

    @Override
    public DocumentOnlineDirectoryVo getDocumentOnlineDirectoryByFilePath(String filePath) {
        if (StringUtils.isBlank(filePath)) {
            return null;
        }
        int directoryRootIndex = filePath.indexOf(DocumentOnlineInitializeIndexHandler.DIRECTORY_ROOT);
        filePath = filePath.substring(directoryRootIndex);
        DocumentOnlineDirectoryVo directory = DocumentOnlineInitializeIndexHandler.DOCUMENT_ONLINE_DIRECTORY_ROOT;
        String[] directoryNameList = filePath.split("/");
        for (int i = 1; i < directoryNameList.length; i++) {
            String directoryName = directoryNameList[i];
            // 标记是否找到对应的目录
            boolean flag = false;
            for (DocumentOnlineDirectoryVo child : directory.getChildren()) {
                String childName = child.getName();
                if (child.getIsFile()) {
                    childName += ".md";
                }
                if (child.getPrefix() != null) {
                    childName = child.getPrefix() + "." + childName;
                }
                if (Objects.equals(childName, directoryName)) {
                    directory = child;
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                return null;
            }
        }
        return directory;
    }

    @Transactional
    @Override
    public void saveDocumentOnlineConfig(DocumentOnlineDirectoryVo directory, DocumentOnlineConfigVo newConfigVo) {
        boolean needUpdateDataBaseTable = false;
        boolean updateSuccessfully = false;
        List<DocumentOnlineConfigVo> configList = directory.getConfigList();
        Iterator<DocumentOnlineConfigVo> iterator = configList.iterator();
        while (iterator.hasNext()) {
            DocumentOnlineConfigVo configVo = iterator.next();
            if (configVo.equals(newConfigVo)) {
                if (!Objects.equals(configVo.getAnchorPoint(), newConfigVo.getAnchorPoint())) {
                    configVo.setAnchorPoint(newConfigVo.getAnchorPoint());
                    configVo.setSource("database");
                    needUpdateDataBaseTable = true;
                }
                updateSuccessfully = true;
                break;
            }
        }
        if (!updateSuccessfully) {
            configList.add(newConfigVo);
            needUpdateDataBaseTable = true;
        }
        if (needUpdateDataBaseTable) {
            IDocumentOnlineCrossoverMapper documentOnlineCrossoverMapper = CrossoverServiceFactory.getApi(IDocumentOnlineCrossoverMapper.class);
            documentOnlineCrossoverMapper.insertDocumentOnlineConfig(newConfigVo);
        }
    }

    @Transactional
    @Override
    public void deleteDocumentOnlineConfig(DocumentOnlineDirectoryVo directory, DocumentOnlineConfigVo oldConfigVo) {
        List<DocumentOnlineConfigVo> configList = directory.getConfigList();
        Iterator<DocumentOnlineConfigVo> iterator = configList.iterator();
        while (iterator.hasNext()) {
            DocumentOnlineConfigVo configVo = iterator.next();
            if (configVo.equals(oldConfigVo)) {
                if (Objects.equals(configVo.getSource(), "database")) {
                    IDocumentOnlineCrossoverMapper documentOnlineCrossoverMapper = CrossoverServiceFactory.getApi(IDocumentOnlineCrossoverMapper.class);
                    documentOnlineCrossoverMapper.deleteDocumentOnlineConfig(oldConfigVo);
                }
                iterator.remove();
                break;
            }
        }
    }
}

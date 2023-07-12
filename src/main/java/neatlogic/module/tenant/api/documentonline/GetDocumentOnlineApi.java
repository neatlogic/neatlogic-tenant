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

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.documentonline.dto.DocumentOnlineDirectoryVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.documentonline.dto.DocumentOnlineVo;
import neatlogic.framework.documentonline.exception.DocumentOnlineNotFoundException;
import neatlogic.framework.util.RegexUtils;
import neatlogic.module.tenant.service.documentonline.DocumentOnlineService;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class GetDocumentOnlineApi extends PrivateApiComponentBase {

    @Autowired
    private DocumentOnlineService documentOnlineService;

    @Override
    public String getName() {
        return "nmtad.getdocumentonlineapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "filePath", type = ApiParamType.STRING, isRequired = true, desc = "common.filepath")
    })
    @Output({
            @Param(explode = DocumentOnlineVo.class, desc = "term.framework.documentonlineinfo")
    })
    @Description(desc = "nmtad.getdocumentonlineapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        String filePath = paramObj.getString("filePath");
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource resource = resolver.getResource("classpath:" + filePath);
        if (!resource.exists()) {
            throw new DocumentOnlineNotFoundException(filePath);
        }
        DocumentOnlineDirectoryVo directory = documentOnlineService.getDocumentOnlineDirectoryByFilePath(filePath);
        if (directory == null) {
            throw new DocumentOnlineNotFoundException(filePath);
        }
        String filename = resource.getFilename().substring(0, resource.getFilename().length() - 3);
        StringWriter writer = new StringWriter();
        IOUtils.copy(resource.getInputStream(), writer, StandardCharsets.UTF_8);
        String content = writer.toString();
        writer.close();
        DocumentOnlineVo documentOnlineVo = new DocumentOnlineVo();
        documentOnlineVo.setContent(replaceImagePath(content, filePath));
        documentOnlineVo.setFileName(filename);
        documentOnlineVo.setUpwardNameList(directory.getUpwardNameList());
        return documentOnlineVo;
    }

    @Override
    public String getToken() {
        return "documentonline/get";
    }

    /**
     * 将文档内容中图片相对路径转化为http请求url
     * @param content 文档内容
     * @param filePath 文档路径
     * @return
     */
    private String replaceImagePath(String content, String filePath) {
        StringBuilder stringBuilder = new StringBuilder();
        int beginIndex = 0;
        Matcher figureMatcher = RegexUtils.getPattern(RegexUtils.MARKDOWN_LINK).matcher(content);
        while (figureMatcher.find()) {
            String group = figureMatcher.group();
            int index = content.indexOf(group, beginIndex);
            String subStr = content.substring(beginIndex, index);
            stringBuilder.append(subStr);
            int lightParenthesisIndex = group.lastIndexOf(")");
            int leftParenthesisIndex = group.lastIndexOf("(", lightParenthesisIndex);
            // 截取出![xxx]部分，xxx可能存在图片格式或链接格式，需要继续解析
            String squareBrackets = group.substring(0, leftParenthesisIndex);
            stringBuilder.append(replaceImagePath(squareBrackets, filePath));
            stringBuilder.append("(");
            String url = null;
            String relativePath = group.substring(leftParenthesisIndex + 1, lightParenthesisIndex);
            String relativePathToLowerCase = relativePath.toLowerCase();
            if (relativePathToLowerCase.startsWith("http://") || relativePathToLowerCase.startsWith("https://")) {
                url = relativePath;
            } else {
                String absolutePath = relativePathToAbsolutePath(relativePath, filePath);
                if (group.startsWith("!")) {
                    url = "api/binary/classpath/image/download?filePath=" + absolutePath;
                } else {
                    url = "documentonline.html#/documentonline-detail?filePath=" + absolutePath;
                }
            }
            stringBuilder.append(url);
            stringBuilder.append(group.substring(lightParenthesisIndex));
            beginIndex = index + group.length();
        }
        stringBuilder.append(content.substring(beginIndex));
        return stringBuilder.toString();
    }

    /**
     * 将图片地址的相对路径转化成绝对路径
     * @param relativePath 相对路径
     * @param filePath 文档路径
     * @return 绝对路径
     */
    private String relativePathToAbsolutePath(String relativePath, String filePath) {
        int returnParentCount = 0;
        for (int i = 0; i < 100; i++) {
            if (relativePath.startsWith("/")) {
                relativePath = relativePath.substring(1);
            } else if (relativePath.startsWith("..")) {
                relativePath = relativePath.substring(2);
                returnParentCount++;
            } else if (relativePath.startsWith(".")) {
                relativePath = relativePath.substring(1);
            } else {
                break;
            }
        }
        int index = filePath.lastIndexOf("/");
        filePath = filePath.substring(0, index);
        for (int i = 0; i < returnParentCount; i++) {
            index = filePath.lastIndexOf("/");
            if (index == -1) {
                filePath = "";
                break;
            }
            filePath = filePath.substring(0, index);
        }
        if (StringUtils.isBlank(filePath)) {
            return relativePath;
        }
        return filePath + "/" + relativePath;
    }
}

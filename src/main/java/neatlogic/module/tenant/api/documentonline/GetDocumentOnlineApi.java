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
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.documentonline.dto.DocumentOnlineVo;
import neatlogic.framework.documentonline.exception.DocumentOnlineNotFoundException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class GetDocumentOnlineApi extends PrivateApiComponentBase {

    private final Pattern PATTERN = Pattern.compile("!\\[\\w*\\]\\((\\.\\./)*(\\w+/)*\\w+\\.\\w+\\)");

    @Override
    public String getName() {
        return "获取单个在线帮助文档";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "filePath", type = ApiParamType.STRING, isRequired = true, desc = "文档路径")
    })
    @Output({
            @Param(explode = DocumentOnlineVo.class, desc = "在线帮助文档详情")
    })
    @Description(desc = "获取单个在线帮助文档")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        String filePath = paramObj.getString("filePath");
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource resource = resolver.getResource("classpath:" + filePath);
        if (resource == null) {
            throw new DocumentOnlineNotFoundException(filePath);
        }
        String filename = resource.getFilename().substring(0, resource.getFilename().length() - 3);
        StringWriter writer = new StringWriter();
        IOUtils.copy(resource.getInputStream(), writer, StandardCharsets.UTF_8);
        String content = writer.toString();
        DocumentOnlineVo documentOnlineVo = new DocumentOnlineVo();
        documentOnlineVo.setContent(replaceImagePath(content, filePath));
        documentOnlineVo.setFileName(filename);
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
        Matcher figureMatcher = PATTERN.matcher(content);
        while (figureMatcher.find()) {
            String group = figureMatcher.group();
            int index = content.indexOf(group, beginIndex);
            String subStr = content.substring(beginIndex, index);
            stringBuilder.append(subStr);
            int lightParenthesisIndex = group.lastIndexOf(")");
            int leftParenthesisIndex = group.lastIndexOf("(", lightParenthesisIndex);
            String relativePath = group.substring(leftParenthesisIndex + 1, lightParenthesisIndex);
            String absolutePath = relativePathToAbsolutePath(relativePath, filePath);
            String url = "api/binary/classpath/image/download?filePath=" + absolutePath;
            stringBuilder.append(group.substring(0, leftParenthesisIndex + 1));
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

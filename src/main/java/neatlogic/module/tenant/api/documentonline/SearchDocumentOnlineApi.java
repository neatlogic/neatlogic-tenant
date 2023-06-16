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
import neatlogic.framework.common.config.Config;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.crossover.CrossoverServiceFactory;
import neatlogic.framework.documentonline.crossover.DocumentOnlineServiceCrossoverService;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.TableResultUtil;
import neatlogic.framework.documentonline.dto.DocumentOnlineVo;
import neatlogic.framework.documentonline.exception.DocumentOnlineIndexDirNotSetException;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class SearchDocumentOnlineApi extends PrivateApiComponentBase {

    private final Logger logger = LoggerFactory.getLogger(SearchDocumentOnlineApi.class);

    @Override
    public String getName() {
        return "查询在线帮助文档";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "keyword", type = ApiParamType.STRING, isRequired = true, desc = "关键字"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页数据条目")
    })
    @Output({
            @Param(explode = BasePageVo.class),
            @Param(name = "tbodyList", explode = DocumentOnlineVo[].class, desc = "文档列表")
    })
    @Description(desc = "查询在线帮助文档")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        if (StringUtils.isBlank(Config.DOCUMENT_ONLINE_INDEX_DIR())) {
            throw new DocumentOnlineIndexDirNotSetException();
        }

        BasePageVo basePageVo = paramObj.toJavaObject(BasePageVo.class);
        IndexReader indexReader = null;
        try {
            // 1.创建分词器（对搜索的关键词进行分词使用）
            // 注意：分词器和创建索引的时候使用的分词器一模一样
            Analyzer analyzer = new IKAnalyzer(true);
            // 创建Directory目标对象，指定索引库的位置
            Directory directory = FSDirectory.open(Paths.get(Config.DOCUMENT_ONLINE_INDEX_DIR()));
            // 创建输入流对象
            indexReader = DirectoryReader.open(directory);
            // 创建搜索对象
            IndexSearcher indexSearcher = new IndexSearcher(indexReader);
            // 搜索，并返回结果
            QueryParser queryParser = new MultiFieldQueryParser(new String[]{"content", "fileName"}, analyzer);
            Query query = queryParser.parse(basePageVo.getKeyword());
            // 第二个参数：是返回多少条数据用于展示，分页使用
            int startNum = basePageVo.getStartNum();
            TopDocs topDocs = indexSearcher.search(query, startNum + basePageVo.getPageSize());
//            basePageVo.setRowNum((int) topDocs.totalHits.value);
            basePageVo.setRowNum((int) topDocs.totalHits.value);
            List<DocumentOnlineVo> tbodyList = new ArrayList<>();
            // 获取结果集
            ScoreDoc[] scoreDocs = topDocs.scoreDocs;
            // 遍历结果集
            if (scoreDocs != null) {
                DocumentOnlineServiceCrossoverService documentOnlineServiceCrossoverService = CrossoverServiceFactory.getApi(DocumentOnlineServiceCrossoverService.class);
                for (; startNum < scoreDocs.length; startNum++) {
                    ScoreDoc scoreDoc = scoreDocs[startNum];
                    int docId = scoreDoc.doc;
                    Document doc = indexSearcher.doc(docId);
                    if (doc == null) {
                        logger.warn("找不到docID为“" + docId + "”的文档");
                        continue;
                    }
                    DocumentOnlineVo documentOnlineVo = new DocumentOnlineVo();
                    List<String> upwardNameList = JSONArray.parseArray(doc.get("upwardNameList"), String.class);
                    documentOnlineVo.setUpwardNameList(upwardNameList);
                    documentOnlineVo.setFileName(doc.get("fileName"));
                    documentOnlineVo.setFilePath(doc.get("filePath"));
                    String content = doc.get("content");
                    int skip = keywordOnlineFirstCharacterIndex(content, basePageVo.getKeyword());
                    String result = documentOnlineServiceCrossoverService.interceptsSpecifiedNumberOfCharacters(new ByteArrayInputStream(content.getBytes()), skip, 120);
                    documentOnlineVo.setContent(result);
                    tbodyList.add(documentOnlineVo);
                }
            }
            return TableResultUtil.getResult(tbodyList, basePageVo);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw e;
        } finally {
            if (indexReader != null) {
                // 关闭流
                indexReader.close();
            }
        }
    }

    @Override
    public String getToken() {
        return "documentonline/search";
    }

    private int keywordOnlineFirstCharacterIndex(String content, String keyword) {
        String lineWrapFlag = "\r\n";
        int keywordFirstIndex = content.indexOf(keyword);
        if (keywordFirstIndex == -1) {
            return 0;
        } else {
            int beginIndex = content.lastIndexOf(lineWrapFlag, keywordFirstIndex);
            if (beginIndex == -1) {
                return 0;
            } else {
                return beginIndex + lineWrapFlag.length();
            }
        }
    }
}

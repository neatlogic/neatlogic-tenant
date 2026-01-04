/*
 *
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 */

package neatlogic.module.tenant.api.documentonline;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.NoAuth;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.documentonline.dto.DocumentOnlineDirectoryVo;
import neatlogic.framework.documentonline.dto.DocumentOnlineVo;
import neatlogic.framework.documentonline.util.DocumentOnlineManager;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.$;
import neatlogic.framework.util.TableResultUtil;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.ByteArrayInputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
@AuthAction(action = NoAuth.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class SearchDocumentOnlineApi extends PrivateApiComponentBase {

    private final Logger logger = LoggerFactory.getLogger(SearchDocumentOnlineApi.class);

    @Override
    public String getName() {
        return "nmtad.searchdocumentonlineapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "keyword", type = ApiParamType.STRING, isRequired = true, desc = "common.keyword"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "common.currentpage"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "common.pagesize")
    })
    @Output({
            @Param(explode = BasePageVo.class),
            @Param(name = "tbodyList", explode = DocumentOnlineVo[].class, desc = "common.tbodylist")
    })
    @Description(desc = "nmtad.searchdocumentonlineapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {

        BasePageVo basePageVo = paramObj.toJavaObject(BasePageVo.class);
        IndexReader indexReader = null;
        try {
            // 1.创建分词器（对搜索的关键词进行分词使用）
            // 注意：分词器和创建索引的时候使用的分词器一模一样
            Analyzer analyzer = new IKAnalyzer(true);
            // 创建Directory目标对象，指定索引库的位置
            Directory directory = MMapDirectory.open(Paths.get(DocumentOnlineManager.INDEX_DIRECTORY));
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
                for (; startNum < scoreDocs.length; startNum++) {
                    ScoreDoc scoreDoc = scoreDocs[startNum];
                    int docId = scoreDoc.doc;
                    Document doc = indexSearcher.doc(docId);
                    if (doc == null) {
                        logger.warn($.t("nmtad.searchdocumentonlineapi.mydoservice.error", docId));
                        continue;
                    }
                    DocumentOnlineVo documentOnlineVo = new DocumentOnlineVo();
                    List<String> upwardNameList = JSONArray.parseArray(doc.get("upwardNameList"), String.class);
                    documentOnlineVo.setUpwardNameList(upwardNameList);
                    documentOnlineVo.setFileName(doc.get("fileName"));
                    documentOnlineVo.setFilePath(doc.get("filePath"));
                    String content = doc.get("content");
                    int skip = keywordOnlineFirstCharacterIndex(content, basePageVo.getKeyword());
                    String result = DocumentOnlineManager.interceptsSpecifiedNumberOfCharacters(new ByteArrayInputStream(content.getBytes()), skip, 120);
                    documentOnlineVo.setContent(result);
                    DocumentOnlineDirectoryVo directoryVo = DocumentOnlineManager.getDocumentOnlineDirectoryByFilePath(doc.get("filePath"));
                    if (directoryVo != null) {
                        documentOnlineVo.setConfigList(directoryVo.getConfigList());
                        documentOnlineVo.setFilePath(directoryVo.getFilePath());
                    }
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

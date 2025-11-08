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

package neatlogic.module.tenant.api.globalsearch.word;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.fulltextindex.dto.globalsearch.WordVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.apache.commons.lang3.StringUtils;

//@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class WordSearchApi extends PrivateApiComponentBase {


    @Override
    public String getToken() {
        return "globalsearch/word/search";
    }

    @Override
    public String getName() {
        return "全局搜索关键字查询接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "keyword", type = ApiParamType.STRING, isRequired = true, desc = "关键字"), @Param(name = "limit", type = ApiParamType.LONG, desc = "返回条目数量")})
    @Output({@Param(name = "ciEntityId", type = ApiParamType.LONG, desc = "ci实例ID"), @Param(name = "transactionId", type = ApiParamType.LONG, desc = "事务ID")})
    @Description(desc = "全局搜索关键字查询接口")
    @Override
    public Object myDoService(JSONObject jsonObj) {
        JSONArray returnList = new JSONArray();
        String keyword = jsonObj.getString("keyword");
        Integer limit = jsonObj.getInteger("limit");
        if (StringUtils.isNotBlank(keyword)) {
            WordVo wordVo = new WordVo();
            wordVo.setWord(keyword);
            if (limit != null) {
                wordVo.setNeedPage(true);
                wordVo.setPageSize(limit);
            } else {
                wordVo.setNeedPage(false);
            }
            //List<WordVo> wordList = GlobalSearchManager.searchWordList(wordVo);
			/*(if (wordList != null && wordList.size() > 0) {
				for (WordVo word : wordList) {
					returnList.add(word.getWord());
				}
			}*/
        }
        return returnList;
    }

}

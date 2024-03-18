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

package neatlogic.module.tenant.api.globalsearch.word;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.fulltextindex.dto.globalsearch.WordVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
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

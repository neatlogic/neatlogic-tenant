/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.globalsearch.word;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.fulltextindex.dto.globalsearch.WordVo;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
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

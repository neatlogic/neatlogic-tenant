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

package neatlogic.module.tenant.api.fulltextindex;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.FULLTEXTINDEX_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.fulltextindex.dao.mapper.FullTextIndexDictMapper;
import neatlogic.framework.fulltextindex.dto.fulltextindex.FullTextIndexWordVo;
import neatlogic.framework.fulltextindex.utils.FullTextIndexUtil;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@AuthAction(action = FULLTEXTINDEX_MODIFY.class)
@OperationType(type = OperationTypeEnum.CREATE)
public class InsertDictionaryWordApi extends PrivateApiComponentBase {

    @Resource
    private FullTextIndexDictMapper fullTextIndexDictMapper;

    @Override
    public String getToken() {
        return "fulltextindex/dictionary/word/insert";
    }

    @Override
    public String getName() {
        return "添加关键字进入自定义字典";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "word", desc = "关键字", type = ApiParamType.STRING, isRequired = true)})
    @Description(desc = "添加关键字进入自定义字典")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        String word = paramObj.getString("word");
        String[] words = word.split("\n");
        for (String w : words) {
            if (StringUtils.isNotBlank(w)) {
                w = w.trim();
                if (w.length() > 20) {
                    w = w.substring(0, 20);
                }
                FullTextIndexWordVo wordVo = new FullTextIndexWordVo(w, null);
                if (fullTextIndexDictMapper.insertWord(wordVo) > 0) {
                    FullTextIndexUtil.addWord(wordVo.getWord());
                }
            }
        }
        return null;
    }
}

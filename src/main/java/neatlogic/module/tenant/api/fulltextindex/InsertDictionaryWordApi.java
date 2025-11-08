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

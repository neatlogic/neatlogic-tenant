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
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@AuthAction(action = FULLTEXTINDEX_MODIFY.class)
@OperationType(type = OperationTypeEnum.DELETE)
public class DeleteDictionaryWordApi extends PrivateApiComponentBase {

    @Resource
    private FullTextIndexDictMapper fullTextIndexDictMapper;

    @Override
    public String getToken() {
        return "fulltextindex/dictionary/word/delete";
    }

    @Override
    public String getName() {
        return "删除关键字";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "id", desc = "id", type = ApiParamType.LONG, isRequired = true)})
    @Description(desc = "删除关键字")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        Long id = paramObj.getLong("id");
        fullTextIndexDictMapper.deleteWordById(id);
        return null;
    }
}

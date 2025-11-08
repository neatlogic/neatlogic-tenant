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
import neatlogic.framework.fulltextindex.dto.fulltextindex.FullTextIndexWordOffsetVo;
import neatlogic.framework.fulltextindex.utils.FullTextIndexUtil;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.springframework.stereotype.Service;

@Service
@AuthAction(action = FULLTEXTINDEX_MODIFY.class)
@OperationType(type = OperationTypeEnum.OPERATE)
public class TestFulltextSliceWordApi extends PrivateApiComponentBase {

    @Override
    public String getToken() {
        return "fulltextindex/dictionary/word/test";
    }

    @Override
    public String getName() {
        return "测试分词";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "content", desc = "内容", type = ApiParamType.STRING, isRequired = true)})
    @Output({@Param(explode = FullTextIndexWordOffsetVo[].class)})
    @Description(desc = "测试分词")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        String content = paramObj.getString("content");
        return FullTextIndexUtil.sliceWord(content);
    }
}

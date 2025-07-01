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

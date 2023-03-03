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

package neatlogic.module.tenant.api.globalsearch.documenttype;

import neatlogic.framework.fulltextindex.core.FullTextIndexHandlerFactory;
import neatlogic.framework.fulltextindex.dto.fulltextindex.FullTextIndexTypeVo;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;

import java.util.stream.Collectors;

//@Service
//@OperationType(type = OperationTypeEnum.SEARCH)
public class ListDocumentTypeApi extends PrivateApiComponentBase {

    @Override
    public String getToken() {
        return "globalsearch/document/type/list";
    }

    @Override
    public String getName() {
        return "获取全局搜索文档类型";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Output({@Param(explode = FullTextIndexTypeVo[].class)})
    @Description(desc = "获取全局搜索文档类型接口")
    @Override
    public Object myDoService(JSONObject jsonObj) {
        return FullTextIndexHandlerFactory.getAllTypeList().stream().filter(FullTextIndexTypeVo::isActiveGlobalSearch).collect(Collectors.toList());
    }

}

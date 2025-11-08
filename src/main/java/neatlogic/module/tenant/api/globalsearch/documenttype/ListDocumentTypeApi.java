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

package neatlogic.module.tenant.api.globalsearch.documenttype;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.fulltextindex.core.FullTextIndexHandlerFactory;
import neatlogic.framework.fulltextindex.dto.fulltextindex.FullTextIndexTypeVo;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;

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

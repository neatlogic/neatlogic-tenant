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
package neatlogic.module.tenant.dto;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.EntityField;

import java.io.Serializable;
import java.util.List;

public class ChangelogVersionContentVo implements Serializable {

    @EntityField(name = "类型", type = ApiParamType.STRING)
    private String type;

    @EntityField(name = "变更内容详情", type = ApiParamType.JSONOBJECT)
    private List<ChangelogVersionContentDetailVo> detail;


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<ChangelogVersionContentDetailVo> getDetail() {
        return detail;
    }

    public void setDetail(List<ChangelogVersionContentDetailVo> detail) {
        this.detail = detail;
    }
}

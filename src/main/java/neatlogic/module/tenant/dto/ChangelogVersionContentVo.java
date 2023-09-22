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

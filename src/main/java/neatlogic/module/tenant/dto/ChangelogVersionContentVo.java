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

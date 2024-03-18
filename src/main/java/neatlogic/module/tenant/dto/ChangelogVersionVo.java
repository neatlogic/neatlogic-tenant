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

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.EntityField;
import neatlogic.framework.util.TimeUtil;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class ChangelogVersionVo implements Serializable {

    @EntityField(name = "版本时间", type = ApiParamType.STRING)
    private Date date;

    @EntityField(name = "版本内容", type = ApiParamType.JSONOBJECT)
    private List<ChangelogVersionContentVo> content;

    @JSONField(serialize = false)
    private JSONObject startTimeRange;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public List<ChangelogVersionContentVo> getContent() {
        return content;
    }

    public void setContent(List<ChangelogVersionContentVo> content) {
        this.content = content;
    }

    public List<Long> getStartTimeRange() {
        return TimeUtil.getTimeRangeList(this.startTimeRange);
    }

    public void setStartTimeRange(JSONObject startTimeRange) {
        this.startTimeRange = startTimeRange;
    }
}

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

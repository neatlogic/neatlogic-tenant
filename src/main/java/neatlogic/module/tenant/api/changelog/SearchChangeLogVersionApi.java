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
package neatlogic.module.tenant.api.changelog;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.TimeUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class SearchChangeLogVersionApi extends PrivateApiComponentBase {
    @Override
    public String getName() {
        return "nmtac.searchchangelogversionapi.getname";
    }

    @Input({
            @Param(name = "moduleId", type = ApiParamType.STRING, isRequired = true, desc = "term.cmdb.moduleid"),
            @Param(name = "startTime", type = ApiParamType.JSONOBJECT, desc = "common.time")
    })
    @Output({
            @Param(name = "versionList", type = ApiParamType.JSONARRAY, desc = "nmtac.searchchangelogversionapi.output.param.desc")
    })

    @Example(example = "{\n" +
            "    \"Status\": \"OK\",\n" +
            "    \"Return\": {\n" +
            "        \"versionList\": [\"2023-09-14\",\"2023-09-10\"]\n" +
            "    }\n" +
            "}")
    @Description(desc = "nmtac.getchangelogversionapi.description.desc")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        JSONObject result = new JSONObject();
        List<String> list = new ArrayList<>();
        int startTimeStart = 0;
        int startTimeEnd = 0;
        if (paramObj.containsKey("startTime") && MapUtils.isNotEmpty(paramObj.getJSONObject("startTime"))) {
            JSONObject startTimeFilter = TimeUtil.getStartTimeAndEndTimeByDateJson(paramObj.getJSONObject("startTime"));
            if (MapUtils.isNotEmpty(startTimeFilter)) {
                startTimeStart = Integer.parseInt((startTimeFilter.getString("startTime").substring(0, 10).replace("-", StringUtils.EMPTY)));
                startTimeEnd = Integer.parseInt((startTimeFilter.getString("endTime").substring(0, 10).replace("-", StringUtils.EMPTY)));
            }
        }

        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath*:neatlogic/resources/" + paramObj.getString("moduleId") + "/changelog/*/");
        for (Resource resource : resources) {
            String fileName = resource.getURL().getPath().substring(0, resource.getURL().getPath().lastIndexOf("/"));
            String version = fileName.substring(fileName.lastIndexOf("/") + 1);
            int versionTmp = Integer.parseInt(version.replace("-", StringUtils.EMPTY).substring(0, 8));
            if ((startTimeStart == 0 && startTimeEnd == 0) || (startTimeStart != 0 && startTimeEnd != 0 && versionTmp >= startTimeStart && versionTmp <= startTimeEnd)) {
                list.add(version);
            }
        }
        if (CollectionUtils.isNotEmpty(list)) {
            // 定义日期格式
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(TimeUtil.YYYY_MM_DD);

            // 定义倒序比较器
            Comparator<String> fileNameComparatorReversed = Comparator.reverseOrder();

            // 按日期排序
            list.sort(fileNameComparatorReversed);
        }
        result.put("versionList", list);
        return result;
    }

    @Override
    public String getToken() {
        return "/module/changelog/search";
    }
}

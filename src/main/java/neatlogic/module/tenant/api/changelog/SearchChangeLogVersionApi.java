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
package neatlogic.module.tenant.api.changelog;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.TimeUtil;
import org.apache.commons.collections4.CollectionUtils;
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
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath*:neatlogic/resources/" + paramObj.getString("moduleId") + "/changelog/*/");
        for (Resource resource : resources) {
            String fileName = resource.getURL().getPath().substring(0, resource.getURL().getPath().lastIndexOf("/"));
            String version = fileName.substring(fileName.lastIndexOf("/") + 1);
            list.add(version);
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

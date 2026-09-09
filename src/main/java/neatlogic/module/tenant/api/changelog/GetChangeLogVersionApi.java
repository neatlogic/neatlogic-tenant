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
package neatlogic.module.tenant.api.changelog;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONReader;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.NoAuth;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.exception.changelog.ChangelogVersionInvalidException;
import neatlogic.framework.exception.type.ParamIrregularException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.TimeUtil;
import neatlogic.module.tenant.dto.ChangelogVersionVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@AuthAction(action = NoAuth.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class GetChangeLogVersionApi extends PrivateApiComponentBase {
    @Override
    public String getName() {
        return "nmtac.getchangelogversionapi.getname";
    }

    @Input({
            @Param(name = "moduleId", type = ApiParamType.STRING, isRequired = true, desc = "term.cmdb.moduleid"),
            @Param(name = "date", type = ApiParamType.STRING, isRequired = true, desc = "common.time")
    })
    @Output({
            @Param(name = "neatlogic", type = ApiParamType.JSONARRAY, desc = "nmtac.getchangelogversionapi.output.param.neatlogic"),
            @Param(name = "neatlogic_tenant", type = ApiParamType.JSONARRAY, desc = "nmtac.getchangelogversionapi.output.param.neatlogictenant"),
            @Param(name = "version", type = ApiParamType.JSONARRAY, desc = "nmtac.getchangelogversionapi.output.param.version")
    })

    @Example(title = "common.example", description = "nf.api.example.replacevalues", example = "{\"moduleId\":\"framework\",\"date\":\"2026-09-01\"}")
    @Description(desc = "nmtac.getchangelogversionapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        String dateString = paramObj.getString("date");
        Date date = TimeUtil.convertStringToDate(dateString, TimeUtil.YYYY_MM_DD);
        if (date == null) {
            throw new ParamIrregularException("date");
        }
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        JSONObject result = new JSONObject();
        Resource[] resources = resolver.getResources("classpath*:neatlogic/resources/" + paramObj.getString("moduleId") + "/changelog/" + dateString + "/*");
        for (Resource resource : resources) {
            String sqlFileName = resource.getFilename();
            if (StringUtils.isNotBlank(sqlFileName) && sqlFileName.endsWith(".sql")) {
                InputStreamReader reader = new InputStreamReader(resource.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(reader);
                String line;
                List<String> sqlLineList = new ArrayList<>();
                while ((line = bufferedReader.readLine()) != null) {
                    sqlLineList.add(line);
                }
                result.put(sqlFileName.substring(0, sqlFileName.indexOf(".")), sqlLineList);
            } else {
                InputStreamReader reader = new InputStreamReader(resource.getInputStream());
                JSONReader jsonReader = new JSONReader(reader);
                JSONObject jsonObject = JSONObject.parseObject(jsonReader.readString());
                try {
                    result.put("version", jsonObject.toJavaObject(ChangelogVersionVo.class));
                } catch (JSONException ex) {
                    throw new ChangelogVersionInvalidException(paramObj.getString("moduleId"), dateString);
                }
            }
        }
        return result;
    }

    @Override
    public String getToken() {
        return "/module/changelog/get";
    }
}

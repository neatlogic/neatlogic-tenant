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

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONReader;
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

    @Example(example = "{\n" +
            "    \"Status\": \"OK\",\n" +
            "    \"Return\": {\n" +
            "        \"dml\": [\n" +
            "            \"select * from user\"\n" +
            "        ],\n" +
            "        \"dll\": [\n" +
            "            \"CREATE TABLE IF NOT EXISTS `api_access_count` (\",\n" +
            "            \"  `token` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'token',\",\n" +
            "            \"  `count` int DEFAULT NULL COMMENT '访问次数',\",\n" +
            "            \"  PRIMARY KEY (`token`) USING BTREE\",\n" +
            "            \") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='接口访问次数表';\"\n" +
            "        ],\n" +
            "        \"version\": {\n" +
            "            \"content\": [\n" +
            "                {\n" +
            "                    \"detail\": [\n" +
            "                        {\n" +
            "                            \"msg\": \"1.增加版本日志。\"\n" +
            "                        },\n" +
            "                        {\n" +
            "                            \"msg\": \"2.增加流水线导入导出。\"\n" +
            "                        }\n" +
            "                    ],\n" +
            "                    \"type\": \"新增功能\"\n" +
            "                },\n" +
            "                {\n" +
            "                    \"detail\": [\n" +
            "                        {\n" +
            "                            \"msg\": \"1.修复自定义工具库-编辑自定义工具并保存为草稿成功，返回路由丢失的问题\"\n" +
            "                        },\n" +
            "                        {\n" +
            "                            \"msg\": \"2.修复工具分类-修改授权对象并保存，再次编辑时授权对象没有变化的问题。\"\n" +
            "                        },\n" +
            "                        {\n" +
            "                            \"msg\": \"3.修复组合工具作业引用矩阵参数默认值异常的问题。\"\n" +
            "                        }\n" +
            "                    ],\n" +
            "                    \"type\": \"修复缺陷\"\n" +
            "                }\n" +
            "            ]\n" +
            "        }\n" +
            "    },\n" +
            "    \"TimeCost\": 5\n" +
            "}")
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

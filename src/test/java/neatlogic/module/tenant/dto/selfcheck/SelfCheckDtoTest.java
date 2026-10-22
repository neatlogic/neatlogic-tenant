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
package neatlogic.module.tenant.dto.selfcheck;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.junit.Assert;
import org.junit.Test;

/** 验证自检配置与历史结果对前端输出稳定的 JSON 结构。 */
public class SelfCheckDtoTest {

    /** 请求参数输出为对象而不是数据库保存使用的 JSON 字符串。 */
    @Test
    public void shouldExposeRequestParameterAsObject() {
        SelfCheckScenarioItemVo itemVo = new SelfCheckScenarioItemVo();
        JSONObject request = new JSONObject();
        request.put("name", "demo");
        itemVo.setRequestParamObj(request);

        JSONObject result = (JSONObject) JSON.toJSON(itemVo);
        Assert.assertEquals("demo", result.getJSONObject("requestParam").getString("name"));
        Assert.assertFalse(result.containsKey("requestParamObj"));
    }

    /** 运行详情将持久化字符串还原为页面可直接消费的对象和数组。 */
    @Test
    public void shouldParseRunItemDetailJson() {
        SelfCheckRunItemVo itemVo = new SelfCheckRunItemVo();
        itemVo.setRequestSnapshot("{\"id\":1}");
        itemVo.setResponseSnapshot("{\"reachable\":true}");
        itemVo.setScriptResult("true");
        itemVo.setPhaseList("[{\"phase\":\"TARGET_EXECUTE\",\"status\":\"SUCCEED\"}]");
        itemVo.setConsoleLogList("[{\"level\":\"log\",\"message\":\"ok\"}]");
        itemVo.setDiagnosticList("[]");
        itemVo.setTruncatedFieldList("[\"responseSnapshot\"]");

        JSONObject detail = itemVo.toDetailJson();
        Assert.assertEquals(1L, detail.getJSONObject("requestSnapshot").getLongValue("id"));
        Assert.assertTrue(detail.getJSONObject("responseSnapshot").getBooleanValue("reachable"));
        Assert.assertTrue(detail.getBooleanValue("scriptResult"));
        JSONArray phaseList = detail.getJSONArray("phaseList");
        Assert.assertEquals("TARGET_EXECUTE", phaseList.getJSONObject(0).getString("phase"));
        Assert.assertEquals("ok", detail.getJSONArray("consoleLogList").getJSONObject(0).getString("message"));
        Assert.assertTrue(detail.getBooleanValue("responseTruncated"));
        Assert.assertFalse(detail.getBooleanValue("requestTruncated"));

        itemVo.setScriptResult("false");
        Assert.assertFalse(itemVo.toDetailJson().getBooleanValue("scriptResult"));
    }

    /** 显式设置的数据库 ID 不被延迟 ID 生成逻辑覆盖。 */
    @Test
    public void shouldPreserveExplicitId() {
        SelfCheckScenarioVo scenarioVo = new SelfCheckScenarioVo();
        scenarioVo.setId(101L);
        Assert.assertEquals(Long.valueOf(101L), scenarioVo.getId());
    }

    /** 执行记录保留负责恢复遗留状态的服务节点标识。 */
    @Test
    public void shouldPreserveRunServerId() {
        SelfCheckRunVo runVo = new SelfCheckRunVo();
        runVo.setServerId(7);
        Assert.assertEquals(Integer.valueOf(7), runVo.getServerId());
    }
}

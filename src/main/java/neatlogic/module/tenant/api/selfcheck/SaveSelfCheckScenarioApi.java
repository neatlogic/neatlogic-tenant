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
package neatlogic.module.tenant.api.selfcheck;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.SELF_CHECK_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.tenant.dto.selfcheck.SelfCheckScenarioItemVo;
import neatlogic.module.tenant.dto.selfcheck.SelfCheckScenarioVo;
import neatlogic.module.tenant.service.selfcheck.SelfCheckService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
@AuthAction(action = SELF_CHECK_MODIFY.class)
@OperationType(type = OperationTypeEnum.UPDATE)
public class SaveSelfCheckScenarioApi extends PrivateApiComponentBase {
    @Resource
    private SelfCheckService selfCheckService;

    /** 返回 API 标识。 */
    @Override
    public String getToken() {
        return "selfcheck/scenario/save";
    }

    /** 返回 API 名称。 */
    @Override
    public String getName() {
        return "保存功能自检场景";
    }

    /** 保存场景配置并固化脚本校验结果。 */
    @Input({
            @Param(name = "id", type = ApiParamType.LONG, desc = "场景ID"),
            @Param(name = "name", type = ApiParamType.STRING, isRequired = true, maxLength = 100, xss = true, desc = "场景名称"),
            @Param(name = "description", type = ApiParamType.STRING, maxLength = 500, xss = true, desc = "说明"),
            @Param(name = "status", type = ApiParamType.ENUM, rule = "DRAFT,ENABLED,DISABLED", desc = "场景状态"),
            @Param(name = "version", type = ApiParamType.INTEGER, desc = "版本"),
            @Param(name = "itemList", type = ApiParamType.JSONARRAY, desc = "检查项列表")
    })
    @Description(desc = "保存功能自检场景，脚本无效时允许保存草稿")
    @Override
    public Object myDoService(JSONObject paramObj) {
        SelfCheckScenarioVo scenarioVo = paramObj.toJavaObject(SelfCheckScenarioVo.class);
        JSONArray itemArray = paramObj.getJSONArray("itemList");
        List<SelfCheckScenarioItemVo> itemList = new ArrayList<>();
        if (itemArray != null) {
            for (int i = 0; i < itemArray.size(); i++) {
                JSONObject itemObj = itemArray.getJSONObject(i);
                JSONObject itemBaseObj = new JSONObject(itemObj);
                itemBaseObj.remove("requestParam");
                SelfCheckScenarioItemVo itemVo = itemBaseObj.toJavaObject(SelfCheckScenarioItemVo.class);
                itemVo.setRequestParamObj(itemObj.getJSONObject("requestParam"));
                itemList.add(itemVo);
            }
        }
        scenarioVo.setItemList(itemList);
        return selfCheckService.saveScenario(scenarioVo);
    }
}

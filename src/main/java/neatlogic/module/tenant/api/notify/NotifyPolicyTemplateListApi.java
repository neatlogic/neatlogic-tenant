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

package neatlogic.module.tenant.api.notify;

import java.util.ArrayList;
import java.util.List;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.common.util.PageUtil;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.notify.core.INotifyHandler;
import neatlogic.framework.notify.core.NotifyHandlerFactory;
import neatlogic.framework.notify.dao.mapper.NotifyMapper;
import neatlogic.framework.notify.dto.NotifyPolicyConfigVo;
import neatlogic.framework.notify.dto.NotifyPolicyVo;
import neatlogic.framework.notify.dto.NotifyTemplateVo;
import neatlogic.framework.notify.exception.NotifyHandlerNotFoundException;
import neatlogic.framework.notify.exception.NotifyPolicyNotFoundException;

@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class NotifyPolicyTemplateListApi extends PrivateApiComponentBase {

    @Autowired
    private NotifyMapper notifyMapper;

    @Override
    public String getToken() {
        return "notify/policy/template/list";
    }

    @Override
    public String getName() {
        return "??????????????????????????????";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "policyId", type = ApiParamType.LONG, isRequired = true, desc = "??????id"),
            @Param(name = "notifyHandler", type = ApiParamType.STRING, desc = "???????????????"),
            @Param(name = "keyword", type = ApiParamType.STRING, desc = "?????????", xss=true),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "?????????"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "??????????????????")
    })
    @Output({
            @Param(explode = BasePageVo.class),
            @Param(name = "templateList", explode = NotifyTemplateVo[].class, desc = "??????????????????")
    })
    @Description(desc = "??????????????????????????????")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long policyId = jsonObj.getLong("policyId");
        NotifyPolicyVo notifyPolicyVo = notifyMapper.getNotifyPolicyById(policyId);
        if (notifyPolicyVo == null) {
            throw new NotifyPolicyNotFoundException(policyId.toString());
        }

        String notifyHandler = jsonObj.getString("notifyHandler");
        if (StringUtils.isNotBlank(notifyHandler)) {
            INotifyHandler handler = NotifyHandlerFactory.getHandler(notifyHandler);
            if (handler == null) {
                throw new NotifyHandlerNotFoundException(notifyHandler);
            }
        }

        List<NotifyTemplateVo> templateList = new ArrayList<>();
        BasePageVo basePageVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<NotifyTemplateVo>(){});

        NotifyPolicyConfigVo config = notifyPolicyVo.getConfig();
        for (NotifyTemplateVo notifyTemplateVo : config.getTemplateList()) {
            if (StringUtils.isNotBlank(notifyHandler) && !notifyHandler.equals(notifyTemplateVo.getNotifyHandler())) {
                continue;
            }
            if (StringUtils.isNotBlank(basePageVo.getKeyword()) && !notifyTemplateVo.getName().toLowerCase().contains(basePageVo.getKeyword().toLowerCase())) {
                continue;
            }
            templateList.add(notifyTemplateVo);
        }
        JSONObject resultObj = new JSONObject();
        resultObj.put("currentPage", basePageVo.getCurrentPage());
        resultObj.put("pageSize", basePageVo.getPageSize());
        int rowNum = 0;
        int pageCount = 0;
        if(CollectionUtils.isNotEmpty(templateList)){
            templateList.sort((e1, e2) -> e2.getActionTime().compareTo(e1.getActionTime()));
            rowNum = templateList.size();
            pageCount = PageUtil.getPageCount(rowNum, basePageVo.getPageSize());
            resultObj.put("pageCount", pageCount);
            resultObj.put("rowNum", rowNum);
            int fromIndex = basePageVo.getStartNum();
            if(fromIndex < rowNum) {
                int toIndex = fromIndex + basePageVo.getPageSize();
                toIndex = toIndex > rowNum ? rowNum : toIndex;
                templateList = templateList.subList(fromIndex, toIndex);
            }else{
                templateList = new ArrayList<>();
            }
        }

        resultObj.put("pageCount", pageCount);
        resultObj.put("rowNum", rowNum);
        resultObj.put("templateList", templateList);
        return resultObj;
    }

}

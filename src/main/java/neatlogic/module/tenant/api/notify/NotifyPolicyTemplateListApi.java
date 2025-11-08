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

package neatlogic.module.tenant.api.notify;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.NoAuth;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.common.util.PageUtil;
import neatlogic.framework.notify.core.INotifyHandler;
import neatlogic.framework.notify.core.NotifyHandlerFactory;
import neatlogic.framework.notify.dao.mapper.NotifyMapper;
import neatlogic.framework.notify.dto.NotifyPolicyConfigVo;
import neatlogic.framework.notify.dto.NotifyPolicyVo;
import neatlogic.framework.notify.dto.NotifyTemplateVo;
import neatlogic.framework.notify.exception.NotifyHandlerNotFoundException;
import neatlogic.framework.notify.exception.NotifyPolicyNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AuthAction(action = NoAuth.class)
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
        return "通知策略模板列表接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "policyId", type = ApiParamType.LONG, isRequired = true, desc = "策略id"),
            @Param(name = "notifyHandler", type = ApiParamType.STRING, desc = "通知处理器"),
            @Param(name = "keyword", type = ApiParamType.STRING, desc = "关键字", xss=true),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页数据条目")
    })
    @Output({
            @Param(explode = BasePageVo.class),
            @Param(name = "templateList", explode = NotifyTemplateVo[].class, desc = "通知模板列表")
    })
    @Description(desc = "通知策略模板列表接口")
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

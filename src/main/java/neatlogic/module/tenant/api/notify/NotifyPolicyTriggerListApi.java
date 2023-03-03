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

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.common.util.PageUtil;
import neatlogic.framework.dto.ConditionParamVo;
import neatlogic.framework.dto.UserTypeVo;
import neatlogic.framework.notify.core.INotifyPolicyHandler;
import neatlogic.framework.notify.core.NotifyPolicyHandlerFactory;
import neatlogic.framework.notify.dao.mapper.NotifyMapper;
import neatlogic.framework.notify.dto.NotifyPolicyConfigVo;
import neatlogic.framework.notify.dto.NotifyPolicyVo;
import neatlogic.framework.notify.dto.NotifyTriggerVo;
import neatlogic.framework.notify.exception.NotifyPolicyHandlerNotFoundException;
import neatlogic.framework.notify.exception.NotifyPolicyNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.usertype.UserTypeFactory;
import neatlogic.module.tenant.service.notify.NotifyPolicyService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class NotifyPolicyTriggerListApi extends PrivateApiComponentBase {

    @Autowired
    private NotifyMapper notifyMapper;

    @Autowired
    private NotifyPolicyService notifyPolicyService;

    @Override
    public String getToken() {
        return "notify/policy/trigger/list";
    }

    @Override
    public String getName() {
        return "通知策略触发类型列表接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "policyId", type = ApiParamType.LONG, isRequired = true, desc = "策略id"),
            @Param(name = "hasAction", type = ApiParamType.ENUM, rule = "-1,0,1", desc = "是否有动作,-1:全部;0:无动作;1:有动作"),
            @Param(name = "keyword", type = ApiParamType.STRING, desc = "关键字", xss=true),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页数据条目"),
            @Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否需要分页，默认true")
    })
    @Output({
            @Param(explode = BasePageVo.class),
            @Param(name = "triggerList", explode = ConditionParamVo[].class, desc = "参数列表")
    })
    @Description(desc = "通知策略触发类型列表接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        List<NotifyTriggerVo> resultList = new ArrayList<>();
        Long policyId = jsonObj.getLong("policyId");
        Integer hasAction = jsonObj.getInteger("hasAction") == null ? -1 : jsonObj.getInteger("hasAction");
        BasePageVo basePageVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<BasePageVo>(){});
        NotifyPolicyVo notifyPolicyVo = notifyMapper.getNotifyPolicyById(policyId);
        if (notifyPolicyVo == null) {
            throw new NotifyPolicyNotFoundException(policyId.toString());
        }
        INotifyPolicyHandler notifyPolicyHandler = NotifyPolicyHandlerFactory.getHandler(notifyPolicyVo.getHandler());
        if (notifyPolicyHandler == null) {
            throw new NotifyPolicyHandlerNotFoundException(notifyPolicyVo.getHandler());
        }
        List<NotifyTriggerVo> systemTriggerList = notifyPolicyHandler.getNotifyTriggerList();
        /** 获取工单干系人枚举 */
        //TODO 没有兼容多模块
        Map<String, UserTypeVo> userTypeVoMap = UserTypeFactory.getUserTypeMap();
        UserTypeVo UsertypeVo = userTypeVoMap.get("process");
        Map<String, String> processUserType = UsertypeVo.getValues();

        NotifyPolicyConfigVo config = notifyPolicyVo.getConfig();
        List<NotifyTriggerVo> triggerList = config.getTriggerList();
        /** 矫正旧配置数据中的触发点 */
        /** 多删 -- 删除已经不存在的触发点 */
        Iterator<NotifyTriggerVo> iterator = triggerList.iterator();
        while (iterator.hasNext()){
            NotifyTriggerVo next = iterator.next();
            if(!systemTriggerList.stream().anyMatch(o -> o.getTrigger().equals(next.getTrigger()))){
                iterator.remove();
            }
        }
        /** 少补 -- 新增老数据中没有而现在有的触发点 */
        Iterator<NotifyTriggerVo> _iterator = systemTriggerList.iterator();
        while (_iterator.hasNext()){
            NotifyTriggerVo next = _iterator.next();
            if(!triggerList.stream().anyMatch(o -> o.getTrigger().equals(next.getTrigger()))){
                triggerList.add(next);
            }
        }
        for (NotifyTriggerVo vo : triggerList) {
            if (StringUtils.isNotBlank(basePageVo.getKeyword())) {
                if (!vo.getTriggerName().toLowerCase().contains(basePageVo.getKeyword().toLowerCase())) {
                    continue;
                }
            }
            if(hasAction == 1 && CollectionUtils.isEmpty(vo.getNotifyList())){
                continue;
            }
            if(hasAction == 0 && CollectionUtils.isNotEmpty(vo.getNotifyList())){
                continue;
            }
            /** 补充通知对象详细信息 */
            notifyPolicyService.addReceiverExtraInfo(processUserType, vo);

            resultList.add(vo);
        }
        JSONObject resultObj = new JSONObject();
        if(basePageVo.getNeedPage()){
            int rowNum = resultList.size();
            int pageCount = PageUtil.getPageCount(rowNum, basePageVo.getPageSize());
            resultObj.put("currentPage", basePageVo.getCurrentPage());
            resultObj.put("pageSize", basePageVo.getPageSize());
            resultObj.put("pageCount", pageCount);
            resultObj.put("rowNum", rowNum);
            int fromIndex = basePageVo.getStartNum();
            if(fromIndex < rowNum) {
                int toIndex = fromIndex + basePageVo.getPageSize();
                toIndex = toIndex > rowNum ? rowNum : toIndex;
                resultList = resultList.subList(fromIndex, toIndex);
            }else{
                resultList = new ArrayList<>();
            }
        }
        resultObj.put("triggerList", resultList);
        return resultObj;
    }

}

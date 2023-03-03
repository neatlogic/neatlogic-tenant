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

package neatlogic.module.tenant.api.mq;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.MQ_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.mq.dao.mapper.MqSubscribeMapper;
import neatlogic.framework.mq.dto.SubscribeVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@AuthAction(action = MQ_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class SearchSubscribeApi extends PrivateApiComponentBase {

    @Resource
    private MqSubscribeMapper mqSubscribeMapper;

    @Override
    public String getToken() {
        return "/mq/subscribe/search";
    }

    @Override
    public String getName() {
        return "消息队列订阅查询";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "keyword", type = ApiParamType.STRING, xss = true, desc = "通知插件列表"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页数"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页大小")})
    @Output({@Param(explode = BasePageVo.class), @Param(name = "tbodyList", explode = SubscribeVo[].class)})
    @Description(desc = "消息队列订阅查询接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        SubscribeVo subscribeVo = JSONObject.toJavaObject(jsonObj, SubscribeVo.class);
        int rowNum = mqSubscribeMapper.searchSubscribeCount(subscribeVo);
        JSONObject returnObj = new JSONObject();
        subscribeVo.setRowNum(rowNum);
        subscribeVo.setCurrentPage(subscribeVo.getCurrentPage());
        subscribeVo.setPageSize(subscribeVo.getPageSize());
        subscribeVo.setPageCount(subscribeVo.getPageCount());
        returnObj.put("rowNum", rowNum);
        if (rowNum > 0) {
            returnObj.put("tbodyList", mqSubscribeMapper.searchSubscribe(subscribeVo));
        }
        return returnObj;
    }

}

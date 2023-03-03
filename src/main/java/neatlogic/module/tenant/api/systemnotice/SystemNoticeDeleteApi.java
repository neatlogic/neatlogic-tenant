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

package neatlogic.module.tenant.api.systemnotice;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.systemnotice.dao.mapper.SystemNoticeMapper;
import neatlogic.framework.systemnotice.dto.SystemNoticeVo;
import neatlogic.framework.systemnotice.exception.SystemNoticeNotFoundException;
import neatlogic.framework.auth.label.SYSTEM_NOTICE_MODIFY;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@AuthAction(action = SYSTEM_NOTICE_MODIFY.class)
@Service
@OperationType(type = OperationTypeEnum.DELETE)
@Transactional
public class SystemNoticeDeleteApi extends PrivateApiComponentBase {

    @Resource
    private SystemNoticeMapper systemNoticeMapper;

    @Override
    public String getToken() {
        return "systemnotice/delete";
    }

    @Override
    public String getName() {
        return "删除系统公告";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "id", type = ApiParamType.LONG, isRequired = true, desc = "公告ID")})
    @Output({})
    @Description(desc = "删除系统公告")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        SystemNoticeVo vo = systemNoticeMapper.getSystemNoticeBaseInfoById(jsonObj.getLong("id"));
        if(vo == null){
            throw new SystemNoticeNotFoundException(jsonObj.getLong("id"));
        }
        /** 不再限制删除已下发公告 **/
//        if(SystemNoticeVo.Status.ISSUED.getValue().equals(vo.getStatus())){
//            throw new SystemNoticeHasBeenIssuedException(vo.getTitle());
//        }
        /** 只删除system_notice与system_notice_recipient，
         * system_notice_user由每个用户登录或者pull时自我删除
         **/
        systemNoticeMapper.deleteRecipientByNoticeId(vo.getId());
        systemNoticeMapper.deleteSystemNoticeById(vo.getId());
        return null;
    }
}

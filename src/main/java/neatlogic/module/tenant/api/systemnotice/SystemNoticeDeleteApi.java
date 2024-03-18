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

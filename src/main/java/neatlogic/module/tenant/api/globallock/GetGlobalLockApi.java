/*
 * Copyright (C) 2026  深圳极向量科技有限公司 All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package neatlogic.module.tenant.api.globallock;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.NoAuth;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dto.globallock.GlobalLockVo;
import neatlogic.framework.globallock.GlobalLockOperationManager;
import neatlogic.framework.globallock.dao.mapper.GlobalLockMapper;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import com.alibaba.fastjson.JSON;

/** 提供当前租户的资源锁详情及操作进度接口。 */
@Service
@AuthAction(action=NoAuth.class)
@OperationType(type=OperationTypeEnum.SEARCH)
public class GetGlobalLockApi extends PrivateApiComponentBase {
    @Resource private GlobalLockOperationManager operations;
    @Resource private GlobalLockMapper mapper;
    @Override public String getToken() { return "global/lock/get"; }
    @Override public String getName() { return "globallock.getgloballockapi"; }
    @Override public String getConfig() { return null; }
    /** 仅返回当前信息，过期或已释放记录通过明确状态返回。 */
    @Input({@Param(name="lockId", type=ApiParamType.LONG, isRequired=true, desc="globallock.lockid")})
    @Output({})
    @Description(desc="globallock.getgloballockapi")
    @Override public Object myDoService(JSONObject input) {
        GlobalLockVo lock = mapper.getGlobalLockById(input.getLong("lockId"));
        JSONObject result = new JSONObject(); result.put("released", lock == null);
        if (lock != null) {
            JSONObject data = (JSONObject) JSON.toJSON(lock);
            data.put("id", lock.getId().toString());
            data.putAll(GlobalLockOperationManager.identity(lock));
            result.put("lock", data);
        }
        return result;
    }
}

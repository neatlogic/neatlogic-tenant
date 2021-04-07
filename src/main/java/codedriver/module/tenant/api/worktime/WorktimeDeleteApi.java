/*
 * Copyright(c) 2021 TechSureCo.,Ltd.AllRightsReserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.worktime;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.auth.label.WORKTIME_MODIFY;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dependency.constvalue.CalleeType;
import codedriver.framework.dependency.core.DependencyManager;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.worktime.dao.mapper.WorktimeMapper;
import codedriver.framework.worktime.dto.WorktimeRangeVo;
import codedriver.framework.worktime.dto.WorktimeVo;
import codedriver.framework.worktime.exception.WorktimeHasBeenRelatedByChannelException;
import codedriver.framework.worktime.exception.WorktimeNotFoundException;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@Transactional
@OperationType(type = OperationTypeEnum.DELETE)
@AuthAction(action = WORKTIME_MODIFY.class)
public class WorktimeDeleteApi extends PrivateApiComponentBase {

    @Resource
    private WorktimeMapper worktimeMapper;

    @Override
    public String getToken() {
        return "worktime/delete";
    }

    @Override
    public String getName() {
        return "工作时间窗口删除接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "uuid", type = ApiParamType.STRING, isRequired = true, desc = "工作时间窗口uuid")
    })
    @Description(desc = "工作时间窗口删除接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        /**
         * 如果被服务引用，则不能删除
         * 如果没被服务引用，却被工单引用，则逻辑删除
         */
        String uuid = jsonObj.getString("uuid");
        if (worktimeMapper.checkWorktimeIsExists(uuid) == 0) {
            throw new WorktimeNotFoundException(uuid);
        }
        WorktimeVo worktime = worktimeMapper.getWorktimeByUuid(uuid);
        if (DependencyManager.getDependencyCount(CalleeType.WORKTIME, uuid) > 0) {
            throw new WorktimeHasBeenRelatedByChannelException(worktime.getName());
        }
        if (DependencyManager.getDependencyCount(CalleeType.WORKTIME, uuid, false) > 0) {
            worktime.setIsDelete(1);
            worktime.setLcu(UserContext.get().getUserUuid());
            worktimeMapper.updateWorktimeDeleteStatus(worktime);
        } else {
            worktimeMapper.deleteWorktimeByUuid(uuid);
            WorktimeRangeVo worktimeRangeVo = new WorktimeRangeVo();
            worktimeRangeVo.setWorktimeUuid(uuid);
            worktimeMapper.deleteWorktimeRange(worktimeRangeVo);
        }

        return null;
    }

}

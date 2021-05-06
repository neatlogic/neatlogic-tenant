/*
 * Copyright(c) 2021 TechSureCo.,Ltd.AllRightsReserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.worktime;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.auth.label.FRAMEWORK_BASE;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dependency.constvalue.CalleeType;
import codedriver.framework.dependency.core.DependencyManager;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.worktime.dao.mapper.WorktimeMapper;
import codedriver.framework.worktime.dto.WorktimeVo;
import codedriver.framework.worktime.exception.WorktimeNotFoundException;
import com.alibaba.fastjson.JSONObject;
import org.apache.tools.ant.taskdefs.optional.depend.Depend;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@AuthAction(action = FRAMEWORK_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class WorktimeGetApi extends PrivateApiComponentBase {

    @Resource
    private WorktimeMapper worktimeMapper;

    @Override
    public String getToken() {
        return "worktime/get";
    }

    @Override
    public String getName() {
        return "工作时间窗口信息查询接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "uuid", type = ApiParamType.STRING, isRequired = true, desc = "工作时间窗口uuid")
    })
    @Output({
            @Param(explode = WorktimeVo.class)
    })
    @Description(desc = "工作时间窗口信息查询接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String uuid = jsonObj.getString("uuid");
        WorktimeVo worktime = worktimeMapper.getWorktimeByUuid(uuid);
        if (worktime == null) {
            throw new WorktimeNotFoundException(uuid);
        }
        int count = DependencyManager.getDependencyCount(CalleeType.WORKTIME, uuid);
        worktime.setReferenceCount(count);
        return worktime;
    }

}

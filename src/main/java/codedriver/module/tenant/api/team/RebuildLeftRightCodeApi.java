/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.team;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.auth.label.TEAM_MODIFY;
import codedriver.framework.lrcode.LRCodeManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.lock.core.LockManager;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.tenant.service.TeamService;

@Service
@Transactional
@AuthAction(action = TEAM_MODIFY.class)
@OperationType(type = OperationTypeEnum.UPDATE)
public class RebuildLeftRightCodeApi extends PrivateApiComponentBase {

    @Autowired
    private TeamService teamService;

    @Autowired
    private LockManager lockService;

    @Override
    public String getToken() {
        return "team/rebuildleftrightcode";
    }

    @Override
    public String getName() {
        return "用户组重建左右编码接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
//        lockService.getLockById("team");
//        teamService.rebuildLeftRightCode();
        LRCodeManager.rebuildLeftRightCode("team", "uuid", "parent_uuid");
        return null;
    }

}

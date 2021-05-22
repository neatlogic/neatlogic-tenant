/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.util;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.auth.label.ADMIN;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.lrcode.LRCodeManager;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@AuthAction(action = ADMIN.class)
@OperationType(type = OperationTypeEnum.OPERATE)
public class LRCodeRebuildApi extends PrivateApiComponentBase {


    @Override
    public String getToken() {
        return "/util/lrcoderebuild";
    }

    @Override
    public String getName() {
        return "重建左右编码";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "tableName", type = ApiParamType.STRING, isRequired = true, desc = "表名"),
            @Param(name = "idKey", type = ApiParamType.STRING, isRequired = true, desc = "id字段名"),
            @Param(name = "parentIdKey", type = ApiParamType.STRING, isRequired = true, desc = "父id字段名")})
    @Description(desc = "重建左右编码接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String tableName = jsonObj.getString("tableName");
        String idKey = jsonObj.getString("idKey");
        String parentIdKey = jsonObj.getString("parentIdKey");
        LRCodeManager.rebuildLeftRightCode(tableName, idKey, parentIdKey);
        return null;
    }

}

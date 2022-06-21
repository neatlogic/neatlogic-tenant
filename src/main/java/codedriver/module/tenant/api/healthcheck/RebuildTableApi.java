/*
 * Copyright(c) 2022 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.healthcheck;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.auth.label.ADMIN;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dto.healthcheck.DatabaseFragmentVo;
import codedriver.framework.healthcheck.dao.mapper.DatabaseFragmentMapper;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@AuthAction(action = ADMIN.class)
@OperationType(type = OperationTypeEnum.OPERATE)
public class RebuildTableApi extends PrivateApiComponentBase {

    @Override
    public String getToken() {
        return "/healthcheck/table/rebuild";
    }

    @Override
    public String getName() {
        return "重建表";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Resource
    private DatabaseFragmentMapper databaseFragmentMapper;


    @Input({@Param(name = "tableName", type = ApiParamType.STRING, desc = "表名", isRequired = true), @Param(name = "schemaType", type = ApiParamType.ENUM, rule = "main,data", desc = "库类型", isRequired = true)})
    @Description(desc = "重建表接口，一般空闲空间太大的表才需要进行此操作")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        DatabaseFragmentVo databaseFragmentVo = JSONObject.toJavaObject(paramObj, DatabaseFragmentVo.class);
        databaseFragmentMapper.rebuildTable(databaseFragmentVo.getSchema(), paramObj.getString("tableName"));
        return null;
    }


}

/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.test;

import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.core.publicapi.PublicApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

@Deprecated
@OperationType(type = OperationTypeEnum.SEARCH)
@Service
public class Test2Api extends PublicApiComponentBase {

    @Override
    public String getName() {
        return "测试2";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Description(desc = "测试公共接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        return "test2";
    }

}

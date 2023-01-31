/*
 * Copyright(c) 2022 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package neatlogic.module.tenant.api.constvalue;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.constvalue.EnumFactory;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;


@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class UniversalSearchNamedEnumApi extends PrivateApiComponentBase {
    @Override
    public String getToken() {
        return "universal/enum/search";
    }

    @Override
    public String getName() {
        return "搜索具名枚举";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "keyword", type = ApiParamType.STRING, desc = "枚举名称")})
    @Output({@Param(name = "name", type = ApiParamType.STRING, desc = "名称"),
            @Param(name = "className", type = ApiParamType.STRING, desc = "类路径")})
    @Description(desc = "搜索具名枚举接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        return EnumFactory.searchEnumClassByName(jsonObj.getString("keyword"));
    }
}

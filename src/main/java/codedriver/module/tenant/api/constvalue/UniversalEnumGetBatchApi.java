/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.constvalue;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.constvalue.EnumFactory;
import codedriver.framework.common.constvalue.IEnum;
import codedriver.framework.exception.type.ParamNotExistsException;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.tenant.exception.constvalue.EnumNotFoundException;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class UniversalEnumGetBatchApi extends PrivateApiComponentBase {
    @Override
    public String getToken() {
        return "universal/enum/get/batch";
    }

    @Override
    public String getName() {
        return "根据多个枚举类名获取枚举值";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "enumClassList", type = ApiParamType.JSONARRAY, desc = "枚举完整类名列表", isRequired = true)
    })
    @Output({
            @Param(name = "Return", type = ApiParamType.JSONOBJECT)
    })
    @Description(desc = "根据多个枚举类名获取枚举值")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONArray enumClassArray = jsonObj.getJSONArray("enumClassList");
        if (CollectionUtils.isEmpty(enumClassArray)) {
            throw new ParamNotExistsException("enumClassList");
        }
        JSONObject resultObj = new JSONObject();
        List<String> enumClassList = enumClassArray.toJavaList(String.class);
        for (String enumClass : enumClassList) {
            Class<? extends IEnum> aClass = EnumFactory.getEnumClass(enumClass);
            if (aClass == null) {
                throw new EnumNotFoundException(enumClass);
            }
            Object instance = null;
            Object[] objects = aClass.getEnumConstants();
            if (objects != null && objects.length > 0) {
                instance = objects[0];
            } else {
                instance = aClass.newInstance();
            }
            resultObj.put(enumClass, aClass.getMethod("getValueTextList").invoke(instance));
        }
        return resultObj;
    }
}

package codedriver.module.tenant.api.constvalue;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.constvalue.EnumFactory;
import codedriver.framework.common.constvalue.IEnum;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.tenant.exception.constvalue.EnumNotFoundException;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * @Title: UniversalEnumGetApi
 * @Package: codedriver.module.tenant.api.constvalue
 * @Description: 通用的枚举获取接口
 * @Author: laiwt
 * @Date: 2021/1/12 11:44
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 **/
@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class UniversalEnumGetApi extends PrivateApiComponentBase {
    @Override
    public String getToken() {
        return "universal/enum/get";
    }

    @Override
    public String getName() {
        return "根据枚举类名获取枚举值";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "enumClass", type = ApiParamType.STRING, desc = "枚举完整类名", isRequired = true)})
    @Output({@Param(explode = List.class)})
    @Description(desc = "根据枚举类名获取枚举值")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String enumClass = jsonObj.getString("enumClass");
        try{
            Class.forName(enumClass);
        }catch (ClassNotFoundException ex){
            throw new EnumNotFoundException(enumClass);
        }
        Class<? extends IEnum> aClass = EnumFactory.getEnumClass(enumClass);
        Object[] objects = aClass.getEnumConstants();
        return aClass.getMethod("getValueTextList").invoke(objects[0]);
    }
}

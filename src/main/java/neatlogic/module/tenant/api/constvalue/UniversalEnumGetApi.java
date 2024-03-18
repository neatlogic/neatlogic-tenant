/*Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

package neatlogic.module.tenant.api.constvalue;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.constvalue.EnumFactory;
import neatlogic.framework.common.constvalue.IEnum;
import neatlogic.framework.reflection.ReflectionManager;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.tenant.exception.constvalue.EnumNotFoundException;
import org.reflections.Reflections;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


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
        Class<? extends IEnum> aClass = EnumFactory.getEnumClass(enumClass);
        if (aClass == null) {
            throw new EnumNotFoundException(enumClass);
        }
        if (aClass.isInterface()) {
            List<Object> valueTextList = new ArrayList<>();
            Reflections reflections = ReflectionManager.getInstance();
            for (Class<?> cls : reflections.getSubTypesOf(aClass)) {
                if (!cls.isInterface()) {
                    Object instance;
                    Object[] objects = cls.getEnumConstants();
                    if (objects != null && objects.length > 0) {
                        instance = objects[0];
                    } else {
                        instance = cls.newInstance();
                    }
                    List<?> list = (List<?>) cls.getMethod("getValueTextList").invoke(instance);
                    valueTextList.addAll(list);
                }
            }
            return valueTextList;
        } else {
            Object instance;
            Object[] objects = aClass.getEnumConstants();
            if (objects != null && objects.length > 0) {
                instance = objects[0];
            } else {
                instance = aClass.newInstance();
            }
            return aClass.getMethod("getValueTextList").invoke(instance);
        }
    }
}

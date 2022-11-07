/*
 * Copyright(c) 2022 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.form;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.exception.type.ParamNotExistsException;
import codedriver.framework.form.dao.mapper.FormMapper;
import codedriver.framework.form.dto.FormCustomItemVo;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class GetFormCustomItemApi extends PrivateApiComponentBase {
    @Resource
    private FormMapper formMapper;

    @Override
    public String getName() {
        return "获取表单自定义组件";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "id", type = ApiParamType.LONG, desc = "组件id"),
            @Param(name = "name", type = ApiParamType.STRING, desc = "组件唯一标识")})
    @Output({@Param(explode = FormCustomItemVo.class)})
    @Description(desc = "获取表单自定义组件接口")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        Long id = paramObj.getLong("id");
        String name = paramObj.getString("name");
        if (id == null && StringUtils.isBlank(name)) {
            throw new ParamNotExistsException("id", "name");
        }
        if (id != null) {
            return formMapper.getFormCustomItemById(paramObj.getLong("id"));
        } else {
            return formMapper.getFormCustomItemByName(name);
        }
    }

    @Override
    public String getToken() {
        return "/form/customitem/get";
    }
}

package neatlogic.module.tenant.api.form;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.FORM_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dto.FieldValidResultVo;
import neatlogic.framework.form.dao.mapper.FormMapper;
import neatlogic.framework.form.dto.FormVo;
import neatlogic.framework.form.exception.FormNameRepeatException;
import neatlogic.framework.form.exception.FormNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.IValid;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.RegexUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@OperationType(type = OperationTypeEnum.UPDATE)
@AuthAction(action = FORM_MODIFY.class)
public class FormUpdateApi extends PrivateApiComponentBase {

    @Autowired
    private FormMapper formMapper;

    @Override
    public String getToken() {
        return "form/update";
    }

    @Override
    public String getName() {
        return "表单基本信息更新接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "uuid", type = ApiParamType.STRING, isRequired = true, desc = "表单uuid"),
            @Param(name = "name", type = ApiParamType.REGEX, rule = RegexUtils.NAME, isRequired = true, maxLength = 50, desc = "表单名称"),
            @Param(name = "isActive", type = ApiParamType.ENUM, rule = "0,1", desc = "是否激活")
    })
    @Output({
            @Param(name = "uuid", type = ApiParamType.STRING, desc = "表单uuid")
    })
    @Description(desc = "表单基本信息更新接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        FormVo formVo = JSON.toJavaObject(jsonObj, FormVo.class);
        //判断表单是否存在
        if (formMapper.checkFormIsExists(formVo.getUuid()) == 0) {
            throw new FormNotFoundException(formVo.getUuid());
        }
        //判断名称是否重复
        if (formVo.getName() != null && formMapper.checkFormNameIsRepeat(formVo) > 0) {
            throw new FormNameRepeatException(formVo.getName());
        }
        formMapper.updateForm(formVo);
        return formVo.getUuid();
    }

    public IValid name() {
        return value -> {
            FormVo formVo = JSON.toJavaObject(value, FormVo.class);
            if (formMapper.checkFormNameIsRepeat(formVo) > 0) {
                return new FieldValidResultVo(new FormNameRepeatException(formVo.getName()));
            }
            return new FieldValidResultVo();
        };
    }

}

package neatlogic.module.tenant.api.form;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.FORM_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.form.dao.mapper.FormMapper;
import neatlogic.framework.form.dto.FormVersionVo;
import neatlogic.framework.form.exception.FormActiveVersionCannotBeDeletedException;
import neatlogic.framework.form.exception.FormVersionNotFoundException;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.FormUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@OperationType(type = OperationTypeEnum.DELETE)
@AuthAction(action = FORM_MODIFY.class)
public class FormVersionDeleteApi extends PrivateApiComponentBase {

    @Autowired
    private FormMapper formMapper;

    @Override
    public String getToken() {
        return "form/version/delete";
    }

    @Override
    public String getName() {
        return "nmtaf.formversiondeleteapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "uuid", type = ApiParamType.STRING, isRequired = true, desc = "common.uuid")
    })
    @Description(desc = "nmtaf.formversiondeleteapi.getname")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String uuid = jsonObj.getString("uuid");
        FormVersionVo formVersion = formMapper.getFormVersionByUuid(uuid);
        //判断被删除的表单版本是否存在
        if (formVersion == null) {
            throw new FormVersionNotFoundException(uuid);
        } else if (formVersion.getIsActive().intValue() == 1) {//当前激活版本不能删除
            throw new FormActiveVersionCannotBeDeletedException(uuid);
        }
        //删除表单版本
        formMapper.deleteFormVersionByUuid(uuid);
        FormUtil.deleteDependency(formVersion);
        return null;
    }

}

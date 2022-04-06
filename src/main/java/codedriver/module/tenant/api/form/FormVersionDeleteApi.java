package codedriver.module.tenant.api.form;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.auth.label.FORM_MODIFY;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dependency.core.DependencyManager;
import codedriver.framework.form.dao.mapper.FormMapper;
import codedriver.framework.form.dto.FormAttributeVo;
import codedriver.framework.form.dto.FormVersionVo;
import codedriver.framework.form.exception.FormActiveVersionCannotBeDeletedException;
import codedriver.framework.form.exception.FormVersionNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.framework.dependency.handler.Integration2FormAttrDependencyHandler;
import codedriver.module.framework.dependency.handler.MatrixAttr2FormAttrDependencyHandler;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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
        return "表单版本删除接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "uuid", type = ApiParamType.STRING, isRequired = true, desc = "表单版本uuid")
    })
    @Description(desc = "表单版本删除接口")
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
        formMapper.deleteFormAttributeMatrixByFormVersionUuid(uuid);
        List<FormAttributeVo> formAttributeList = formVersion.getFormAttributeList();
        if (CollectionUtils.isNotEmpty(formAttributeList)) {
            for (FormAttributeVo formAttributeVo : formAttributeList) {
                DependencyManager.delete(MatrixAttr2FormAttrDependencyHandler.class, formAttributeVo.getUuid());
                DependencyManager.delete(Integration2FormAttrDependencyHandler.class, formAttributeVo.getUuid());
            }
        }
        return null;
    }

}

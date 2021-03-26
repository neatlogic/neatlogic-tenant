package codedriver.module.tenant.api.form;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.auth.label.FORM_MODIFY;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.form.dao.mapper.FormMapper;
import codedriver.framework.form.dto.FormAttributeVo;
import codedriver.framework.form.dto.FormVersionVo;
import codedriver.framework.form.exception.FormIllegalParameterException;
import codedriver.framework.form.exception.FormNotFoundException;
import codedriver.framework.form.exception.FormVersionNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@OperationType(type = OperationTypeEnum.UPDATE)
@AuthAction(action = FORM_MODIFY.class)
public class FormVersionActiveApi extends PrivateApiComponentBase {

    @Autowired
    private FormMapper formMapper;

    @Override
    public String getToken() {
        return "form/version/active";
    }

    @Override
    public String getName() {
        return "表单版本激活接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "uuid", type = ApiParamType.STRING, isRequired = true, desc = "表单uuid"),
            @Param(name = "versionUuid", type = ApiParamType.STRING, isRequired = true, desc = "表单版本uuid")
    })
    @Description(desc = "表单版本激活接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String uuid = jsonObj.getString("uuid");
        //判断表单是否存在
        if (formMapper.checkFormIsExists(uuid) == 0) {
            throw new FormNotFoundException(uuid);
        }

        String versionUuid = jsonObj.getString("versionUuid");
        //判断被激活的表单版本是否存在
        FormVersionVo formVersion = formMapper.getFormVersionByUuid(versionUuid);
        if (formVersion == null) {
            throw new FormVersionNotFoundException(versionUuid);
        }
        if (!uuid.equals(formVersion.getFormUuid())) {
            throw new FormIllegalParameterException("表单版本：'" + versionUuid + "'不属于表单：'" + uuid + "'的版本");
        }
        //将所有版本设置为非激活状态
        formMapper.resetFormVersionIsActiveByFormUuid(uuid);
        FormVersionVo formVersionVo = new FormVersionVo();
        formVersionVo.setUuid(versionUuid);
        //将当前版本设置为激活版本
        formVersionVo.setIsActive(1);
        formMapper.updateFormVersion(formVersionVo);

        formMapper.deleteFormAttributeByFormUuid(uuid);
        List<FormAttributeVo> formAttributeList = formVersion.getFormAttributeList();
        if (CollectionUtils.isNotEmpty(formAttributeList)) {
            for (FormAttributeVo formAttributeVo : formAttributeList) {
                formMapper.insertFormAttribute(formAttributeVo);
            }
        }

        return null;
    }

}

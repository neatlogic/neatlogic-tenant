package neatlogic.module.tenant.api.form;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.FORM_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.form.dao.mapper.FormMapper;
import neatlogic.framework.form.dto.FormAttributeVo;
import neatlogic.framework.form.dto.FormVersionVo;
import neatlogic.framework.form.exception.FormNotFoundException;
import neatlogic.framework.form.exception.FormVersionNotFoundException;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
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
            @Param(name = "versionUuid", type = ApiParamType.STRING, isRequired = true, desc = "表单版本uuid")
    })
    @Description(desc = "表单版本激活接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {

        String versionUuid = jsonObj.getString("versionUuid");
        //判断被激活的表单版本是否存在
        FormVersionVo formVersion = formMapper.getFormVersionByUuid(versionUuid);
        if (formVersion == null) {
            throw new FormVersionNotFoundException(versionUuid);
        }
        //判断表单是否存在
        if (formMapper.checkFormIsExists(formVersion.getFormUuid()) == 0) {
            throw new FormNotFoundException(formVersion.getFormUuid());
        }
        //将所有版本设置为非激活状态
        formMapper.resetFormVersionIsActiveByFormUuid(formVersion.getFormUuid());
        FormVersionVo formVersionVo = new FormVersionVo();
        formVersionVo.setUuid(versionUuid);
        //将当前版本设置为激活版本
        formVersionVo.setIsActive(1);
        formMapper.updateFormVersion(formVersionVo);

        formMapper.deleteFormAttributeByFormUuid(formVersion.getFormUuid());
        String mainSceneUuid = formVersion.getFormConfig().getString("uuid");
        formVersion.setSceneUuid(mainSceneUuid);
        List<FormAttributeVo> formAttributeList = formVersion.getFormAttributeList();
        if (CollectionUtils.isNotEmpty(formAttributeList)) {
            for (FormAttributeVo formAttributeVo : formAttributeList) {
                formMapper.insertFormAttribute(formAttributeVo);
            }
        }

        return null;
    }

}

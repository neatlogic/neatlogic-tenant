package neatlogic.module.tenant.api.form;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.FORM_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dependency.constvalue.FrameworkFromType;
import neatlogic.framework.dependency.core.DependencyManager;
import neatlogic.framework.form.dao.mapper.FormMapper;
import neatlogic.framework.form.dto.FormAttributeVo;
import neatlogic.framework.form.dto.FormVersionVo;
import neatlogic.framework.form.exception.FormReferencedCannotBeDeletedException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.framework.dependency.handler.Integration2FormAttrDependencyHandler;
import neatlogic.module.framework.dependency.handler.MatrixAttr2FormAttrDependencyHandler;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
@Transactional
@OperationType(type = OperationTypeEnum.DELETE)
@AuthAction(action = FORM_MODIFY.class)
public class FormDeleteApi extends PrivateApiComponentBase {

    @Resource
    private FormMapper formMapper;

    @Override
    public String getToken() {
        return "form/delete";
    }

    @Override
    public String getName() {
        return "表单删除接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "uuid", type = ApiParamType.STRING, isRequired = true, desc = "表单uuid")
    })
    @Output({
            @Param(name = "uuid", type = ApiParamType.STRING, desc = "表单uuid")
    })
    @Description(desc = "表单删除接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String uuid = jsonObj.getString("uuid");
        if (formMapper.checkFormIsExists(uuid) > 0) {
            int count = DependencyManager.getDependencyCount(FrameworkFromType.FORM, uuid);
            if (count > 0) {
                throw new FormReferencedCannotBeDeletedException(uuid);
            }
            List<FormVersionVo> formVersionList = formMapper.getFormVersionByFormUuid(uuid);
            if (CollectionUtils.isNotEmpty(formVersionList)) {
                for (FormVersionVo formVersionVo : formVersionList) {
                    formMapper.deleteFormAttributeMatrixByFormVersionUuid(formVersionVo.getUuid());
                    List<FormAttributeVo> formAttributeList = formVersionVo.getFormAttributeList();
                    if (CollectionUtils.isNotEmpty(formAttributeList)) {
                        for (FormAttributeVo formAttributeVo : formAttributeList) {
                            DependencyManager.delete(MatrixAttr2FormAttrDependencyHandler.class, formAttributeVo.getUuid());
                            DependencyManager.delete(Integration2FormAttrDependencyHandler.class, formAttributeVo.getUuid());
                        }
                    }
                }
            }
            formMapper.deleteFormByUuid(uuid);
            formMapper.deleteFormVersionByFormUuid(uuid);
            formMapper.deleteFormAttributeByFormUuid(uuid);
            return uuid;
        }
        return null;
    }

}

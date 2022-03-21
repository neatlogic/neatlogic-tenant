package codedriver.module.tenant.api.form;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.auth.label.FORM_MODIFY;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dependency.constvalue.FrameworkFromType;
import codedriver.framework.dependency.core.DependencyManager;
import codedriver.framework.form.dao.mapper.FormMapper;
import codedriver.framework.form.dto.FormVersionVo;
import codedriver.framework.form.exception.FormReferencedCannotBeDeletedException;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
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
            List<FormVersionVo> formVersionList = formMapper.getFormVersionSimpleByFormUuid(uuid);
            if (CollectionUtils.isNotEmpty(formVersionList)) {
                for (FormVersionVo formVersionVo : formVersionList) {
                    formMapper.deleteFormAttributeMatrixByFormVersionUuid(formVersionVo.getUuid());
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

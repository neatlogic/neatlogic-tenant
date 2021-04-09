package codedriver.module.tenant.api.form;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dependency.constvalue.CalleeType;
import codedriver.framework.dependency.core.DependencyManager;
import codedriver.framework.exception.type.ParamIrregularException;
import codedriver.framework.form.dao.mapper.FormMapper;
import codedriver.framework.form.dto.FormVersionVo;
import codedriver.framework.form.dto.FormVo;
import codedriver.framework.form.exception.FormActiveVersionNotFoundExcepiton;
import codedriver.framework.form.exception.FormNotFoundException;
import codedriver.framework.form.exception.FormVersionNotFoundException;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class FormGetApi extends PrivateApiComponentBase {

    @Resource
    private FormMapper formMapper;

    @Override
    public String getToken() {
        return "form/get";
    }

    @Override
    public String getName() {
        return "单个表单查询接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Override
    @Input({
            @Param(name = "uuid", type = ApiParamType.STRING, desc = "表单uuid"),
            @Param(name = "currentVersionUuid", type = ApiParamType.STRING, desc = "选择表单版本uuid"),
    })
    @Output({@Param(explode = FormVo.class)})
    @Description(desc = "单个表单查询接口")
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String currentVersionUuid = jsonObj.getString("currentVersionUuid");
        String uuid = jsonObj.getString("uuid");
        if (StringUtils.isNotBlank(currentVersionUuid)) {
            FormVersionVo formVersion = formMapper.getFormVersionByUuid(currentVersionUuid);
            //判断表单版本是否存在
            if (formVersion == null) {
                throw new FormVersionNotFoundException(currentVersionUuid);
            }
            FormVo formVo = formMapper.getFormByUuid(formVersion.getFormUuid());
            //判断表单是否存在
            if (formVo == null) {
                throw new FormNotFoundException(formVersion.getFormUuid());
            }
            formVo.setCurrentVersionUuid(currentVersionUuid);
            //表单内容
            formVo.setFormConfig(formVersion.getFormConfig());
            //表单版本列表
            List<FormVersionVo> formVersionList = formMapper.getFormVersionSimpleByFormUuid(formVersion.getFormUuid());
            formVo.setVersionList(formVersionList);
            //引用数量
            int count = DependencyManager.getDependencyCount(CalleeType.FORM, formVo.getUuid());
            formVo.setReferenceCount(count);
            return formVo;
        } else if(StringUtils.isNotBlank(uuid)) {//获取激活版本
            FormVo formVo = formMapper.getFormByUuid(uuid);
            //判断表单是否存在
            if (formVo == null) {
                throw new FormNotFoundException(uuid);
            }
            FormVersionVo formVersion = formMapper.getActionFormVersionByFormUuid(uuid);
            if (formVersion == null) {
                throw new FormActiveVersionNotFoundExcepiton(uuid);
            }
            formVo.setCurrentVersionUuid(formVersion.getUuid());
            //表单内容
            formVo.setFormConfig(formVersion.getFormConfig());
            //表单版本列表
            List<FormVersionVo> formVersionList = formMapper.getFormVersionSimpleByFormUuid(uuid);
            formVo.setVersionList(formVersionList);
            //引用数量
            int count = DependencyManager.getDependencyCount(CalleeType.FORM, formVo.getUuid());
    		formVo.setReferenceCount(count);
            return formVo;
        } else {
            throw new ParamIrregularException("参数：'uuid'和'currentVersionUuid'，不能同时为空");
        }
    }

}

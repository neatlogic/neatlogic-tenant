package codedriver.module.tenant.api.form;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.auth.label.FORM_MODIFY;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.crossover.CrossoverServiceFactory;
import codedriver.framework.dependency.core.DependencyManager;
import codedriver.framework.dto.FieldValidResultVo;
import codedriver.framework.form.dao.mapper.FormMapper;
import codedriver.framework.form.dto.FormAttributeVo;
import codedriver.framework.form.dto.FormVersionVo;
import codedriver.framework.form.dto.FormVo;
import codedriver.framework.form.exception.FormAttributeNameIsRepeatException;
import codedriver.framework.form.exception.FormNameRepeatException;
import codedriver.framework.form.exception.FormVersionNotFoundException;
import codedriver.framework.form.service.IFormCrossoverService;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.IValid;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.util.UuidUtil;
import codedriver.module.framework.dependency.handler.Integration2FormAttrDependencyHandler;
import codedriver.module.framework.dependency.handler.MatrixAttr2FormAttrDependencyHandler;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional
@AuthAction(action = FORM_MODIFY.class)
@OperationType(type = OperationTypeEnum.CREATE)
public class FormSaveApi extends PrivateApiComponentBase {

    @Resource
    private FormMapper formMapper;

    @Override
    public String getToken() {
        return "form/save";
    }

    @Override
    public String getName() {
        return "表单保存接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Override
    @Input({
            @Param(name = "uuid", type = ApiParamType.STRING, desc = "表单uuid", isRequired = true),
            @Param(name = "name", type = ApiParamType.REGEX, rule = "^[A-Za-z_\\d\\u4e00-\\u9fa5]+$", isRequired = true, maxLength = 50, desc = "表单名称"),
            @Param(name = "currentVersionUuid", type = ApiParamType.STRING, desc = "当前版本的uuid，为空代表创建一个新版本", isRequired = false),
            @Param(name = "formConfig", type = ApiParamType.JSONOBJECT, desc = "表单控件生成的json内容", isRequired = true)
    })
    @Output({
            @Param(name = "uuid", type = ApiParamType.STRING, desc = "表单uuid"),
            @Param(name = "formVersionUuid", type = ApiParamType.STRING, desc = "表单版本uuid")
    })
    @Description(desc = "表单保存接口")
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject resultObj = new JSONObject();
        FormVo formVo = jsonObj.toJavaObject(FormVo.class);
        String formUuid = formVo.getUuid();
        String currentVersionUuid = formVo.getCurrentVersionUuid();
        resultObj.put("uuid", formUuid);
        boolean updateName = true;
        boolean updateFormConfig = true;
        boolean formIsExists = false;
        Integer oldFormVersionIsActive = 0;
        FormVo oldFormVo = formMapper.getFormByUuid(formUuid);
        if (oldFormVo != null) {
            formIsExists = true;
            if (Objects.equals(oldFormVo.getName(), formVo.getName())) {
                updateName = false;
            }
            if (StringUtils.isNotBlank(currentVersionUuid)) {
                FormVersionVo oldFormVersionVo = formMapper.getFormVersionByUuid(currentVersionUuid);
                if (oldFormVersionVo == null) {
                    throw new FormVersionNotFoundException(currentVersionUuid);
                }
                oldFormVersionIsActive = oldFormVersionVo.getIsActive();
                resultObj.put("currentVersionUuid", oldFormVersionVo.getUuid());
                resultObj.put("currentVersion", oldFormVersionVo.getVersion());
                if (Objects.equals(oldFormVersionVo.getFormConfig(), formVo.getFormConfig())) {
                    updateFormConfig = false;
                } else {
                    formMapper.deleteFormAttributeMatrixByFormVersionUuid(currentVersionUuid);
                    List<FormAttributeVo> formAttributeList = oldFormVersionVo.getFormAttributeList();
                    if (CollectionUtils.isNotEmpty(formAttributeList)) {
                        for (FormAttributeVo formAttributeVo : formAttributeList) {
                            DependencyManager.delete(MatrixAttr2FormAttrDependencyHandler.class, formAttributeVo.getUuid());
                            DependencyManager.delete(Integration2FormAttrDependencyHandler.class, formAttributeVo.getUuid());
                        }
                    }
                }
            }
        }
        if (updateName && formMapper.checkFormNameIsRepeat(formVo) > 0) {
            throw new FormNameRepeatException(formVo.getName());
        }
        if (!updateName && !updateFormConfig) {
            //如果表单名和配置信息都没有更新，就直接返回
        } else if (!updateFormConfig) {
            //只更新表单名信息
            formMapper.updateForm(formVo);
        } else {
            FormVersionVo formVersionVo = new FormVersionVo();
            formVersionVo.setFormConfig(formVo.getFormConfig());
            formVersionVo.setFormUuid(formUuid);
            if (StringUtils.isNotBlank(currentVersionUuid)) {
                formVersionVo.setUuid(currentVersionUuid);
                formVersionVo.setIsActive(oldFormVersionIsActive);
            } else {
                formVersionVo.getUuid();
            }
            List<FormAttributeVo> formAttributeList = formVersionVo.getFormAttributeList();
            if (CollectionUtils.isNotEmpty(formAttributeList)) {
                //判断组件名是否重复
                List<String> attributeNameList = formAttributeList.stream().map(FormAttributeVo::getLabel).collect(Collectors.toList());
                List<String> nameList = new ArrayList<>();
                for (int i = 0; i < attributeNameList.size(); i++) {
                    String name = attributeNameList.get(i);
                    if (i == 0) {
                        nameList.add(name);
                        continue;
                    }
                    if (nameList.contains(name)) {
                        throw new FormAttributeNameIsRepeatException(name);
                    }
                    nameList.add(name);
                }
            }
            //判断表单是否存在
            if (!formIsExists) {
                //插入表单信息
                formMapper.insertForm(formVo);
            } else {
                //更新表单信息
                formMapper.updateForm(formVo);
            }

            //插入表单版本信息
            if (StringUtils.isBlank(currentVersionUuid)) {
                Integer version = formMapper.getMaxVersionByFormUuid(formUuid);
                if (version == null) {//如果表单没有激活版本时，设置当前版本号为1，且为激活版本
                    version = 1;
                    formVersionVo.setIsActive(1);
                } else {
                    version += 1;
                    formVersionVo.setIsActive(0);
                    //另存为新版时，需要对表单组件的uuid重新生成新值，既同一个表单的不同版本中组件uuid不同
                    if (CollectionUtils.isNotEmpty(formAttributeList)) {
                        String formConfig = formVersionVo.getFormConfig();
                        for (FormAttributeVo formAttributeVo : formAttributeList) {
                            String oldUuid = formAttributeVo.getUuid();
                            String newUuid = UuidUtil.randomUuid();
                            formConfig = formConfig.replace(oldUuid, newUuid);
                        }
                        formVersionVo.setFormConfig(formConfig);
                        formVersionVo.setFormAttributeList(null);
                        formAttributeList = formVersionVo.getFormAttributeList();
                    }
                }
                formVersionVo.setVersion(version);
                formMapper.insertFormVersion(formVersionVo);
                resultObj.put("currentVersionUuid", formVersionVo.getUuid());
                resultObj.put("currentVersion", formVersionVo.getVersion());
            } else {
                formMapper.updateFormVersion(formVersionVo);
            }
            //保存激活版本时，更新表单属性信息
            if (Objects.equals(formVersionVo.getIsActive(), 1)) {
                formMapper.deleteFormAttributeByFormUuid(formUuid);
            }
            if (CollectionUtils.isNotEmpty(formAttributeList)) {
                IFormCrossoverService formCrossoverService = CrossoverServiceFactory.getApi(IFormCrossoverService.class);
                for (FormAttributeVo formAttributeVo : formAttributeList) {
                    //保存激活版本时，更新表单属性信息
                    if (Objects.equals(formVersionVo.getIsActive(), 1)) {
                        formMapper.insertFormAttribute(formAttributeVo);
                    }
                    formCrossoverService.saveDependency(formAttributeVo);
                }
            }
        }
        return resultObj;
    }

    public IValid name() {
        return value -> {
            FormVo formVo = value.toJavaObject(FormVo.class);
            if (formMapper.checkFormNameIsRepeat(formVo) > 0) {
                return new FieldValidResultVo(new FormNameRepeatException(formVo.getName()));
            }
            return new FieldValidResultVo();
        };
    }

}

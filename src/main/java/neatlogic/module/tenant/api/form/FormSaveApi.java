package neatlogic.module.tenant.api.form;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.FORM_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.crossover.CrossoverServiceFactory;
import neatlogic.framework.dependency.core.DependencyManager;
import neatlogic.framework.dto.FieldValidResultVo;
import neatlogic.framework.form.dao.mapper.FormMapper;
import neatlogic.framework.form.dto.FormAttributeVo;
import neatlogic.framework.form.dto.FormVersionVo;
import neatlogic.framework.form.dto.FormVo;
import neatlogic.framework.form.exception.FormAttributeNameIsRepeatException;
import neatlogic.framework.form.exception.FormNameRepeatException;
import neatlogic.framework.form.exception.FormVersionNotFoundException;
import neatlogic.framework.form.service.IFormCrossoverService;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.IValid;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.RegexUtils;
import neatlogic.framework.util.UuidUtil;
import neatlogic.module.framework.dependency.handler.Integration2FormAttrDependencyHandler;
import neatlogic.module.framework.dependency.handler.MatrixAttr2FormAttrDependencyHandler;
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
        return "??????????????????";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Override
    @Input({
            @Param(name = "uuid", type = ApiParamType.STRING, desc = "??????uuid", isRequired = true),
            @Param(name = "name", type = ApiParamType.REGEX, rule = RegexUtils.NAME, isRequired = true, maxLength = 50, desc = "????????????"),
            @Param(name = "currentVersionUuid", type = ApiParamType.STRING, desc = "???????????????uuid????????????????????????????????????", isRequired = false),
            @Param(name = "formConfig", type = ApiParamType.JSONOBJECT, desc = "?????????????????????json??????", isRequired = true)
    })
    @Output({
            @Param(name = "uuid", type = ApiParamType.STRING, desc = "??????uuid"),
            @Param(name = "formVersionUuid", type = ApiParamType.STRING, desc = "????????????uuid")
    })
    @Description(desc = "??????????????????")
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
            //???????????????????????????????????????????????????????????????
            return resultObj;
        } else if (!updateFormConfig) {
            //????????????????????????
            formMapper.updateForm(formVo);
            return resultObj;
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
                //???????????????????????????
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
            //????????????????????????
            if (!formIsExists) {
                //??????????????????
                formMapper.insertForm(formVo);
            } else {
                //??????????????????
                formMapper.updateForm(formVo);
            }

            //????????????????????????
            if (StringUtils.isBlank(currentVersionUuid)) {
                Integer version = formMapper.getMaxVersionByFormUuid(formUuid);
                if (version == null) {//????????????????????????????????????????????????????????????1?????????????????????
                    version = 1;
                    formVersionVo.setIsActive(1);
                } else {
                    version += 1;
                    formVersionVo.setIsActive(0);
                    //?????????????????????????????????????????????uuid???????????????????????????????????????????????????????????????uuid??????
                    if (CollectionUtils.isNotEmpty(formAttributeList)) {
                        String formConfigStr = formVersionVo.getFormConfig().toJSONString();
                        for (FormAttributeVo formAttributeVo : formAttributeList) {
                            String oldUuid = formAttributeVo.getUuid();
                            String newUuid = UuidUtil.randomUuid();
                            formConfigStr = formConfigStr.replace(oldUuid, newUuid);
                        }
                        formVersionVo.setFormConfig(JSONObject.parseObject(formConfigStr));
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
            //????????????????????????????????????????????????
            if (Objects.equals(formVersionVo.getIsActive(), 1)) {
                formMapper.deleteFormAttributeByFormUuid(formUuid);
            }
            if (CollectionUtils.isNotEmpty(formAttributeList)) {
                IFormCrossoverService formCrossoverService = CrossoverServiceFactory.getApi(IFormCrossoverService.class);
                for (FormAttributeVo formAttributeVo : formAttributeList) {
                    //????????????????????????????????????????????????
                    if (Objects.equals(formVersionVo.getIsActive(), 1)) {
                        formMapper.insertFormAttribute(formAttributeVo);
                    }
                    formCrossoverService.saveDependency(formAttributeVo);
                }
            }
            return resultObj;
        }
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

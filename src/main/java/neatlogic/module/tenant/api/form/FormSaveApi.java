package neatlogic.module.tenant.api.form;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.FORM_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.crossover.CrossoverServiceFactory;
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
        return "nmtaf.formsaveapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Override
    @Input({
            @Param(name = "uuid", type = ApiParamType.STRING, desc = "common.uuid", isRequired = true),
            @Param(name = "name", type = ApiParamType.REGEX, rule = RegexUtils.NAME, isRequired = true, maxLength = 50, desc = "common.name"),
            @Param(name = "currentVersionUuid", type = ApiParamType.STRING, desc = "common.versionuuid", help = "当前版本的uuid，为空代表创建一个新版本"),
            @Param(name = "formConfig", type = ApiParamType.JSONOBJECT, desc = "common.config", isRequired = true)
    })
    @Output({
            @Param(name = "uuid", type = ApiParamType.STRING, desc = "common.uuid"),
            @Param(name = "formVersionUuid", type = ApiParamType.STRING, desc = "common.versionuuid")
    })
    @Description(desc = "nmtaf.formsaveapi.getname")
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
        IFormCrossoverService formCrossoverService = CrossoverServiceFactory.getApi(IFormCrossoverService.class);
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
                    formCrossoverService.deleteDependency(oldFormVersionVo);
                }
            }
        }
        if (updateName && formMapper.checkFormNameIsRepeat(formVo) > 0) {
            throw new FormNameRepeatException(formVo.getName());
        }
        if (!updateName && !updateFormConfig) {
            //如果表单名和配置信息都没有更新，就直接返回
            return resultObj;
        } else if (!updateFormConfig) {
            //只更新表单名信息
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
            formCrossoverService.saveDependency(formVersionVo);
            //保存激活版本时，更新表单属性信息
            if (Objects.equals(formVersionVo.getIsActive(), 1)) {
                formMapper.deleteFormAttributeByFormUuid(formUuid);
                if (CollectionUtils.isNotEmpty(formAttributeList)) {
                    for (FormAttributeVo formAttributeVo : formAttributeList) {
                        //保存激活版本时，更新表单属性信息
                        formMapper.insertFormAttribute(formAttributeVo);
                    }
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

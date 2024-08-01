package neatlogic.module.tenant.api.form;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.FORM_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dto.FieldValidResultVo;
import neatlogic.framework.exception.type.ParamNotExistsException;
import neatlogic.framework.form.constvalue.FormHandler;
import neatlogic.framework.form.dao.mapper.FormMapper;
import neatlogic.framework.form.dto.FormAttributeParentVo;
import neatlogic.framework.form.dto.FormAttributeVo;
import neatlogic.framework.form.dto.FormVersionVo;
import neatlogic.framework.form.dto.FormVo;
import neatlogic.framework.form.exception.FormNameRepeatException;
import neatlogic.framework.form.exception.FormNotFoundException;
import neatlogic.framework.form.exception.FormVersionNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.IValid;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.FormUtil;
import neatlogic.framework.util.RegexUtils;
import neatlogic.framework.util.UuidUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

@Service
@Transactional
@OperationType(type = OperationTypeEnum.CREATE)
@AuthAction(action = FORM_MODIFY.class)
public class FormCopyApi extends PrivateApiComponentBase {

    @Resource
    private FormMapper formMapper;

    @Override
    public String getToken() {
        return "form/copy";
    }

    @Override
    public String getName() {
        return "nmtaf.formcopyapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "uuid", type = ApiParamType.STRING, isRequired = true, desc = "common.uuid"),
            @Param(name = "name", type = ApiParamType.REGEX, rule = RegexUtils.NAME, isRequired = true, maxLength = 50, desc = "common.name"),
            @Param(name = "currentVersionUuid", type = ApiParamType.STRING, desc = "common.versionuuid", help = "要复制的版本uuid,空表示复制所有版本")
    })
    @Output({
            @Param(name = "Return", explode = FormVo.class, desc = "common.tbodylist")
    })
    @Description(desc = "nmtaf.formcopyapi.getname")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        FormVo newFrom = new FormVo();
        String name = jsonObj.getString("name");
        newFrom.setName(name);
        //如果表单名称已存在
        if (formMapper.checkFormNameIsRepeat(newFrom) > 0) {
            throw new FormNameRepeatException(name);
        }
        // 收集要复制的版本
        List<FormVersionVo> newFormVersionList = new ArrayList<>();
        String uuid = jsonObj.getString("uuid");
        String currentVersionUuid = jsonObj.getString("currentVersionUuid");
        if (StringUtils.isNotBlank(currentVersionUuid)) {
            FormVersionVo formVersionVo = formMapper.getFormVersionByUuid(currentVersionUuid);
            if (formVersionVo == null) {
                throw new FormVersionNotFoundException(currentVersionUuid);
            }
            FormVo formVo = formMapper.getFormByUuid(formVersionVo.getFormUuid());
            if (formVo == null) {
                throw new FormNotFoundException(formVersionVo.getFormUuid());
            }
            newFrom.setIsActive(formVo.getIsActive());
            Map<String, String> formAttributeOldUuid2NewUuidMap = new HashMap<>();
            FormVersionVo newFormVersion = copyFormVersion(formVersionVo, newFrom.getUuid(), formAttributeOldUuid2NewUuidMap);
            newFormVersionList.add(newFormVersion);
        } else if(StringUtils.isNotBlank(uuid)) {
            FormVo formVo = formMapper.getFormByUuid(uuid);
            if (formVo == null) {
                throw new FormNotFoundException(uuid);
            }
            newFrom.setIsActive(formVo.getIsActive());
            List<FormVersionVo> formVersionList = formMapper.getFormVersionByFormUuid(uuid);
            for (FormVersionVo formVersionVo : formVersionList) {
                Map<String, String> formAttributeOldUuid2NewUuidMap = new HashMap<>();
                FormVersionVo newFormVersion = copyFormVersion(formVersionVo, newFrom.getUuid(), formAttributeOldUuid2NewUuidMap);
                newFormVersionList.add(newFormVersion);
            }
        } else {
            throw new ParamNotExistsException("uuid", "currentVersionUuid");
        }
        // 插入表单
        formMapper.insertForm(newFrom);
        // 重新生成版本号
        if (newFormVersionList.size() == 1) {
            newFormVersionList.get(0).setVersion(1);
        } else {
            newFormVersionList.sort(Comparator.comparing(FormVersionVo::getVersion));
            for (int i = 0; i < newFormVersionList.size(); i++) {
                newFormVersionList.get(i).setVersion(i + 1);
            }
        }
        // 遍历要复制的版本列表
        for (FormVersionVo formVersionVo : newFormVersionList) {
            // 插入表单版本
            formMapper.insertFormVersion(formVersionVo);
            // 保存依赖
            FormUtil.saveDependency(formVersionVo);
            if (formVersionVo.getIsActive().equals(1)) {
                newFrom.setCurrentVersion(formVersionVo.getVersion());
                newFrom.setCurrentVersionUuid(formVersionVo.getUuid());
                String mainSceneUuid = formVersionVo.getFormConfig().getString("uuid");
                formVersionVo.setSceneUuid(mainSceneUuid);
                // 对应激活版本需要插入表单属性
                for (FormAttributeVo formAttributeVo : formVersionVo.getFormAttributeList()) {
                    formMapper.insertFormAttribute(formAttributeVo);
                }
            }
            List<FormAttributeVo> formVersionExtendAttributeList = formVersionVo.getFormExtendAttributeList();
            if (CollectionUtils.isNotEmpty(formVersionExtendAttributeList)) {
                for (FormAttributeVo formAttributeVo : formVersionExtendAttributeList) {
                    formMapper.insertFormExtendAttribute(formAttributeVo);
                }
            }
            List<FormAttributeVo> formVersionCustomExtendAttributeList = formVersionVo.getFormCustomExtendAttributeList();
            if (CollectionUtils.isNotEmpty(formVersionCustomExtendAttributeList)) {
                for (FormAttributeVo formAttributeVo : formVersionCustomExtendAttributeList) {
                    formMapper.insertFormExtendAttribute(formAttributeVo);
                }
            }
        }
        return newFrom;
    }

    public IValid name() {
        return value -> {
            FormVo formVo = JSON.toJavaObject(value, FormVo.class);
            if (formMapper.checkFormNameIsRepeat(formVo) > 0) {
                return new FieldValidResultVo(new FormNameRepeatException(formVo.getName()));
            }
            return new FieldValidResultVo();
        };
    }

    /**
     * 复制一份表单版本配置信息，需要将所有属性的uuid值重新生成，避免不同版本的属性uuid冲突
     * @param oldFormVersionVo
     * @param newFormUuid
     * @return
     */
    private FormVersionVo copyFormVersion(FormVersionVo oldFormVersionVo, String newFormUuid, Map<String, String> formAttributeOldUuid2NewUuidMap) {
        JSONObject formConfig = oldFormVersionVo.getFormConfig();
        // 更新各种唯一标识uuid，防止不同表单版本之间唯一标识uuid相同
        // 更新场景uuid
        String defaultSceneUuid = formConfig.getString("defaultSceneUuid");
        JSONArray sceneList = formConfig.getJSONArray("sceneList");
        if (CollectionUtils.isNotEmpty(sceneList)) {
            for (int i = 0; i < sceneList.size(); i++) {
                JSONObject scene = sceneList.getJSONObject(i);
                String uuid = scene.getString("uuid");
                String newUuid = UuidUtil.randomUuid();
                scene.put("uuid", newUuid);
                if (Objects.equals(uuid, defaultSceneUuid)) {
                    formConfig.put("defaultSceneUuid", newUuid);
                }
            }
        }
        String uuid = formConfig.getString("uuid");
        String newUuid = UuidUtil.randomUuid();
        formConfig.put("uuid", newUuid);
        if (Objects.equals(uuid, defaultSceneUuid)) {
            formConfig.put("defaultSceneUuid", newUuid);
        }

        String content = formConfig.toJSONString();
        List<FormAttributeVo> allFormAttributeList = FormUtil.getAllFormAttributeList(formConfig);
        for (FormAttributeVo formAttributeVo : allFormAttributeList) {
            FormAttributeParentVo parent = formAttributeVo.getParent();
            // 对于表格选择组件中的属性uuid不做替换，因为它们的来源是矩阵属性uuid，不会变
            if (parent != null && Objects.equals(parent.getHandler(), FormHandler.FORMTABLESELECTOR.getHandler())) {
                continue;
            }
            // 更新表单属性uuid
            String formAttributeNewUuid = UuidUtil.randomUuid();
            formAttributeOldUuid2NewUuidMap.put(formAttributeVo.getUuid(), formAttributeNewUuid);
            content = content.replace(formAttributeVo.getUuid(), formAttributeNewUuid);
        }
        FormVersionVo formVersionVo = new FormVersionVo();
        formVersionVo.setVersion(oldFormVersionVo.getVersion());
        formVersionVo.setIsActive(oldFormVersionVo.getIsActive());
        formVersionVo.setFormUuid(newFormUuid);
        formVersionVo.setFormConfig(JSONObject.parseObject(content));
        return formVersionVo;
    }
}

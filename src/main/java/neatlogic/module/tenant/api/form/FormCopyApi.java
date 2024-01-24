package neatlogic.module.tenant.api.form;

import com.alibaba.fastjson.JSONArray;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.FORM_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.crossover.CrossoverServiceFactory;
import neatlogic.framework.dto.FieldValidResultVo;
import neatlogic.framework.exception.type.ParamNotExistsException;
import neatlogic.framework.form.dao.mapper.FormMapper;
import neatlogic.framework.form.dto.FormAttributeVo;
import neatlogic.framework.form.dto.FormVersionVo;
import neatlogic.framework.form.dto.FormVo;
import neatlogic.framework.form.exception.FormNameRepeatException;
import neatlogic.framework.form.exception.FormNotFoundException;
import neatlogic.framework.form.exception.FormVersionNotFoundException;
import neatlogic.framework.form.service.IFormCrossoverService;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.IValid;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.RegexUtils;
import neatlogic.framework.util.UuidUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Objects;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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
            newFormVersionList.add(copyFormVersion(formVersionVo, newFrom.getUuid()));
        } else if(StringUtils.isNotBlank(uuid)) {
            FormVo formVo = formMapper.getFormByUuid(uuid);
            if (formVo == null) {
                throw new FormNotFoundException(uuid);
            }
            newFrom.setIsActive(formVo.getIsActive());
            List<FormVersionVo> formVersionList = formMapper.getFormVersionByFormUuid(uuid);
            for (FormVersionVo formVersionVo : formVersionList) {
                newFormVersionList.add(copyFormVersion(formVersionVo, newFrom.getUuid()));
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
        IFormCrossoverService formCrossoverService = CrossoverServiceFactory.getApi(IFormCrossoverService.class);
        // 遍历要复制的版本列表
        for (FormVersionVo formVersionVo : newFormVersionList) {
            // 插入表单版本
            formMapper.insertFormVersion(formVersionVo);
            // 保存依赖
            formCrossoverService.saveDependency(formVersionVo);
            if (formVersionVo.getIsActive().equals(1)) {
                newFrom.setCurrentVersion(formVersionVo.getVersion());
                newFrom.setCurrentVersionUuid(formVersionVo.getUuid());
                // 对应激活版本需要插入表单属性
                for (FormAttributeVo formAttributeVo : formVersionVo.getFormAttributeList()) {
                    formMapper.insertFormAttribute(formAttributeVo);
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
    private FormVersionVo copyFormVersion(FormVersionVo oldFormVersionVo, String newFormUuid) {
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
                if (Objects.equal(uuid, defaultSceneUuid)) {
                    formConfig.put("defaultSceneUuid", newUuid);
                }
            }
        }
        String uuid = formConfig.getString("uuid");
        String newUuid = UuidUtil.randomUuid();
        formConfig.put("uuid", newUuid);
        if (Objects.equal(uuid, defaultSceneUuid)) {
            formConfig.put("defaultSceneUuid", newUuid);
        }

        String content = formConfig.toJSONString();
        IFormCrossoverService formCrossoverService = CrossoverServiceFactory.getApi(IFormCrossoverService.class);
        List<FormAttributeVo> allFormAttributeList = formCrossoverService.getAllFormAttributeList(formConfig);
        for (FormAttributeVo formAttributeVo : allFormAttributeList) {
            // 更新表单属性uuid
            content = content.replace(formAttributeVo.getUuid(), UuidUtil.randomUuid());
        }
        FormVersionVo formVersionVo = new FormVersionVo();
        formVersionVo.setVersion(oldFormVersionVo.getVersion());
        formVersionVo.setIsActive(oldFormVersionVo.getIsActive());
        formVersionVo.setFormUuid(newFormUuid);
        formVersionVo.setFormConfig(JSONObject.parseObject(content));
        return formVersionVo;
    }
}

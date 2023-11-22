package neatlogic.module.tenant.api.form;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.FORM_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.form.dao.mapper.FormMapper;
import neatlogic.framework.form.dto.FormVersionVo;
import neatlogic.framework.form.dto.FormVo;
import neatlogic.framework.form.exception.FormNotFoundException;
import neatlogic.framework.form.exception.FormVersionNotFoundException;
import neatlogic.framework.form.exception.FormVersionSceneNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

@Service
@AuthAction(action = FORM_MODIFY.class)
@OperationType(type = OperationTypeEnum.UPDATE)
public class SaveFormVersionDefaultSceneApi extends PrivateApiComponentBase {

    @Resource
    private FormMapper formMapper;

    @Override
    public String getName() {
        return "nmtaf.saveformversiondefaultsceneapi.getname";
    }

    @Input({
            @Param(name = "versionUuid", type = ApiParamType.STRING, desc = "common.versionuuid", isRequired = true),
            @Param(name = "sceneUuid", type = ApiParamType.STRING, desc = "common.sceneuuid", isRequired = true)
    })
    @Output({})
    @Description(desc = "nmtaf.saveformversiondefaultsceneapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        String versionUuid = paramObj.getString("versionUuid");
        FormVersionVo formVersionVo = formMapper.getFormVersionByUuid(versionUuid);
        if (formVersionVo == null) {
            throw new FormVersionNotFoundException(versionUuid);
        }
        JSONObject config = formVersionVo.getFormConfig();
        if (MapUtils.isEmpty(config)) {
            return null;
        }
        String sceneUuid = paramObj.getString("sceneUuid");
        String defaultSceneUuid = config.getString("defaultSceneUuid");
        if (Objects.equals(defaultSceneUuid, sceneUuid)) {
            return null;
        }
        JSONArray sceneList = config.getJSONArray("sceneList");
        if (CollectionUtils.isEmpty(sceneList)) {
            return null;
        }
        boolean flag = false;
        for (int i = 0; i < sceneList.size(); i++) {
            JSONObject sceneObj = sceneList.getJSONObject(i);
            String uuid = sceneObj.getString("uuid");
            if (Objects.equals(uuid, sceneUuid)) {
                flag = true;
                break;
            }
        }
        if (!flag) {
            FormVo formVo = formMapper.getFormByUuid(formVersionVo.getFormUuid());
            if (formVo == null) {
                throw new FormNotFoundException(formVersionVo.getFormUuid());
            }
            throw new FormVersionSceneNotFoundException(formVo.getName(), String.valueOf(formVersionVo.getVersion()), sceneUuid);
        }
        config.put("defaultSceneUuid", sceneUuid);
        formVersionVo.setFormConfig(config);
        formMapper.updateFormVersionConfigByUuid(formVersionVo);
        return null;
    }

    @Override
    public String getToken() {
        return "form/version/defaultscene/save";
    }
}

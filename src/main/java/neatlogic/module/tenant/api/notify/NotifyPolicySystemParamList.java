package neatlogic.module.tenant.api.notify;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.crossover.CrossoverServiceFactory;
import neatlogic.framework.dto.ConditionParamVo;
import neatlogic.framework.form.attribute.core.FormAttributeHandlerFactory;
import neatlogic.framework.form.attribute.core.IFormAttributeHandler;
import neatlogic.framework.form.dao.mapper.FormMapper;
import neatlogic.framework.form.dto.FormAttributeVo;
import neatlogic.framework.form.dto.FormVo;
import neatlogic.framework.form.exception.FormNotFoundException;
import neatlogic.framework.form.service.IFormCrossoverService;
import neatlogic.framework.notify.core.INotifyPolicyHandler;
import neatlogic.framework.notify.core.NotifyPolicyHandlerFactory;
import neatlogic.framework.notify.exception.NotifyPolicyHandlerNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class NotifyPolicySystemParamList extends PrivateApiComponentBase {

    @Resource
    private FormMapper formMapper;

    @Override
    public String getToken() {
        return "notify/policy/systemparam/list";
    }

    @Override
    public String getName() {
        return "nmtan.notifypolicysystemparamlist.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "notifyPolicyHandler", type = ApiParamType.STRING, isRequired = true, desc = "common.handler"),
            @Param(name = "formUuid", type = ApiParamType.STRING, desc = "term.framework.formuuid"),
            @Param(name = "tag", type = ApiParamType.STRING, desc = "common.tag"),
            @Param(name = "isAll", type = ApiParamType.INTEGER, rule = "0,1", desc = "term.process.isreturnallattr")
    })
    @Output({
            @Param(name = "tbodyList", explode = ConditionParamVo[].class, desc = "common.tbodylist")
    })
    @Description(desc = "nmtan.notifypolicysystemparamlist.getname")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String notifyPolicyHandler = jsonObj.getString("notifyPolicyHandler");
        INotifyPolicyHandler handler = NotifyPolicyHandlerFactory.getHandler(notifyPolicyHandler);
        if (handler == null) {
            throw new NotifyPolicyHandlerNotFoundException(notifyPolicyHandler);
        }
        List<ConditionParamVo> systemParamList = handler.getSystemParamList();
        systemParamList.sort((e1, e2) -> e1.getName().compareToIgnoreCase(e2.getName()));
        List<ConditionParamVo> paramList = new ArrayList<>(systemParamList);
        // 表单条件
        String formUuid = jsonObj.getString("formUuid");
        if (StringUtils.isNotBlank(formUuid)) {
            FormVo form = formMapper.getFormByUuid(formUuid);
            if (form == null) {
                throw new FormNotFoundException(formUuid);
            }
            Integer isAll = jsonObj.getInteger("isAll");
            String tag = jsonObj.getString("tag");
            IFormCrossoverService formCrossoverService = CrossoverServiceFactory.getApi(IFormCrossoverService.class);
            List<FormAttributeVo> formAttrList = formCrossoverService.getFormAttributeListNew(formUuid, form.getName(), tag);
            for (FormAttributeVo formAttributeVo : formAttrList) {
                IFormAttributeHandler formHandler = FormAttributeHandlerFactory.getHandler(formAttributeVo.getHandler());
                if(formHandler == null){
                    continue;
                }
                if ((isAll != null && isAll.equals(1)) || formHandler.isConditionable()) {
                    ConditionParamVo conditionParamVo = new ConditionParamVo();
                    conditionParamVo.setName(formAttributeVo.getUuid());
                    conditionParamVo.setLabel(formAttributeVo.getLabel());
                    if (formHandler.getParamType() != null) {
                        conditionParamVo.setParamType(formHandler.getParamType().getName());
                        conditionParamVo.setParamTypeName(formHandler.getParamType().getText());
                    }
                    conditionParamVo.setIsEditable(0);
                    conditionParamVo.setType("form");
                    paramList.add(conditionParamVo);
                }
            }
        }
        JSONObject resultObj = new JSONObject();
        resultObj.put("tbodyList", paramList);
        return resultObj;
    }

}

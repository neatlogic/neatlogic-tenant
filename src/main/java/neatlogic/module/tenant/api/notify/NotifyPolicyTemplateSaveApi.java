/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package neatlogic.module.tenant.api.notify;

import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.NOTIFY_POLICY_MODIFY;
import neatlogic.framework.common.constvalue.ActionType;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dto.FieldValidResultVo;
import neatlogic.framework.notify.core.INotifyPolicyHandler;
import neatlogic.framework.notify.core.NotifyPolicyHandlerFactory;
import neatlogic.framework.notify.dao.mapper.NotifyMapper;
import neatlogic.framework.notify.dto.NotifyPolicyConfigVo;
import neatlogic.framework.notify.dto.NotifyPolicyVo;
import neatlogic.framework.notify.dto.NotifyTemplateVo;
import neatlogic.framework.notify.exception.NotifyPolicyHandlerNotFoundException;
import neatlogic.framework.notify.exception.NotifyPolicyNotFoundException;
import neatlogic.framework.notify.exception.NotifyTemplateNameRepeatException;
import neatlogic.framework.notify.exception.NotifyTemplateNotFoundException;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.IValid;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.RegexUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Transactional
@AuthAction(action = NOTIFY_POLICY_MODIFY.class)
@OperationType(type = OperationTypeEnum.UPDATE)
public class NotifyPolicyTemplateSaveApi extends PrivateApiComponentBase {

    @Autowired
    private NotifyMapper notifyMapper;

    @Override
    public String getToken() {
        return "notify/policy/template/save";
    }

    @Override
    public String getName() {
        return "通知模板保存接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "policyId", type = ApiParamType.LONG, isRequired = true, desc = "策略id"),
        @Param(name = "id", type = ApiParamType.LONG, desc = "模板id"),
        @Param(name = "name", type = ApiParamType.REGEX, rule = RegexUtils.NAME, maxLength = 50,
                isRequired = true, desc = "模板名称"),
        @Param(name = "title", type = ApiParamType.STRING, isRequired = true, desc = "模板标题"),
        @Param(name = "content", type = ApiParamType.STRING, isRequired = true, desc = "模板内容"),
        @Param(name = "notifyHandler", type = ApiParamType.STRING, desc = "通知处理器")})
    @Output({@Param(explode = NotifyTemplateVo.class, desc = "通知模板信息")})
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long policyId = jsonObj.getLong("policyId");
        NotifyPolicyVo notifyPolicyVo = notifyMapper.getNotifyPolicyById(policyId);
        if (notifyPolicyVo == null) {
            throw new NotifyPolicyNotFoundException(policyId.toString());
        }
        INotifyPolicyHandler notifyPolicyHandler = NotifyPolicyHandlerFactory.getHandler(notifyPolicyVo.getHandler());
        if (notifyPolicyHandler == null) {
            throw new NotifyPolicyHandlerNotFoundException(notifyPolicyVo.getHandler());
        }

        Long id = jsonObj.getLong("id");
        String name = jsonObj.getString("name");
        String title = jsonObj.getString("title");
        String content = jsonObj.getString("content");
        String notifyHandler = jsonObj.getString("notifyHandler");
        NotifyTemplateVo resultTemplateVo = null;
        NotifyPolicyConfigVo config = notifyPolicyVo.getConfig();
        List<NotifyTemplateVo> templateList = config.getTemplateList();
        for (NotifyTemplateVo notifyTemplateVo : templateList) {
            if (name.equals(notifyTemplateVo.getName()) && !notifyTemplateVo.getId().equals(id)) {
                throw new NotifyTemplateNameRepeatException(name);
            }
        }
        if (id != null) {
            boolean isExists = false;
            for (NotifyTemplateVo notifyTemplateVo : templateList) {
                if (id.equals(notifyTemplateVo.getId())) {
                    notifyTemplateVo.setName(name);
                    notifyTemplateVo.setTitle(title);
                    notifyTemplateVo.setContent(content);
                    notifyTemplateVo.setNotifyHandler(notifyHandler);
                    notifyTemplateVo.setAction(ActionType.UPDATE.getValue());
                    notifyTemplateVo.setActionTime(new Date());
                    notifyTemplateVo.setActionUser(UserContext.get().getUserName());
                    isExists = true;
                    resultTemplateVo = notifyTemplateVo;
                }
            }
            if (!isExists) {
                throw new NotifyTemplateNotFoundException(id.toString());
            }
        } else {
            NotifyTemplateVo notifyTemplateVo = new NotifyTemplateVo();
            notifyTemplateVo.setName(name);
            notifyTemplateVo.setTitle(title);
            notifyTemplateVo.setContent(content);
            notifyTemplateVo.setNotifyHandler(notifyHandler);
            notifyTemplateVo.setAction(ActionType.CREATE.getValue());
            notifyTemplateVo.setActionTime(new Date());
            notifyTemplateVo.setActionUser(UserContext.get().getUserName());
            templateList.add(notifyTemplateVo);
            resultTemplateVo = notifyTemplateVo;
        }
        notifyMapper.updateNotifyPolicyById(notifyPolicyVo);
        return resultTemplateVo;
    }

    public IValid name(){
        return value -> {
            NotifyTemplateVo templateVo = JSON.toJavaObject(value,NotifyTemplateVo.class);
            Long policyId = value.getLong("policyId");
            NotifyPolicyVo notifyPolicyVo = notifyMapper.getNotifyPolicyById(policyId);
            if (notifyPolicyVo == null) {
                return new FieldValidResultVo(new NotifyPolicyNotFoundException(policyId.toString()));
            }
            NotifyPolicyConfigVo config = notifyPolicyVo.getConfig();
            List<NotifyTemplateVo> templateList = config.getTemplateList();
            for (NotifyTemplateVo notifyTemplateVo : templateList) {
                if (templateVo.getName().equals(notifyTemplateVo.getName()) && !notifyTemplateVo.getId().equals(templateVo.getId())) {
                    return new FieldValidResultVo(new NotifyTemplateNameRepeatException(templateVo.getName()));
                }
            }
            return new FieldValidResultVo();
        };
    }

}

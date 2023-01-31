/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package neatlogic.module.tenant.api.notify;

import java.util.List;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.dto.FieldValidResultVo;
import neatlogic.framework.notify.core.INotifyPolicyHandler;
import neatlogic.framework.notify.core.NotifyPolicyHandlerFactory;
import neatlogic.framework.notify.exception.NotifyPolicyHandlerNotFoundException;
import neatlogic.framework.notify.exception.NotifyPolicyParamNameRepeatException;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.core.IValid;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;

import neatlogic.framework.auth.label.NOTIFY_POLICY_MODIFY;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.constvalue.Expression;
import neatlogic.framework.common.constvalue.FormHandlerType;
import neatlogic.framework.common.constvalue.ParamType;
import neatlogic.framework.dto.ConditionParamVo;
import neatlogic.framework.dto.ExpressionVo;
import neatlogic.framework.exception.type.ParamIrregularException;
import neatlogic.framework.notify.dao.mapper.NotifyMapper;
import neatlogic.framework.notify.dto.NotifyPolicyConfigVo;
import neatlogic.framework.notify.dto.NotifyPolicyVo;
import neatlogic.framework.notify.exception.NotifyPolicyNotFoundException;

@Service
@Transactional
@AuthAction(action = NOTIFY_POLICY_MODIFY.class)
@OperationType(type = OperationTypeEnum.UPDATE)
public class NotifyPolicyParamSaveApi extends PrivateApiComponentBase {

    @Autowired
    private NotifyMapper notifyMapper;

    @Override
    public String getToken() {
        return "notify/policy/param/save";
    }

    @Override
    public String getName() {
        return "通知策略参数保存接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "policyId", type = ApiParamType.LONG, isRequired = true, desc = "策略id"),
            @Param(name = "uuid", type = ApiParamType.STRING, isRequired = true, desc = "uuid"),
            @Param(name = "name", type = ApiParamType.STRING, isRequired = true, desc = "参数名"),
            @Param(name = "paramType", type = ApiParamType.STRING, isRequired = true, desc = "参数类型"),
            @Param(name = "label", type = ApiParamType.STRING, isRequired = true, desc = "参数描述")
    })
    @Output({@Param(explode = ConditionParamVo.class, desc = "参数信息")})
    @Description(desc = "通知策略参数保存接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long policyId = jsonObj.getLong("policyId");
        NotifyPolicyVo notifyPolicyVo = notifyMapper.getNotifyPolicyById(policyId);
        if (notifyPolicyVo == null) {
            throw new NotifyPolicyNotFoundException(policyId.toString());
        }
        String paramType = jsonObj.getString("paramType");
        ParamType basicTypeEnum = ParamType.getParamType(paramType);
        if (basicTypeEnum == null) {
            throw new ParamIrregularException("paramType");
        }
        String controller = FormHandlerType.INPUT.toString();
        JSONObject paramConfig = new JSONObject();
        if (ParamType.STRING == basicTypeEnum || ParamType.NUMBER == basicTypeEnum
            || ParamType.ARRAY == basicTypeEnum) {
            paramConfig.put("type", "text");
            paramConfig.put("value", "");
            paramConfig.put("defaultValue", "");
            paramConfig.put("maxlength", 50);
        } else if (ParamType.DATE == basicTypeEnum) {
            controller = FormHandlerType.DATE.toString();
            paramConfig.put("type", "datetimerange");
            paramConfig.put("value", "");
            paramConfig.put("defaultValue", "");
            paramConfig.put("format", "yyyy-MM-dd HH:mm:ss");
            paramConfig.put("valueType", "timestamp");
        }
        String uuid = jsonObj.getString("uuid");
        String name = jsonObj.getString("name");
        String label = jsonObj.getString("label");
        INotifyPolicyHandler notifyPolicyHandler = NotifyPolicyHandlerFactory.getHandler(notifyPolicyVo.getHandler());
        if (notifyPolicyHandler == null) {
            throw new NotifyPolicyHandlerNotFoundException(notifyPolicyVo.getHandler());
        }
        List<ConditionParamVo> systemParamList = notifyPolicyHandler.getSystemParamList();
        if(systemParamList.stream().anyMatch(o -> o.getName().equals(name))){
            throw new NotifyPolicyParamNameRepeatException(name);
        }
        ConditionParamVo resultParamVo = null;
        boolean isNew = true;
        NotifyPolicyConfigVo config = notifyPolicyVo.getConfig();
        List<ConditionParamVo> paramList = config.getParamList();

        if(paramList.stream().anyMatch(o -> name.equals(o.getName()) && !uuid.equals(o.getUuid()))){
            throw new NotifyPolicyParamNameRepeatException(name);
        }

        for (ConditionParamVo notifyPolicyParamVo : paramList) {
            if (uuid.equals(notifyPolicyParamVo.getUuid())) {
                notifyPolicyParamVo.setName(name);
                notifyPolicyParamVo.setParamType(paramType);
                notifyPolicyParamVo.setLabel(label);
                notifyPolicyParamVo.setController(controller);
                notifyPolicyParamVo.setConfig(paramConfig);
                notifyPolicyParamVo.setParamTypeName(basicTypeEnum.getText());
                notifyPolicyParamVo.setDefaultExpression(basicTypeEnum.getDefaultExpression().getExpression());
                notifyPolicyParamVo.getExpressionList().clear();
                for (Expression expression : basicTypeEnum.getExpressionList()) {
                    notifyPolicyParamVo.getExpressionList()
                            .add(new ExpressionVo(expression.getExpression(), expression.getExpressionName(),expression.getIsShowConditionValue()));
                }
                notifyPolicyParamVo.setLcd(System.currentTimeMillis());
                isNew = false;
                resultParamVo = notifyPolicyParamVo;
                break;
            }
        }
        if (isNew) {
            ConditionParamVo notifyPolicyParamVo = new ConditionParamVo();
            notifyPolicyParamVo.setUuid(uuid);
            notifyPolicyParamVo.setName(name);
            notifyPolicyParamVo.setLabel(label);
            notifyPolicyParamVo.setController(controller);
            notifyPolicyParamVo.setConfig(paramConfig);
            notifyPolicyParamVo.setType("custom");
            notifyPolicyParamVo.setParamType(paramType);
            notifyPolicyParamVo.setParamTypeName(basicTypeEnum.getText());
            notifyPolicyParamVo.setDefaultExpression(basicTypeEnum.getDefaultExpression().getExpression());
            for (Expression expression : basicTypeEnum.getExpressionList()) {
                notifyPolicyParamVo.getExpressionList()
                    .add(new ExpressionVo(expression.getExpression(), expression.getExpressionName(),expression.getIsShowConditionValue()));
            }
            notifyPolicyParamVo.setLcd(System.currentTimeMillis());
            paramList.add(notifyPolicyParamVo);
            resultParamVo = notifyPolicyParamVo;
        }

        notifyMapper.updateNotifyPolicyById(notifyPolicyVo);
        return resultParamVo;
    }

    public IValid name(){
        return value -> {
            ConditionParamVo paramVo = JSON.toJavaObject(value,ConditionParamVo.class);
            Long policyId = value.getLong("policyId");
            NotifyPolicyVo notifyPolicyVo = notifyMapper.getNotifyPolicyById(policyId);
            if (notifyPolicyVo == null) {
                return new FieldValidResultVo(new NotifyPolicyNotFoundException(policyId.toString()));
            }
            INotifyPolicyHandler notifyPolicyHandler = NotifyPolicyHandlerFactory.getHandler(notifyPolicyVo.getHandler());
            if (notifyPolicyHandler == null) {
                return new FieldValidResultVo(new NotifyPolicyHandlerNotFoundException(notifyPolicyVo.getHandler()));
            }
            List<ConditionParamVo> systemParamList = notifyPolicyHandler.getSystemParamList();
            if(systemParamList.stream().anyMatch(o -> o.getName().equals(paramVo.getName()))){
                return new FieldValidResultVo(new NotifyPolicyParamNameRepeatException(paramVo.getName()));
            }
            NotifyPolicyConfigVo config = notifyPolicyVo.getConfig();
            List<ConditionParamVo> paramList = config.getParamList();
            if(paramList.stream().anyMatch(o -> paramVo.getName().equals(o.getName()) && !paramVo.getUuid().equals(o.getUuid()))){
                return new FieldValidResultVo(new NotifyPolicyParamNameRepeatException(paramVo.getName()));
            }
            return new FieldValidResultVo();
        };
    }

}

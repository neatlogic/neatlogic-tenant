package codedriver.module.tenant.api.reminder;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.reminder.service.GlobalReminderService;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @program: codedriver
 * @description:
 * @create: 2019-12-10 12:01
 **/
@Service
public class UpdatePopApi extends ApiComponentBase {

    @Autowired
    private GlobalReminderService reminderService;

    @Override
    public String getToken() {
        return "globalReminder/updateMessageKeep";
    }

    @Override
    public String getName() {
        return "重置弹窗有效性接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({ @Param( name = "idStr", type = ApiParamType.STRING, desc = "ID拼接字符串，多个使用“,” 隔开", isRequired = true)})
    @Description(desc = "重置弹窗有效性接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String idStr = jsonObj.getString("idStr");
        if (idStr != null && !("").equals(idStr)){
            String[] idArray = idStr.split(",");
            for (String id : idArray){
                reminderService.updateMessageKeepStatus(UserContext.get().getUserId(), Long.parseLong(id));
            }
        }
        return new JSONObject();
    }

}

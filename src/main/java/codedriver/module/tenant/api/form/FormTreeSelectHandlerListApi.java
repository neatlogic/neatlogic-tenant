package codedriver.module.tenant.api.form;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.auth.label.FORM_MODIFY;
import codedriver.framework.form.treeselect.core.ITreeSelectDataSourceHandler;
import codedriver.framework.form.treeselect.core.TreeSelectDataSourceFactory;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@OperationType(type = OperationTypeEnum.CREATE)
@AuthAction(action = FORM_MODIFY.class)
public class FormTreeSelectHandlerListApi extends PrivateApiComponentBase {

    @Override
    public String getToken() {
        return "form/treeselect/handler/list";
    }

    @Override
    public String getName() {
        return "获取树形下拉框数据源列表";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
    })
    @Output({

    })
    @Description(desc = "获取树形下拉框数据源列表接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONArray jsonArray = new JSONArray();
        List<ITreeSelectDataSourceHandler> handlerList = TreeSelectDataSourceFactory.getHandlerList();
        handlerList.forEach(handler ->{
            JSONObject result = new JSONObject();
            result.put("handler",handler.getHandler());
            result.put("handlerName",handler.getHandlerName());
            result.put("config",handler.getConfig());
            jsonArray.add(result);
        });
       return jsonArray;
    }
}

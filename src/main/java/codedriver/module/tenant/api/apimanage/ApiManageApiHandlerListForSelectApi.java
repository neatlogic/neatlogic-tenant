package codedriver.module.tenant.api.apimanage;

import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.framework.restful.core.ApiComponentFactory;
import codedriver.framework.restful.dto.ApiHandlerVo;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class ApiManageApiHandlerListForSelectApi extends ApiComponentBase {

    @Override
    public String getToken() {
        return "apimanage/apihandler/list/forselect";
    }

    @Override
    public String getName() {
        return "获取接口组件列表_下拉框";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({})
    @Output({@Param(explode = ValueTextVo[].class, desc = "接口组件列表")})
    @Description(desc = "获取接口组件列表_下拉框")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {

        List<ValueTextVo> resultList = new ArrayList<>();

        List<ApiHandlerVo> apiHandlerList = new ArrayList<>();
        for (ApiHandlerVo apiHandlerVo : ApiComponentFactory.getApiHandlerList()) {
            if (apiHandlerVo.isPrivate() != false) {
                continue;
            }
            apiHandlerList.add(apiHandlerVo);
        }
        apiHandlerList.sort(Comparator.comparing(ApiHandlerVo::getHandler));

        for(ApiHandlerVo vo : apiHandlerList){
            resultList.add(new ValueTextVo(vo.getHandler(),vo.getName()));
        }

        return resultList;

    }

}

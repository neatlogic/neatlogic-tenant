/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package neatlogic.module.tenant.api.apimanage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import neatlogic.framework.auth.core.AuthAction;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import neatlogic.framework.common.dto.ValueTextVo;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.restful.core.publicapi.PublicApiComponentFactory;
import neatlogic.framework.restful.dto.ApiHandlerVo;

@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class ApiManageApiHandlerListForSelectApi extends PrivateApiComponentBase {

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

        List<ApiHandlerVo> apiHandlerList = PublicApiComponentFactory.getApiHandlerList();
        
        apiHandlerList.sort(Comparator.comparing(ApiHandlerVo::getHandler));

        for(ApiHandlerVo vo : apiHandlerList){
            resultList.add(new ValueTextVo(vo.getHandler(),vo.getName()));
        }

        return resultList;

    }

}

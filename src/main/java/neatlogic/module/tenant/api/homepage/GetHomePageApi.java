package neatlogic.module.tenant.api.homepage;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.HOME_PAGE_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dao.mapper.HomePageMapper;
import neatlogic.framework.dto.AuthorityVo;
import neatlogic.framework.dto.HomePageVo;
import neatlogic.framework.exception.homepage.HomePageNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@AuthAction(action = HOME_PAGE_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class GetHomePageApi extends PrivateApiComponentBase {

    @Resource
    private HomePageMapper homePageMapper;

    @Override
    public String getToken() {
        return "homepage/get";
    }

    @Override
    public String getName() {
        return "获取首页配置信息";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "id", type = ApiParamType.LONG, isRequired = true, desc = "common.id")
    })
    @Output({
            @Param(explode = HomePageVo.class, desc = "common.tbodylist")
    })
    @Description(desc = "获取首页配置信息")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long id = jsonObj.getLong("id");
        HomePageVo homePage = homePageMapper.getHomePageById(id);
        if (homePage == null) {
			throw new HomePageNotFoundException(id);
        }

        List<AuthorityVo> authorityVoList = homePageMapper.getHomePageAuthorityListByHomePageId(id);
        homePage.setAuthorityList(AuthorityVo.getAuthorityList(authorityVoList));
        return homePage;
    }

}

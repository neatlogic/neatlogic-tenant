/*Copyright (C) $today.year  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

package neatlogic.module.tenant.api.homepage;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.HOME_PAGE_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dao.mapper.HomePageMapper;
import neatlogic.framework.dto.AuthorityVo;
import neatlogic.framework.dto.FieldValidResultVo;
import neatlogic.framework.dto.HomePageVo;
import neatlogic.framework.exception.homepage.HomePageNameRepeatException;
import neatlogic.framework.exception.homepage.HomePageNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.IValid;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.RegexUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
@Transactional
@OperationType(type = OperationTypeEnum.CREATE)
@AuthAction(action = HOME_PAGE_MODIFY.class)
public class SaveHomePageApi extends PrivateApiComponentBase {

    @Resource
    private HomePageMapper homePageMapper;

    @Override
    public String getToken() {
        return "homepage/save";
    }

    @Override
    public String getName() {
        return "nmtah.savehomepageapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "id", type = ApiParamType.STRING, desc = "common.id"),
            @Param(name = "name", type = ApiParamType.REGEX, rule = RegexUtils.NAME, isRequired = true, maxLength = 50, desc = "common.name"),
            @Param(name = "isActive", type = ApiParamType.ENUM, isRequired = true, desc = "common.isactive", rule = "0,1"),
            @Param(name = "authorityList", type = ApiParamType.JSONARRAY, desc = "common.authoritylist", help = "可多选，格式[\"user#userUuid\",\"team#teamUuid\",\"role#roleUuid\"]"),
            @Param(name = "config", type = ApiParamType.JSONOBJECT, isRequired = true, desc = "common.config")
    })
    @Output({
            @Param(name = "Return", type = ApiParamType.LONG, desc = "common.id")
    })
    @Description(desc = "nmtah.savehomepageapi.getname")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long id = jsonObj.getLong("id");
        HomePageVo homePage = jsonObj.toJavaObject(HomePageVo.class);
        if (id != null) {
            HomePageVo oldHomePage = homePageMapper.getHomePageById(id);
            if (oldHomePage == null) {
                throw new HomePageNotFoundException(id);
            }
            homePage.setSort(oldHomePage.getSort());
            homePageMapper.deleteHomePageAuthorityByHomePageId(homePage.getId());
        } else {
            Integer sort = homePageMapper.getMaxSort();
            if(sort == null) {
                sort = 0;
            }
            sort++;
            homePage.setSort(sort);
        }
        homePageMapper.insertHomePage(homePage);
        List<String> authorityList = homePage.getAuthorityList();
        if (CollectionUtils.isNotEmpty(authorityList)) {
            List<AuthorityVo> authorityVoList = AuthorityVo.getAuthorityVoList(authorityList, null);
            for(AuthorityVo authorityVo : authorityVoList) {
                homePageMapper.insertHomePageAuthority(homePage.getId(), authorityVo);
            }
        }
        return homePage.getId();
    }

    public IValid name() {
        return value -> {
            HomePageVo homePage = value.toJavaObject(HomePageVo.class);
            if (homePageMapper.checkHomePageNameIsRepeat(homePage) > 0) {
                return new FieldValidResultVo(new HomePageNameRepeatException(homePage.getName()));
            }
            return new FieldValidResultVo();
        };
    }

}

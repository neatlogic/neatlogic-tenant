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
import neatlogic.framework.dto.HomePageVo;
import neatlogic.framework.exception.homepage.HomePageNotFoundException;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.dao.mapper.HomePageMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@Transactional
@OperationType(type = OperationTypeEnum.UPDATE)
@AuthAction(action = HOME_PAGE_MODIFY.class)
public class MoveHomePageApi extends PrivateApiComponentBase {

    @Resource
    private HomePageMapper homePageMapper;

    @Override
    public String getToken() {
        return "homepage/move";
    }

    @Override
    public String getName() {
        return "nmtah.movehomepageapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "id", type = ApiParamType.STRING, isRequired = true, desc = "common.id"),
            @Param(name = "sort", type = ApiParamType.INTEGER, isRequired = true, desc = "common.sort")
    })
    @Description(desc = "nmtah.movehomepageapi.getname")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long id = jsonObj.getLong("id");
        HomePageVo homePage = homePageMapper.getHomePageById(id);
        if (homePage == null) {
            throw new HomePageNotFoundException(id);
        }
        int oldSort = homePage.getSort();
        int newSort = jsonObj.getIntValue("sort");
        if(oldSort < newSort) {//往后移动
            homePageMapper.updateSortDecrement(oldSort, newSort);
        }else if(oldSort > newSort) {//往前移动
            homePageMapper.updateSortIncrement(newSort, oldSort);
        }
        homePage.setSort(newSort);
        homePageMapper.updateHomePageSortById(homePage);
        return null;
    }

}

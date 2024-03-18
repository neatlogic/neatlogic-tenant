/*Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.

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

package neatlogic.module.tenant.api.form;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.common.util.PageUtil;
import neatlogic.framework.dependency.constvalue.FrameworkFromType;
import neatlogic.framework.dependency.core.DependencyManager;
import neatlogic.framework.dependency.dto.DependencyInfoVo;
import neatlogic.framework.form.dao.mapper.FormMapper;
import neatlogic.framework.form.exception.FormNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service

@OperationType(type = OperationTypeEnum.SEARCH)
@Deprecated
public class FormReferenceList extends PrivateApiComponentBase {

    @Resource
    private FormMapper formMapper;

    @Override
    public String getToken() {
        return "form/reference/list";
    }

    @Override
    public String getName() {
        return "表单引用列表接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "formUuid", type = ApiParamType.STRING, isRequired = true, desc = "表单uuid"),
            @Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否需要分页，默认true"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页条目"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页")
    })
    @Output({
            @Param(explode = BasePageVo.class),
            @Param(name = "list", explode = DependencyInfoVo[].class, desc = "引用列表")
    })
    @Description(desc = "表单引用列表接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String formUuid = jsonObj.getString("formUuid");
        if (formMapper.checkFormIsExists(formUuid) == 0) {
            throw new FormNotFoundException(formUuid);
        }
        BasePageVo basePageVo = JSON.toJavaObject(jsonObj, BasePageVo.class);
        JSONObject resultObj = new JSONObject();
        int pageCount = 0;
        int rowNum = DependencyManager.getDependencyCount(FrameworkFromType.FORM, formUuid);
        if(rowNum > 0){
            pageCount = PageUtil.getPageCount(rowNum, basePageVo.getPageSize());
            List<DependencyInfoVo> list = DependencyManager.getDependencyList(FrameworkFromType.FORM, formUuid, basePageVo);
            resultObj.put("list", list);
        }else {
            resultObj.put("list", new ArrayList<>());
        }
        resultObj.put("currentPage", basePageVo.getCurrentPage());
        resultObj.put("pageSize", basePageVo.getPageSize());
        resultObj.put("pageCount", pageCount);
        resultObj.put("rowNum", rowNum);
        return resultObj;
    }

}

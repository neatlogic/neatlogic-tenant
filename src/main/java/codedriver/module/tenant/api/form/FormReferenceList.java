/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.form;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.dependency.constvalue.CalleeType;
import codedriver.framework.dependency.core.DependencyManager;
import codedriver.framework.form.dao.mapper.FormMapper;
import codedriver.framework.form.exception.FormNotFoundException;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
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
            @Param(name = "list", explode = ValueTextVo[].class, desc = "引用列表")
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
        int rowNum = DependencyManager.getDependencyCount(CalleeType.FORM, formUuid);
        if(rowNum > 0){
            pageCount = PageUtil.getPageCount(rowNum, basePageVo.getPageSize());
            List<ValueTextVo> list = DependencyManager.getDependencyList(CalleeType.FORM, formUuid, basePageVo);
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

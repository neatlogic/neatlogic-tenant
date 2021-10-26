/*
 * Copyright (c)  2021 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.runner;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.dto.runner.RunnerGroupVo;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.tagent.auth.label.TAGENT_BASE;
import codedriver.framework.tagent.dao.mapper.TagentMapper;
import codedriver.framework.tagent.dto.TagentVo;
import codedriver.framework.util.TableResultUtil;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@AuthAction(action = TAGENT_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class RunnerGroupListApi extends PrivateApiComponentBase {

    @Resource
    TagentMapper tagentMapper;

    @Override
    public String getName() {
        return "获取所有runner组";
    }

    @Override
    public String getToken() {
        return "runnergroup/list";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Description(desc = "获取所有runner组")
    @Input({
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页数据条目"),
            @Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否需要分页，默认true")
    })
    @Output({
            @Param(name = "tbodyList",explode = RunnerGroupVo[].class,desc = "runner 组列表")
    })
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        TagentVo tagentVo =JSONObject.toJavaObject(paramObj, TagentVo.class);
        int rowNum =tagentMapper.searchTagentRunnerCount();
        tagentVo.setRowNum(rowNum);
        List<RunnerGroupVo> runnerGroupVoList =tagentMapper.searchTagentRunnerGroup();
        return TableResultUtil.getResult(runnerGroupVoList, tagentVo);
    }


}

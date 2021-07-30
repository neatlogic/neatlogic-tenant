/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.user;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.UserTitleVo;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class UserTitleListApi extends PrivateApiComponentBase {

    @Resource
    private UserMapper userMapper;

    @Override
    public String getToken() {
        return "user/title/search";
    }

    @Override
    public String getName() {
        return "查询用户头衔列表";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "keyword", type = ApiParamType.STRING, xss = true, desc = "关键词"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页数据条目"),
            @Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否需要分页，默认true")
    })
    @Output({
            @Param(name = "tbodyList", explode = UserTitleVo[].class, desc = "列表"),
            @Param(explode = BasePageVo.class),
    })
    @Description(desc = "查询用户头衔列表接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject resultObj = new JSONObject();
        UserTitleVo userTitleVo = JSON.toJavaObject(jsonObj, UserTitleVo.class);
        if (userTitleVo.getNeedPage()) {
            int rowNum = userMapper.searchUserTitleCount(userTitleVo);
            resultObj.put("rowNum", rowNum);
            resultObj.put("pageSize", userTitleVo.getPageSize());
            resultObj.put("currentPage", userTitleVo.getCurrentPage());
            resultObj.put("pageCount", PageUtil.getPageCount(rowNum, userTitleVo.getPageSize()));
        }
        resultObj.put("tbodyList", userMapper.searchUserTitle(userTitleVo));
        return resultObj;
    }
}

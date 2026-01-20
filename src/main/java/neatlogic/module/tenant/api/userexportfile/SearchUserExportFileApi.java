/*
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package neatlogic.module.tenant.api.userexportfile;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.core.AuthActionChecker;
import neatlogic.framework.auth.label.NoAuth;
import neatlogic.framework.auth.label.USER_EXPORT_FILE_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.constvalue.GroupSearch;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.dao.mapper.UserExportFileMapper;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.userexportfile.dto.UserExportFileSearchVo;
import neatlogic.framework.userexportfile.dto.UserExportFileVo;
import neatlogic.framework.util.TableResultUtil;
import neatlogic.framework.util.TimeUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
@AuthAction(action = NoAuth.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class SearchUserExportFileApi extends PrivateApiComponentBase {

    @Resource
    private UserExportFileMapper userExportFileMapper;

    @Override
    public String getName() {
        return "nmtau.searchuserexportfileapi.getname";
    }

    @Input({
            @Param(name = "keyword", type = ApiParamType.STRING, desc = "common.keyword"),
            @Param(name = "defaultValue", type = ApiParamType.JSONARRAY, desc = "common.defaultvalue"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "common.currentpage"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "common.pagesize"),
            @Param(name = "timeRange", type = ApiParamType.INTEGER, desc = "common.duration"),
            @Param(name = "timeUnit", type = ApiParamType.STRING, desc = "common.timeunit"),
            @Param(name = "startTime", type = ApiParamType.LONG, desc = "common.starttime"),
            @Param(name = "endTime", type = ApiParamType.LONG, desc = "common.endtime"),
            @Param(name = "isAll", type = ApiParamType.ENUM, rule = "0,1", desc = "nmtau.searchuserexportfileapi.input.param.desc"),
            @Param(name = "userUuid", type = ApiParamType.STRING, desc = "common.useruuid"),
    })
    @Output({
            @Param(explode = BasePageVo.class),
            @Param(name = "tbodyList", explode = BasePageVo[].class, desc = "common.tbodylist")
    })
    @Description(desc = "nmtau.searchuserexportfileapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        List<UserExportFileVo> tbodyList = new ArrayList<>();
        UserExportFileSearchVo searchVo = paramObj.toJavaObject(UserExportFileSearchVo.class);
        JSONArray defaultValue = searchVo.getDefaultValue();
        if (CollectionUtils.isNotEmpty(defaultValue)) {
            List<Long> idList = defaultValue.toJavaList(Long.class);
            tbodyList = userExportFileMapper.getUserExportFileListByIdList(idList);
        } else {
            //将时间范围转为 开始时间、结束时间
            if (searchVo.getStartTime() == null && searchVo.getEndTime() == null) {
                Integer timeRange = paramObj.getInteger("timeRange");
                String timeUnit = paramObj.getString("timeUnit");
                if (timeRange != null && StringUtils.isNotBlank(timeUnit)) {
                    searchVo.setStartTime(TimeUtil.recentTimeTransfer(timeRange, timeUnit));
                    searchVo.setEndTime(new Date());
                }
            }
            boolean searchAllUser = false;
            Integer isAll = paramObj.getInteger("isAll");
            if (Objects.equals(isAll, 1)) {
                if (AuthActionChecker.check(USER_EXPORT_FILE_MODIFY.class)) {
                    searchAllUser = true;
                }
            }
            if (!searchAllUser) {
                searchVo.setUserUuid(UserContext.get().getUserUuid());
            } else {
                String userUuid = searchVo.getUserUuid();
                if (StringUtils.isNotEmpty(userUuid) && userUuid.startsWith(GroupSearch.USER.getValuePlugin())) {
                    searchVo.setUserUuid(userUuid.substring(GroupSearch.USER.getValuePlugin().length()));
                }
            }
            int rowNum = userExportFileMapper.getUserExportFileCount(searchVo);
            if (rowNum > 0) {
                searchVo.setRowNum(rowNum);
                tbodyList = userExportFileMapper.getUserExportFileList(searchVo);
            }
        }
        return TableResultUtil.getResult(tbodyList, searchVo);
    }

    @Override
    public String getToken() {
        return "user/exportfile/search";
    }
}

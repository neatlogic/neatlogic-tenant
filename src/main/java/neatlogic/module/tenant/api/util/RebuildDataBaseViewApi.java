/*
 *
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 */

package neatlogic.module.tenant.api.util;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.ADMIN;
import neatlogic.framework.dao.mapper.DataBaseViewInfoMapper;
import neatlogic.framework.rebuilddatabaseview.core.RebuildDataBaseViewManager;
import neatlogic.framework.rebuilddatabaseview.core.ViewStatusInfo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.TableResultUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
@Transactional
@AuthAction(action = ADMIN.class)
@OperationType(type = OperationTypeEnum.OPERATE)
public class RebuildDataBaseViewApi extends PrivateApiComponentBase {

    @Resource
    private DataBaseViewInfoMapper dataBaseViewInfoMapper;

    @Override
    public String getName() {
        return "重建数据库视图";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({})
    @Output({
            @Param(name = "tbodyList", explode = ViewStatusInfo.class, desc = "视图列表")
    })
    @Description(desc = "重建数据库视图")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        dataBaseViewInfoMapper.deleteDataBaseViewInfo();
        List<ViewStatusInfo> resultList = RebuildDataBaseViewManager.createOrReplaceView();
        return TableResultUtil.getResult(resultList);
    }

    @Override
    public String getToken() {
        return "util/rebuilddatabaesview";
    }
}

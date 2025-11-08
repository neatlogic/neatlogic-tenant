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

package neatlogic.module.tenant.api.database;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.DATA_WAREHOUSE_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.datawarehouse.dao.mapper.DatabaseMapper;
import neatlogic.framework.datawarehouse.dto.DatabaseVo;
import neatlogic.framework.datawarehouse.exceptions.DatabaseNotFoundException;
import neatlogic.framework.file.dao.mapper.FileMapper;
import neatlogic.framework.file.dto.FileVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
@AuthAction(action = DATA_WAREHOUSE_MODIFY.class)
@OperationType(type = OperationTypeEnum.OPERATE)
public class GetDataBaseApi extends PrivateApiComponentBase {

    @Resource
    private DatabaseMapper databaseMapper;

    @Resource
    private FileMapper fileMapper;

    @Override
    public String getName() {
        return "nmtad.getdatabaseapi.getname";
    }

    @Input({
            @Param(name = "id", type = ApiParamType.LONG, isRequired = true, desc = "common.id")
    })
    @Output({
            @Param(explode = DatabaseVo.class, desc = "common.tbodylist")
    })
    @Description(desc = "nmtad.getdatabaseapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        Long id = paramObj.getLong("id");
        DatabaseVo databaseVo = databaseMapper.getDataBaseById(id);
        if (databaseVo == null) {
            throw new DatabaseNotFoundException(id);
        }
        List<Long> fileIdList = databaseVo.getFileIdList();
        if (CollectionUtils.isNotEmpty(fileIdList)) {
            List<FileVo> fileList = fileMapper.getFileListByIdList(fileIdList);
            databaseVo.setFileList(fileList);
        } else {
            databaseVo.setFileList(new ArrayList<>());
        }
        return databaseVo;
    }

    @Override
    public String getToken() {
        return "database/get";
    }
}

/*
 * Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package neatlogic.module.tenant.api.datawarehouse;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.DATA_WAREHOUSE_BASE;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.datawarehouse.dao.mapper.DataWarehouseDataSourceMapper;
import neatlogic.framework.datawarehouse.dto.DataSourceVo;
import neatlogic.framework.datawarehouse.exceptions.DataSourceIsNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateBinaryStreamApiComponentBase;
import neatlogic.framework.util.FileUtil;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@AuthAction(action = DATA_WAREHOUSE_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ExportDataSourceApi extends PrivateBinaryStreamApiComponentBase {

    @Resource
    private DataWarehouseDataSourceMapper dataSourceMapper;

    @Override
    public String getToken() {
        return "datawarehouse/datasource/export";
    }

    @Override
    public String getName() {
        return "导出数据仓库数据源";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "idList", type = ApiParamType.JSONARRAY, desc = "数据源id列表", isRequired = true)})
    @Output({@Param(explode = DataSourceVo.class)})
    @Description(desc = "导出数据仓库数据源")
    @Override
    public Object myDoService(JSONObject paramObj, HttpServletRequest request, HttpServletResponse response) throws Exception {
        List<Long> idList = paramObj.getJSONArray("idList").toJavaList(Long.class);
        List<Long> existedIdList = dataSourceMapper.getExistIdListByIdList(idList);
        idList.removeAll(existedIdList);
        if (CollectionUtils.isNotEmpty(idList)) {
            throw new DataSourceIsNotFoundException(StringUtils.join(idList, ","));
        }
        String fileName = FileUtil.getEncodedFileName("数据仓库数据源." + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".pak");
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", " attachment; filename=\"" + fileName + "\"");
        try (ZipOutputStream zos = new ZipOutputStream(response.getOutputStream())) {
            for (Long id : existedIdList) {
                DataSourceVo dataSourceVo = dataSourceMapper.getDataSourceById(id);
                zos.putNextEntry(new ZipEntry(dataSourceVo.getName() + ".json"));
                zos.write(JSONObject.toJSONBytes(dataSourceVo));
                zos.closeEntry();
            }
        }
        return null;
    }

}

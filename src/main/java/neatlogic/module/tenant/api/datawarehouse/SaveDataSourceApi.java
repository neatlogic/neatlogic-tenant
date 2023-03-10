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
import neatlogic.framework.auth.label.DATA_WAREHOUSE_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.datawarehouse.dao.mapper.DataWarehouseDataSourceMapper;
import neatlogic.framework.datawarehouse.dto.DataSourceFieldVo;
import neatlogic.framework.datawarehouse.dto.DataSourceVo;
import neatlogic.framework.datawarehouse.exceptions.DataSourceIsNotFoundException;
import neatlogic.framework.datawarehouse.exceptions.DataSourceNameIsExistsException;
import neatlogic.framework.datawarehouse.service.DataSourceService;
import neatlogic.framework.datawarehouse.utils.ReportXmlUtil;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.RegexUtils;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
@AuthAction(action = DATA_WAREHOUSE_MODIFY.class)
@OperationType(type = OperationTypeEnum.UPDATE)
@Transactional
public class SaveDataSourceApi extends PrivateApiComponentBase {

    @Resource
    private DataWarehouseDataSourceMapper dataSourceMapper;

    @Resource
    DataSourceService dataSourceService;

    @Override
    public String getToken() {
        return "datawarehouse/datasource/save";
    }

    @Override
    public String getName() {
        return "???????????????????????????";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "id", type = ApiParamType.LONG, desc = "id???????????????????????????"),
            @Param(name = "name", type = ApiParamType.REGEX, desc = "????????????", rule = RegexUtils.ENGLISH_NAME, maxLength = 50, isRequired = true, xss = true),
            @Param(name = "label", type = ApiParamType.STRING, desc = "??????", maxLength = 50, isRequired = true, xss = true),
            @Param(name = "xml", type = ApiParamType.STRING, desc = "??????xml", isRequired = true),
            @Param(name = "isActive", type = ApiParamType.INTEGER, desc = "????????????", isRequired = true),
            @Param(name = "mode", type = ApiParamType.ENUM, rule = "append,replace", desc = "????????????", isRequired = true),
            @Param(name = "expireCount", type = ApiParamType.INTEGER, desc = "???????????????"),
            @Param(name = "expireUnit", type = ApiParamType.ENUM, rule = "minute,hour,day", desc = "???????????????"),
            @Param(name = "expireUnit", type = ApiParamType.ENUM, rule = "minute,hour,day", desc = "???????????????"),
            @Param(name = "dbType", type = ApiParamType.STRING, isRequired = true, desc = "???????????????")})
    @Description(desc = "?????????????????????????????????")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        DataSourceVo newDataSourceVo = JSONObject.toJavaObject(jsonObj, DataSourceVo.class);
        if (dataSourceMapper.checkDataSourceNameIsExists(newDataSourceVo) > 0) {
            throw new DataSourceNameIsExistsException(newDataSourceVo.getName());
        }
        /*FileVo fileVo = fileMapper.getFileById(newDataSourceVo.getFileId());
        if (fileVo == null) {
            throw new DataSourceFileIsNotFoundException();
        }
        String xml = IOUtils.toString(FileUtil.getData(fileVo.getPath()), StandardCharsets.UTF_8);*/
        String xml = jsonObj.getString("xml");
        DataSourceVo dataSourceVo = ReportXmlUtil.generateDataSourceFromXml(xml);
        newDataSourceVo.setDataCount(0);

        //reportDataSourceVo.setConditionList(dataSourceVo.getConditionList());
        Long id = jsonObj.getLong("id");
        if (id == null) {
            newDataSourceVo.setFieldList(dataSourceVo.getFieldList());
            dataSourceService.insertDataSource(newDataSourceVo);
        } else {
            DataSourceVo oldDatasourceVo = dataSourceMapper.getDataSourceById(id);
            //??????????????????????????????????????????????????????????????????????????????????????????????????????????????????
            if (oldDatasourceVo == null) {
                throw new DataSourceIsNotFoundException(id);
            }
            // ??????????????????
            List<DataSourceFieldVo> newFieldList = dataSourceService.revertFieldCondition(dataSourceVo.getFieldList(), oldDatasourceVo.getFieldList());
            dataSourceVo.setFieldList(newFieldList);
            dataSourceService.updateDataSource(newDataSourceVo, dataSourceVo, oldDatasourceVo);
        }
        dataSourceService.createDataSourceSchema(newDataSourceVo);
        dataSourceService.loadOrUnloadReportDataSourceJob(newDataSourceVo);

        return null;
    }

}

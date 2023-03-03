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
        return "保存数据仓库数据源";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "id", type = ApiParamType.LONG, desc = "id，存在代表需要替换"),
            @Param(name = "name", type = ApiParamType.REGEX, desc = "唯一标识", rule = RegexUtils.ENGLISH_NAME, maxLength = 50, isRequired = true, xss = true),
            @Param(name = "label", type = ApiParamType.STRING, desc = "名称", maxLength = 50, isRequired = true, xss = true),
            @Param(name = "xml", type = ApiParamType.STRING, desc = "配置xml", isRequired = true),
            @Param(name = "isActive", type = ApiParamType.INTEGER, desc = "是否激活", isRequired = true),
            @Param(name = "mode", type = ApiParamType.ENUM, rule = "append,replace", desc = "同步模式", isRequired = true),
            @Param(name = "expireCount", type = ApiParamType.INTEGER, desc = "有效期时间"),
            @Param(name = "expireUnit", type = ApiParamType.ENUM, rule = "minute,hour,day", desc = "有效期单位"),
            @Param(name = "expireUnit", type = ApiParamType.ENUM, rule = "minute,hour,day", desc = "有效期单位"),
            @Param(name = "dbType", type = ApiParamType.STRING, isRequired = true, desc = "数据库类型")})
    @Description(desc = "保存数据仓库数据源接口")
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
            //比较新老数据，找出需要新增、修改和删除的属性，这样做的目的是为了保留条件配置
            if (oldDatasourceVo == null) {
                throw new DataSourceIsNotFoundException(id);
            }
            // 还原条件设置
            List<DataSourceFieldVo> newFieldList = dataSourceService.revertFieldCondition(dataSourceVo.getFieldList(), oldDatasourceVo.getFieldList());
            dataSourceVo.setFieldList(newFieldList);
            dataSourceService.updateDataSource(newDataSourceVo, dataSourceVo, oldDatasourceVo);
        }
        dataSourceService.createDataSourceSchema(newDataSourceVo);
        dataSourceService.loadOrUnloadReportDataSourceJob(newDataSourceVo);

        return null;
    }

}

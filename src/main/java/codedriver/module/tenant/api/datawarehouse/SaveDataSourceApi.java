/*
 * Copyright(c) 2022 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.datawarehouse;

import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.auth.label.DATA_WAREHOUSE_MODIFY;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.datawarehouse.dao.mapper.DataWarehouseDataSourceMapper;
import codedriver.framework.datawarehouse.dao.mapper.DataWarehouseDataSourceSchemaMapper;
import codedriver.framework.datawarehouse.dto.DataSourceFieldVo;
import codedriver.framework.datawarehouse.dto.DataSourceParamVo;
import codedriver.framework.datawarehouse.dto.DataSourceVo;
import codedriver.framework.datawarehouse.exceptions.CreateDataSourceSchemaException;
import codedriver.framework.datawarehouse.exceptions.DataSourceIsNotFoundException;
import codedriver.framework.datawarehouse.exceptions.DataSourceNameIsExistsException;
import codedriver.framework.datawarehouse.utils.ReportXmlUtil;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.scheduler.core.IJob;
import codedriver.framework.scheduler.core.SchedulerManager;
import codedriver.framework.scheduler.dto.JobObject;
import codedriver.framework.transaction.core.EscapeTransactionJob;
import codedriver.framework.util.RegexUtils;
import codedriver.module.framework.scheduler.datawarehouse.ReportDataSourceJob;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AuthAction(action = DATA_WAREHOUSE_MODIFY.class)
@OperationType(type = OperationTypeEnum.UPDATE)
@Transactional
public class SaveDataSourceApi extends PrivateApiComponentBase {

    @Resource
    private DataWarehouseDataSourceMapper dataSourceMapper;


    @Resource
    private DataWarehouseDataSourceSchemaMapper dataSourceSchemaMapper;

    @Resource
    private SchedulerManager schedulerManager;

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
            @Param(name = "cronExpression", type = ApiParamType.STRING, desc = "定时策略")})
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
            dataSourceMapper.insertDataSource(newDataSourceVo);
            if (CollectionUtils.isNotEmpty(newDataSourceVo.getFieldList())) {
                for (DataSourceFieldVo field : newDataSourceVo.getFieldList()) {
                    field.setDataSourceId(newDataSourceVo.getId());
                    dataSourceMapper.insertDataSourceField(field);
                }
            }
            if (CollectionUtils.isNotEmpty(newDataSourceVo.getParamList())) {
                for (DataSourceParamVo param : newDataSourceVo.getParamList()) {
                    param.setDataSourceId(newDataSourceVo.getId());
                    dataSourceMapper.insertDataSourceParam(param);
                }
            }
        } else {
            DataSourceVo oldDatasourceVo = dataSourceMapper.getDataSourceById(id);
            //比较新老数据，找出需要新增、修改和删除的属性，这样做的目的是为了保留条件配置
            if (oldDatasourceVo == null) {
                throw new DataSourceIsNotFoundException(id);
            }
            List<DataSourceFieldVo> deleteFieldList = oldDatasourceVo.getFieldList().stream().filter(d -> !dataSourceVo.getFieldList().contains(d)).collect(Collectors.toList());
            List<DataSourceFieldVo> updateFieldList = dataSourceVo.getFieldList().stream().filter(d -> oldDatasourceVo.getFieldList().contains(d)).collect(Collectors.toList());
            List<DataSourceFieldVo> insertFieldList = dataSourceVo.getFieldList().stream().filter(d -> !oldDatasourceVo.getFieldList().contains(d)).collect(Collectors.toList());
            //用回旧的fieldId
            if (CollectionUtils.isNotEmpty(updateFieldList)) {
                for (DataSourceFieldVo field : updateFieldList) {
                    Optional<DataSourceFieldVo> op = oldDatasourceVo.getFieldList().stream().filter(d -> d.equals(field)).findFirst();
                    op.ifPresent(dataSourceFieldVo -> field.setId(dataSourceFieldVo.getId()));
                }
            }
            newDataSourceVo.setFieldList(null);//清空旧数据
            newDataSourceVo.addField(insertFieldList);
            newDataSourceVo.addField(updateFieldList);

            List<DataSourceParamVo> deleteParamList = oldDatasourceVo.getParamList().stream().filter(d -> !dataSourceVo.getParamList().contains(d)).collect(Collectors.toList());
            List<DataSourceParamVo> updateParamList = dataSourceVo.getParamList().stream().filter(d -> oldDatasourceVo.getParamList().contains(d)).collect(Collectors.toList());
            List<DataSourceParamVo> insertParamList = dataSourceVo.getParamList().stream().filter(d -> !oldDatasourceVo.getParamList().contains(d)).collect(Collectors.toList());
            //用回旧的paramId
            if (CollectionUtils.isNotEmpty(updateParamList)) {
                for (DataSourceParamVo param : updateParamList) {
                    Optional<DataSourceParamVo> op = oldDatasourceVo.getParamList().stream().filter(d -> d.equals(param)).findFirst();
                    op.ifPresent(dataSourceParamVo -> param.setId(dataSourceParamVo.getId()));
                }
            }
            newDataSourceVo.setParamList(null);//清空旧数据
            newDataSourceVo.addParam(insertParamList);
            newDataSourceVo.addParam(updateParamList);

            //FIXME 检查数据源是否被使用
            dataSourceMapper.updateDataSource(newDataSourceVo);

            if (CollectionUtils.isNotEmpty(deleteFieldList)) {
                for (DataSourceFieldVo field : deleteFieldList) {
                    dataSourceMapper.deleteDataSourceFieldById(field.getId());
                }
            }

            if (CollectionUtils.isNotEmpty(updateFieldList)) {
                for (DataSourceFieldVo field : updateFieldList) {
                    dataSourceMapper.updateDataSourceField(field);
                }
            }

            if (CollectionUtils.isNotEmpty(insertFieldList)) {
                for (DataSourceFieldVo field : insertFieldList) {
                    field.setDataSourceId(newDataSourceVo.getId());
                    dataSourceMapper.insertDataSourceField(field);
                }
            }

            if (CollectionUtils.isNotEmpty(deleteParamList)) {
                for (DataSourceParamVo param : deleteParamList) {
                    dataSourceMapper.deleteDataSourceParamById(param.getId());
                }
            }

            if (CollectionUtils.isNotEmpty(updateParamList)) {
                for (DataSourceParamVo param : updateParamList) {
                    dataSourceMapper.updateDataSourceParam(param);
                }
            }

            if (CollectionUtils.isNotEmpty(insertParamList)) {
                for (DataSourceParamVo param : insertParamList) {
                    param.setDataSourceId(newDataSourceVo.getId());
                    dataSourceMapper.insertDataSourceParam(param);
                }
            }

        }
        //由于以下操作是DDL操作，所以需要使用EscapeTransactionJob避开当前事务，否则在进行DDL操作之前事务就会提交，如果DDL出错，则上面的事务就无法回滚了
        EscapeTransactionJob.State s = new EscapeTransactionJob(() -> {
            dataSourceSchemaMapper.deleteDataSourceTable(newDataSourceVo);
            dataSourceSchemaMapper.createDataSourceTable(newDataSourceVo);
        }).execute();
        if (!s.isSucceed()) {
            throw new CreateDataSourceSchemaException(newDataSourceVo);
        }

        String tenantUuid = TenantContext.get().getTenantUuid();
        IJob jobHandler = SchedulerManager.getHandler(ReportDataSourceJob.class.getName());
        JobObject jobObject = new JobObject.Builder(newDataSourceVo.getId().toString(), jobHandler.getGroupName(), jobHandler.getClassName(), tenantUuid)
                .withCron(newDataSourceVo.getCronExpression())
                .addData("datasourceId", newDataSourceVo.getId())
                .build();
        if (StringUtils.isNotBlank(newDataSourceVo.getCronExpression())) {
            schedulerManager.loadJob(jobObject);
        } else {
            schedulerManager.unloadJob(jobObject);
        }

        return null;
    }

}

/*
 * Copyright(c) 2022 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.datawarehouse;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.auth.label.DATA_WAREHOUSE_MODIFY;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.util.FileUtil;
import codedriver.framework.datawarehouse.dao.mapper.DataWarehouseDataSourceMapper;
import codedriver.framework.datawarehouse.dao.mapper.DataWarehouseDataSourceSchemaMapper;
import codedriver.framework.datawarehouse.dto.DataSourceFieldVo;
import codedriver.framework.datawarehouse.dto.DataSourceVo;
import codedriver.framework.datawarehouse.exceptions.CreateDataSourceSchemaException;
import codedriver.framework.datawarehouse.exceptions.DataSourceFileIsNotFoundException;
import codedriver.framework.datawarehouse.exceptions.DataSourceIsNotFoundException;
import codedriver.framework.datawarehouse.exceptions.DataSourceNameIsExistsException;
import codedriver.framework.datawarehouse.utils.ReportXmlUtil;
import codedriver.framework.file.dao.mapper.FileMapper;
import codedriver.framework.file.dto.FileVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.transaction.core.EscapeTransactionJob;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AuthAction(action = DATA_WAREHOUSE_MODIFY.class)
@OperationType(type = OperationTypeEnum.CREATE)
@Transactional
public class ImportReportDataSourceApi extends PrivateApiComponentBase {

    @Resource
    private DataWarehouseDataSourceMapper dataSourceMapper;


    @Resource
    private DataWarehouseDataSourceSchemaMapper dataSourceSchemaMapper;

    @Resource
    private FileMapper fileMapper;

    @Override
    public String getToken() {
        return "datawarehouse/datasource/import";
    }

    @Override
    public String getName() {
        return "导入数据仓库数据源";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "id", type = ApiParamType.LONG, desc = "id，存在代表需要替换"),
            @Param(name = "name", type = ApiParamType.REGEX, desc = "唯一标识", rule = "^[A-Za-z_]+$", maxLength = 50, isRequired = true, xss = true),
            @Param(name = "label", type = ApiParamType.STRING, desc = "名称", maxLength = 50, isRequired = true, xss = true),
            @Param(name = "fileId", type = ApiParamType.LONG, desc = "配置文件id", isRequired = true)})
    @Description(desc = "导入数据仓库数据源接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        DataSourceVo newDataSourceVo = JSONObject.toJavaObject(jsonObj, DataSourceVo.class);
        if (dataSourceMapper.checkDataSourceNameIsExists(newDataSourceVo) > 0) {
            throw new DataSourceNameIsExistsException(newDataSourceVo.getName());
        }
        FileVo fileVo = fileMapper.getFileById(newDataSourceVo.getFileId());
        if (fileVo == null) {
            throw new DataSourceFileIsNotFoundException();
        }
        String xml = IOUtils.toString(FileUtil.getData(fileVo.getPath()), StandardCharsets.UTF_8);
        DataSourceVo dataSourceVo = ReportXmlUtil.generateDataSourceFromXml(xml);
        newDataSourceVo.setXml(xml);
        newDataSourceVo.setIsActive(0);
        newDataSourceVo.setDataCount(0);

        //reportDataSourceVo.setConditionList(dataSourceVo.getConditionList());
        Long id = jsonObj.getLong("id");
        if (id == null) {
            dataSourceMapper.insertDataSource(newDataSourceVo);
        } else {
            DataSourceVo oldDatasourceVo = dataSourceMapper.getDataSourceById(id);
            //比较新老数据，找出需要新增、修改和删除的属性，这样做的目的是为了保留条件配置
            if (oldDatasourceVo == null) {
                throw new DataSourceIsNotFoundException(id);
            }
            List<DataSourceFieldVo> deleteList = oldDatasourceVo.getFieldList().stream().filter(d -> !dataSourceVo.getFieldList().contains(d)).collect(Collectors.toList());
            List<DataSourceFieldVo> updateList = dataSourceVo.getFieldList().stream().filter(d -> oldDatasourceVo.getFieldList().contains(d)).collect(Collectors.toList());
            List<DataSourceFieldVo> insertList = dataSourceVo.getFieldList().stream().filter(d -> !oldDatasourceVo.getFieldList().contains(d)).collect(Collectors.toList());
            //用回旧的fieldId
            if (CollectionUtils.isNotEmpty(updateList)) {
                for (DataSourceFieldVo field : updateList) {
                    Optional<DataSourceFieldVo> op = oldDatasourceVo.getFieldList().stream().filter(d -> d.equals(field)).findFirst();
                    op.ifPresent(dataSourceFieldVo -> field.setId(dataSourceFieldVo.getId()));
                }
            }
            newDataSourceVo.setFieldList(null);//清空旧数据
            newDataSourceVo.addField(insertList);
            newDataSourceVo.addField(updateList);
            //FIXME 检查数据源是否被使用
            dataSourceMapper.updateDataSource(newDataSourceVo);

            if (CollectionUtils.isNotEmpty(deleteList)) {
                for (DataSourceFieldVo field : deleteList) {
                    dataSourceMapper.deleteDataSourceFieldById(field.getId());
                }
            }

            if (CollectionUtils.isNotEmpty(updateList)) {
                for (DataSourceFieldVo field : updateList) {
                    dataSourceMapper.updateDataSourceField(field);
                }
            }

            if (CollectionUtils.isNotEmpty(insertList)) {
                for (DataSourceFieldVo field : insertList) {
                    field.setDataSourceId(newDataSourceVo.getId());
                    dataSourceMapper.insertDataSourceField(field);
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

        return null;
    }

}

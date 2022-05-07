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
import codedriver.framework.datawarehouse.dto.ReportDataSourceConditionVo;
import codedriver.framework.datawarehouse.dto.ReportDataSourceFieldVo;
import codedriver.framework.datawarehouse.dto.ReportDataSourceVo;
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

@Service
@AuthAction(action = DATA_WAREHOUSE_MODIFY.class)
@OperationType(type = OperationTypeEnum.CREATE)
@Transactional
public class ImportReportDataSourceApi extends PrivateApiComponentBase {

    @Resource
    private DataWarehouseDataSourceMapper reportDataSourceMapper;


    @Resource
    private DataWarehouseDataSourceSchemaMapper reportDataSourceSchemaMapper;

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
        ReportDataSourceVo reportDataSourceVo = JSONObject.toJavaObject(jsonObj, ReportDataSourceVo.class);
        if (reportDataSourceMapper.checkDataSourceNameIsExists(reportDataSourceVo) > 0) {
            throw new DataSourceNameIsExistsException(reportDataSourceVo.getName());
        }
        FileVo fileVo = fileMapper.getFileById(reportDataSourceVo.getFileId());
        if (fileVo == null) {
            throw new DataSourceFileIsNotFoundException();
        }
        String xml = IOUtils.toString(FileUtil.getData(fileVo.getPath()), StandardCharsets.UTF_8);
        ReportDataSourceVo dataSourceVo = ReportXmlUtil.generateDataSourceFromXml(xml);
        reportDataSourceVo.setXml(xml);
        reportDataSourceVo.setIsActive(0);
        reportDataSourceVo.setDataCount(0);
        reportDataSourceVo.setFieldList(dataSourceVo.getFieldList());
        reportDataSourceVo.setConditionList(dataSourceVo.getConditionList());
        Long id = jsonObj.getLong("id");
        if (id == null) {
            reportDataSourceMapper.insertReportDataSource(reportDataSourceVo);
        } else {
            if (reportDataSourceMapper.getReportDataSourceById(id) == null) {
                throw new DataSourceIsNotFoundException(id);
            }
            //FIXME 检查数据源是否被使用
            reportDataSourceMapper.updateReportDataSource(reportDataSourceVo);
            reportDataSourceMapper.deleteReportDataSourceFieldByDataSourceId(reportDataSourceVo.getId());
            reportDataSourceMapper.deleteReportDataSourceConditionByDataSourceId(reportDataSourceVo.getId());
        }
        if (CollectionUtils.isNotEmpty(reportDataSourceVo.getFieldList())) {
            for (ReportDataSourceFieldVo field : reportDataSourceVo.getFieldList()) {
                field.setDataSourceId(reportDataSourceVo.getId());
                reportDataSourceMapper.insertReportDataSourceField(field);
            }
        }
        if (CollectionUtils.isNotEmpty(reportDataSourceVo.getConditionList())) {
            for (ReportDataSourceConditionVo condition : reportDataSourceVo.getConditionList()) {
                condition.setDataSourceId(reportDataSourceVo.getId());
                reportDataSourceMapper.insertReportDataSourceCondition(condition);
            }
        }
        //由于以下操作是DDL操作，所以需要使用EscapeTransactionJob避开当前事务，否则在进行DDL操作之前事务就会提交，如果DDL出错，则上面的事务就无法回滚了
        EscapeTransactionJob.State s = new EscapeTransactionJob(() -> {
            reportDataSourceSchemaMapper.deleteDataSourceTable(reportDataSourceVo);
            reportDataSourceSchemaMapper.createDataSourceTable(reportDataSourceVo);
        }).execute();
        if (!s.isSucceed()) {
            throw new CreateDataSourceSchemaException(reportDataSourceVo);
        }

        return null;
    }

}

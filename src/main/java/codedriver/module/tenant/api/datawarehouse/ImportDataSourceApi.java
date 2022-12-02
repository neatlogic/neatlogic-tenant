/*
 * Copyright(c) 2021. TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.datawarehouse;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.auth.label.DATA_WAREHOUSE_MODIFY;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.datawarehouse.dao.mapper.DataWarehouseDataSourceMapper;
import codedriver.framework.datawarehouse.dto.DataSourceFieldVo;
import codedriver.framework.datawarehouse.dto.DataSourceVo;
import codedriver.framework.datawarehouse.service.DataSourceService;
import codedriver.framework.datawarehouse.utils.ReportXmlUtil;
import codedriver.framework.exception.file.FileExtNotAllowedException;
import codedriver.framework.exception.file.FileNotUploadException;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateBinaryStreamApiComponentBase;
import codedriver.framework.transaction.util.TransactionUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.DocumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

@Service
@AuthAction(action = DATA_WAREHOUSE_MODIFY.class)
@OperationType(type = OperationTypeEnum.OPERATE)
public class ImportDataSourceApi extends PrivateBinaryStreamApiComponentBase {
    static Logger logger = LoggerFactory.getLogger(ImportDataSourceApi.class);
    @Resource
    DataWarehouseDataSourceMapper dataSourceMapper;

    @Resource
    DataSourceService dataSourceService;

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

    @Input({
    })
    @Output({
            @Param(name = "successCount", type = ApiParamType.INTEGER, desc = "导入成功数量"),
            @Param(name = "failureCount", type = ApiParamType.INTEGER, desc = "导入失败数量"),
            @Param(name = "failureReasonList", type = ApiParamType.JSONARRAY, desc = "失败原因")
    })
    @Description(desc = "导入数据仓库数据源")
    @Override
    public Object myDoService(JSONObject paramObj, HttpServletRequest request, HttpServletResponse response) throws Exception {
        JSONObject resultObj = new JSONObject();
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        Map<String, MultipartFile> multipartFileMap = multipartRequest.getFileMap();
        if (multipartFileMap.isEmpty()) {
            throw new FileNotUploadException();
        }
        JSONArray resultList = new JSONArray();
        byte[] buf = new byte[1024];
        int successCount = 0;
        int failureCount = 0;
        for (Map.Entry<String, MultipartFile> entry : multipartFileMap.entrySet()) {
            MultipartFile multipartFile = entry.getValue();
            try (ZipInputStream zis = new ZipInputStream(multipartFile.getInputStream());
                 ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                while (zis.getNextEntry() != null) {
                    int len;
                    while ((len = zis.read(buf)) != -1) {
                        out.write(buf, 0, len);
                    }
                    DataSourceVo dataSourceVo = JSONObject.parseObject(new String(out.toByteArray(), StandardCharsets.UTF_8), new TypeReference<DataSourceVo>() {
                    });
                    JSONObject result = null;
                    TransactionStatus tx = TransactionUtil.openTx();
                    try {
                        result = save(dataSourceVo);
                        TransactionUtil.commitTx(tx);
                    } catch (Exception ex) {
                        logger.error(ex.getMessage(),ex);
                        TransactionUtil.rollbackTx(tx);
                        throw new Exception(ex.getMessage(),ex);
                    }
                    if (MapUtils.isNotEmpty(result)) {
                        resultList.add(result);
                        failureCount++;
                    } else {
                        successCount++;
                    }
                    out.reset();
                }
            } catch (Exception e) {
                throw new FileExtNotAllowedException(multipartFile.getOriginalFilename());
            }
        }
        resultObj.put("successCount", successCount);
        resultObj.put("failureCount", failureCount);
        if (CollectionUtils.isNotEmpty(resultList)) {
            resultObj.put("failureReasonList", resultList);
        }
        return resultObj;
    }

    private JSONObject save(DataSourceVo vo) {
        List<String> failReasonList = new ArrayList<>();
        String name = vo.getName();
        String label = vo.getLabel();
        List<DataSourceFieldVo> fieldList = vo.getFieldList();
        if (StringUtils.isBlank(name) && StringUtils.isBlank(label)) {
            return null;
        }
        if (StringUtils.isBlank(name)) {
            failReasonList.add("缺少唯一标识");
        } else if (StringUtils.isBlank(label)) {
            failReasonList.add("缺少名称");
        }
        if (StringUtils.isBlank(vo.getXml())) {
            failReasonList.add("缺少配置xml");
        }
        if (StringUtils.isBlank(vo.getMode())) {
            failReasonList.add("缺少同步模式");
        }
        if (CollectionUtils.isEmpty(failReasonList)) {
            DataSourceVo xmlConfig = null;
            try {
                xmlConfig = ReportXmlUtil.generateDataSourceFromXml(vo.getXml());
            } catch (DocumentException e) {
                failReasonList.add("配置xml格式错误");
            }
            if (xmlConfig != null) {
                vo.setDataCount(0);
                DataSourceVo oldVo = dataSourceMapper.getDataSourceDetailByName(name);
                // 还原条件设置
                List<DataSourceFieldVo> newFieldList = dataSourceService.revertFieldCondition(xmlConfig.getFieldList(), vo.getFieldList());
                if (oldVo == null) {
                    vo.setId(null);
                    if (vo.getIsActive() == null) {
                        vo.setIsActive(1);
                    }
                    if (CollectionUtils.isNotEmpty(newFieldList)) {
                        vo.setFieldList(newFieldList);
                    }
                    dataSourceService.insertDataSource(vo);
                } else {
                    vo.setId(oldVo.getId());
                    if (vo.getIsActive() == null) {
                        vo.setIsActive(oldVo.getIsActive());
                    }
                    if (CollectionUtils.isNotEmpty(newFieldList)) {
                        xmlConfig.setFieldList(newFieldList);
                    }
                    dataSourceService.updateDataSource(vo, xmlConfig, oldVo);
                }
                dataSourceService.createDataSourceSchema(vo);
                dataSourceService.loadOrUnloadReportDataSourceJob(vo);
            }
        }
        if (CollectionUtils.isNotEmpty(failReasonList)) {
            JSONObject result = new JSONObject();
            result.put("item", "导入：" + (StringUtils.isNotBlank(name) ? name : label) + "时出现如下问题：");
            result.put("list", failReasonList);
            return result;
        }
        return null;
    }

}
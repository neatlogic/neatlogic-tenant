/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package neatlogic.module.tenant.api.integration;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.exception.file.FileExtNotAllowedException;
import neatlogic.framework.exception.file.FileNotUploadException;
import neatlogic.framework.integration.authentication.enums.HttpMethod;
import neatlogic.framework.integration.core.IIntegrationHandler;
import neatlogic.framework.integration.core.IntegrationHandlerFactory;
import neatlogic.framework.integration.dao.mapper.IntegrationMapper;
import neatlogic.framework.integration.dto.IntegrationVo;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateBinaryStreamApiComponentBase;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.ZipInputStream;

@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class IntegrationImportApi extends PrivateBinaryStreamApiComponentBase {

    static final Pattern urlPattern = Pattern.compile("^((http|ftp|https)://)(([a-zA-Z0-9\\._-]+)|([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}))(:[0-9]{1,4})*(/[a-zA-Z0-9\\&%_\\./-~-]*)?");

    @Resource
    private IntegrationMapper integrationMapper;

    @Override
    public String getToken() {
        return "integration/import";
    }

    @Override
    public String getName() {
        return "导入集成设置信息";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Output({
            @Param(name = "successCount", type = ApiParamType.INTEGER, desc = "导入成功数量"),
            @Param(name = "failureCount", type = ApiParamType.INTEGER, desc = "导入失败数量"),
            @Param(name = "failureReasonList", type = ApiParamType.JSONARRAY, desc = "失败原因")
    })
    @Description(desc = "导入集成设置信息")
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
                    IntegrationVo integrationVo = JSONObject.parseObject(new String(out.toByteArray(), StandardCharsets.UTF_8), new TypeReference<IntegrationVo>() {
                    });
                    JSONObject result = save(integrationVo);
                    if (MapUtils.isNotEmpty(result)) {
                        resultList.add(result);
                        failureCount++;
                    } else {
                        successCount++;
                    }
                    out.reset();
                }
            } catch (IOException e) {
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

    private JSONObject save(IntegrationVo integrationVo) {
        List<String> failReasonList = new ArrayList<>();
        String name = integrationVo.getName();
        if (StringUtils.isBlank(name)) {
            failReasonList.add("名称不能为空");
        }
        if (StringUtils.isBlank(integrationVo.getHandler())) {
            failReasonList.add("集成配置处理器不能为空");
        }
        if (StringUtils.isBlank(integrationVo.getMethod())) {
            failReasonList.add("请求方式不能为空");
        }
        if (StringUtils.isBlank(integrationVo.getUrl())) {
            failReasonList.add("url不能为空");
        }
        if (CollectionUtils.isEmpty(failReasonList)) {
            IntegrationVo old = integrationMapper.getIntegrationByUuid(integrationVo.getUuid());
            int index = 0;
            while (integrationMapper.checkNameIsRepeats(integrationVo) > 0) {
                index++;
                integrationVo.setName(name + "_" + index);
            }
            IIntegrationHandler handler = IntegrationHandlerFactory.getHandler(integrationVo.getHandler());
            if (handler == null) {
                failReasonList.add("不存在的集成配置处理器：" + integrationVo.getHandler());
            }
            if (HttpMethod.getHttpMethod(integrationVo.getMethod()) == null) {
                failReasonList.add("不存在的请求方式：" + integrationVo.getMethod());
            }
            if (!urlPattern.matcher(integrationVo.getUrl()).matches()) {
                failReasonList.add("url不符合格式要求");
            }
            if (integrationVo.getUrl().contains("integration/run/")) {
                failReasonList.add("url可能是集成调用地址，不允许配置");
            }
            if (CollectionUtils.isEmpty(failReasonList)) {
                if (old == null) {
                    integrationMapper.insertIntegration(integrationVo);
                } else {
                    integrationMapper.updateIntegration(integrationVo);
                }
            } else {
                JSONObject result = new JSONObject();
                result.put("item", "导入：" + name + "时出现如下问题：");
                result.put("list", failReasonList);
                return result;
            }
        } else {
            JSONObject result = new JSONObject();
            result.put("item", "导入：" + name + "时出现如下问题：");
            result.put("list", failReasonList);
            return result;
        }
        return null;
    }

}

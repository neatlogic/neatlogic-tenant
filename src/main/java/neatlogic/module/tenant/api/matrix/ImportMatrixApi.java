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

package neatlogic.module.tenant.api.matrix;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dto.FieldValidResultVo;
import neatlogic.framework.exception.file.FileExtNotAllowedException;
import neatlogic.framework.exception.file.FileNotUploadException;
import neatlogic.framework.matrix.core.IMatrixDataSourceHandler;
import neatlogic.framework.matrix.core.MatrixDataSourceHandlerFactory;
import neatlogic.framework.matrix.dao.mapper.MatrixMapper;
import neatlogic.framework.matrix.dto.MatrixVo;
import neatlogic.framework.matrix.exception.MatrixDataSourceHandlerNotFoundException;
import neatlogic.framework.matrix.exception.MatrixLabelRepeatException;
import neatlogic.framework.matrix.exception.MatrixNameRepeatException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.IValid;
import neatlogic.framework.restful.core.privateapi.PrivateBinaryStreamApiComponentBase;
import neatlogic.framework.util.UuidUtil;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.zip.ZipInputStream;

@Component
@Transactional
@OperationType(type = OperationTypeEnum.UPDATE)
public class ImportMatrixApi extends PrivateBinaryStreamApiComponentBase {

    private final Logger logger = LoggerFactory.getLogger(ImportMatrixApi.class);

    @Resource
    private MatrixMapper matrixMapper;


    @Override
    public String getName() {
        return "nmtam.importmatrixapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "name", type = ApiParamType.STRING, minLength = 1, desc = "common.name"),
            @Param(name = "label", type = ApiParamType.STRING, minLength = 1, desc = "common.tag"),
            @Param(name = "isOverride", type = ApiParamType.ENUM, rule = "1", desc = "common.isoverride")
    })
    @Output({
            @Param(name = "uuidDuplication", type = ApiParamType.ENUM, rule = "0,1", desc = "nmtam.importmatrixapi.output.param.desc.uuidduplication"),
            @Param(name = "nameDuplication", type = ApiParamType.ENUM, rule = "0,1", desc = "nmtam.importmatrixapi.output.param.desc.nameduplication"),
            @Param(name = "labelDuplication", type = ApiParamType.ENUM, rule = "0,1", desc = "nmtam.importmatrixapi.output.param.desc.labelduplication"),
            @Param(name = "uuid", type = ApiParamType.STRING, desc = "common.uuid"),
            @Param(name = "name", type = ApiParamType.STRING, desc = "common.name"),
            @Param(name = "label", type = ApiParamType.STRING, desc = "common.tag"),
    })
    @Description(desc = "nmtam.importmatrixapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj, HttpServletRequest request, HttpServletResponse response) throws Exception {
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        // 获取所有导入文件
        Map<String, MultipartFile> multipartFileMap = multipartRequest.getFileMap();
        // 如果没有导入文件，抛出异常
        if (multipartFileMap.isEmpty()) {
            throw new FileNotUploadException();
        }
        String name = paramObj.getString("name");
        String label = paramObj.getString("label");
        Integer isOverride = paramObj.getInteger("isOverride");
        JSONObject resultObj = new JSONObject();
        byte[] buf = new byte[1024];
        for (Map.Entry<String, MultipartFile> entry : multipartFileMap.entrySet()) {
            MultipartFile multipartFile = entry.getValue();
            //
            try (ZipInputStream zipis = new ZipInputStream(multipartFile.getInputStream());
                ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                while (zipis.getNextEntry() != null) {
                    int len;
                    while ((len = zipis.read(buf)) != -1) {
                        out.write(buf, 0, len);
                    }
                    MatrixVo matrixDefinition = JSONObject.parseObject(new String(out.toByteArray(), StandardCharsets.UTF_8), MatrixVo.class);
                    IMatrixDataSourceHandler matrixDataSourceHandler = MatrixDataSourceHandlerFactory.getHandler(matrixDefinition.getType());
                    if (matrixDataSourceHandler == null) {
                        throw new MatrixDataSourceHandlerNotFoundException(matrixDefinition.getType());
                    }
                    String uuid = matrixDefinition.getUuid();
                    if (!Objects.equals(isOverride, 1)) {
                        if (matrixMapper.checkMatrixIsExists(uuid) > 0) {
                            resultObj.put("uuidDuplication", 1);
                        }
                    }
                    if (StringUtils.isNotBlank(name)) {
                        // 用新的name值
                        matrixDefinition.setName(name);
                    }
                    // 验证name是否重复
                    if (matrixMapper.checkMatrixNameIsRepeat(matrixDefinition) > 0) {
                        resultObj.put("nameDuplication", 1);
                        resultObj.put("uuid", uuid);
                        resultObj.put("name", matrixDefinition.getName());
                    }
                    if (StringUtils.isNotBlank(label)) {
                        // 用新的label值
                        matrixDefinition.setLabel(label);
                    }
                    // 验证label是否重复
                    if (matrixMapper.checkMatrixLabelIsRepeat(matrixDefinition) > 0) {
                        resultObj.put("labelDuplication", 1);
                        resultObj.put("uuid", uuid);
                        resultObj.put("label", matrixDefinition.getLabel());
                    }
                    if (MapUtils.isNotEmpty(resultObj)) {
                        return resultObj;
                    }
                    matrixDataSourceHandler.importMatrix(matrixDefinition);
                    out.reset();
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                throw new FileExtNotAllowedException(multipartFile.getOriginalFilename());
            }
        }
        return resultObj;
    }

    @Override
    public String getToken() {
        return "matrix/import";
    }

    public IValid name() {
        return value -> {
            MatrixVo matrixVo = JSONObject.toJavaObject(value, MatrixVo.class);
            if (StringUtils.isBlank(matrixVo.getUuid())) {
                matrixVo.setUuid(UuidUtil.randomUuid());
            }
            if (matrixMapper.checkMatrixNameIsRepeat(matrixVo) > 0) {
                return new FieldValidResultVo(new MatrixNameRepeatException(matrixVo.getName()));
            }
            return new FieldValidResultVo();
        };
    }

    public IValid label() {
        return value -> {
            MatrixVo matrixVo = JSONObject.toJavaObject(value, MatrixVo.class);
            if (StringUtils.isBlank(matrixVo.getUuid())) {
                matrixVo.setUuid(UuidUtil.randomUuid());
            }
            if (matrixMapper.checkMatrixLabelIsRepeat(matrixVo) > 0) {
                return new FieldValidResultVo(new MatrixLabelRepeatException(matrixVo.getLabel()));
            }
            return new FieldValidResultVo();
        };
    }
}

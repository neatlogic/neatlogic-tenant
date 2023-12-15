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

package neatlogic.module.tenant.api.importexport;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.exception.type.ParamIrregularException;
import neatlogic.framework.importexport.core.ImportExportHandler;
import neatlogic.framework.importexport.core.ImportExportHandlerFactory;
import neatlogic.framework.importexport.dto.ImportExportBaseInfoVo;
import neatlogic.framework.importexport.dto.ImportExportVo;
import neatlogic.framework.importexport.exception.ExportNoAuthException;
import neatlogic.framework.importexport.exception.ImportExportHandlerNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateBinaryStreamApiComponentBase;
import neatlogic.framework.util.FileUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
@OperationType(type = OperationTypeEnum.UPDATE)
public class ExportApi extends PrivateBinaryStreamApiComponentBase {

    private Logger logger = LoggerFactory.getLogger(ExportApi.class);

    @Override
    public String getName() {
        return "nmtai.exportapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "primaryKey", type = ApiParamType.NOAUTH, isRequired = true, desc = "common.primarykey"),
            @Param(name = "type", type = ApiParamType.STRING, isRequired = true, desc = "common.type")
    })
    @Output({})
    @Description(desc = "nmtai.exportapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj, HttpServletRequest request, HttpServletResponse response) throws Exception {
        Object primaryKey = paramObj.get("primaryKey");
        String type = paramObj.getString("type");
        ImportExportHandler importExportHandler = ImportExportHandlerFactory.getHandler(type);
        if (importExportHandler == null) {
            throw new ImportExportHandlerNotFoundException(type);
        }
        if (primaryKey instanceof String) {
            String str = (String) primaryKey;
            if (StringUtils.length(str) != 32) {
                try {
                    primaryKey = Long.valueOf(str);
                } catch (NumberFormatException e) {
                    throw new ParamIrregularException("primaryKey");
                }
            }
        }
        if (!importExportHandler.checkExportAuth(primaryKey)) {
            throw new ExportNoAuthException();
        }

        String fileName = null;
        // 先检查导出对象及依赖对象有没有找不到数据，如果有就抛异常
        {
            List<ImportExportBaseInfoVo> dependencyBaseInfoList = new ArrayList<>();
            dependencyBaseInfoList.add(new ImportExportBaseInfoVo(type, primaryKey));
            ImportExportVo importExportVo = importExportHandler.exportData(primaryKey, dependencyBaseInfoList, null);
            fileName = importExportHandler.getType().getText() + "-" + importExportVo.getName() + "(" + importExportVo.getPrimaryKey() + ").pak";
        }
        // 上面代码检查没有异常再进行导出压缩到文件
        response.setContentType("application/vnd.ms-excel;charset=utf-8");
        fileName = FileUtil.getEncodedFileName(fileName);
        response.setHeader("Content-Disposition", " attachment; filename=\"" + fileName + "\"");
        List<ImportExportBaseInfoVo> dependencyBaseInfoList = new ArrayList<>();
        try (ZipOutputStream zipos = new ZipOutputStream(response.getOutputStream())) {
            dependencyBaseInfoList.add(new ImportExportBaseInfoVo(type, primaryKey));
            ImportExportVo importExportVo = importExportHandler.exportData(primaryKey, dependencyBaseInfoList, zipos);
            dependencyBaseInfoList.remove(0);
            importExportVo.setDependencyBaseInfoList(dependencyBaseInfoList);
            zipos.putNextEntry(new ZipEntry(importExportVo.getPrimaryKey() + ".json"));
            zipos.write(JSONObject.toJSONBytes(importExportVo));
            zipos.closeEntry();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw e;
        }
        return null;
    }

    @Override
    public String getToken() {
        return "common/export";
    }
}

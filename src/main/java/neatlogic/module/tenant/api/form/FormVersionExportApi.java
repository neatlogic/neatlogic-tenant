/*
 *
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 */

package neatlogic.module.tenant.api.form;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.NoAuth;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.form.dao.mapper.FormMapper;
import neatlogic.framework.form.dto.FormVersionVo;
import neatlogic.framework.form.dto.FormVo;
import neatlogic.framework.form.exception.FormNotFoundException;
import neatlogic.framework.form.exception.FormVersionNotFoundException;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateBinaryStreamApiComponentBase;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@AuthAction(action = NoAuth.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class FormVersionExportApi extends PrivateBinaryStreamApiComponentBase {

    @Autowired
    private FormMapper formMapper;

    @Override
    public String getToken() {
        return "form/version/export";
    }

    @Override
    public String getName() {
        return "nmtaf.formversionexportapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "formVersionUuid", type = ApiParamType.STRING, desc = "term.framework.formversionuuid", isRequired = true)
    })
    @Description(desc = "nmtaf.formversionexportapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj, HttpServletRequest request, HttpServletResponse response) throws Exception {
        String formVersionUuid = paramObj.getString("formVersionUuid");
        //判断要导出的表单版本是否存在
        FormVersionVo formVersion = formMapper.getFormVersionByUuid(formVersionUuid);
        if (formVersion == null) {
            throw new FormVersionNotFoundException(formVersionUuid);
        }
        FormVo formVo = formMapper.getFormByUuid(formVersion.getFormUuid());
        //判断表单是否存在
        if (formVo == null) {
            throw new FormNotFoundException(formVersion.getFormUuid());
        }
        formVersion.setFormName(formVo.getName());
        //设置导出文件名, 表单名称_版本号
        String fileNameEncode = formVersion.getFormName() + "_" + formVersion.getVersion() + ".formversion";
        Boolean flag = request.getHeader("User-Agent").indexOf("Gecko") > 0;
        if (request.getHeader("User-Agent").toLowerCase().indexOf("msie") > 0 || flag) {
            fileNameEncode = URLEncoder.encode(fileNameEncode, "UTF-8");// IE浏览器
        } else {
            fileNameEncode = new String(fileNameEncode.replace(" ", "").getBytes(StandardCharsets.UTF_8), "ISO8859-1");
        }
        response.setContentType("application/x-msdownload");
        response.setHeader("Content-Disposition", " attachment; filename=\"" + fileNameEncode + "\"");
        //获取序列化字节数组
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(formVersion);
        ServletOutputStream os = null;
        os = response.getOutputStream();
        IOUtils.write(baos.toByteArray(), os);
        if (os != null) {
            os.flush();
            os.close();
        }
        if (oos != null) {
            oos.close();
        }
        if (baos != null) {
            baos.close();
        }

        return null;
    }

}

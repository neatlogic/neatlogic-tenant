/*Copyright (C) $today.year  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

package neatlogic.module.tenant.api;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.common.config.Config;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateBinaryStreamApiComponentBase;
import neatlogic.framework.util.TranslateUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class DownloadI18nTranslatePropertiesApi extends PrivateBinaryStreamApiComponentBase {
    Logger logger = LoggerFactory.getLogger(DownloadI18nTranslatePropertiesApi.class);

    @Override
    public String getToken() {
        return "i18n/translate/properties/download";
    }

    @Override
    public String getName() {
        return "获取翻译properties文件";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "language", type = ApiParamType.STRING, isRequired = true, desc = "需要从中文转换的语言，例如：en")
    })
    @Description(desc = "获取翻译properties文件")
    @Override
    public Object myDoService(JSONObject paramObj, HttpServletRequest request, HttpServletResponse response) throws Exception {
        String lan = paramObj.getString("language");
        try (ServletOutputStream os = response.getOutputStream();
             InputStream is = Config.class.getClassLoader().getResourceAsStream("i18n/message_zh.properties");
             InputStreamReader inputStreamReader = new InputStreamReader(is);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
            response.setContentType("application/stream");
            response.setHeader("Content-Disposition", " attachment; filename=message_" + lan + ".properties");
            String line;
            Map<String, String> keyValueMap = new LinkedHashMap<String, String>();
            int index = 0;
            StringBuilder valueSb = new StringBuilder();
            StringBuilder outPropertiesSb = new StringBuilder();
            while ((line = bufferedReader.readLine()) != null) {
                if (StringUtils.isNotBlank(line) && line.contains("=")) {
                    String[] pros = line.trim().split("=");
                    String value;
                    if (pros.length == 2) {
                        value = pros[1];
                    } else {
                        value = StringUtils.EMPTY;
                    }
                    keyValueMap.put(pros[0], value);
                    if (index != 0) {
                        valueSb.append("\n");
                    }
                    valueSb.append(value);
                    index++;
                } else {
                    outPutTranslate(valueSb, lan, keyValueMap, outPropertiesSb, os);
                    index = 0;
                    IOUtils.copyLarge(IOUtils.toInputStream(line + System.lineSeparator(), StandardCharsets.UTF_8), os);
                    os.flush();
                }
                if (index == 100) {
                    outPutTranslate(valueSb, lan, keyValueMap, outPropertiesSb, os);
                    index = 0;
                }
            }
            if (index != 0) {
                outPutTranslate(valueSb, lan, keyValueMap, outPropertiesSb, os);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    private void outPutTranslate(StringBuilder valueSb, String lan, Map<String, String> keyValueMap, StringBuilder outPropertiesSb, ServletOutputStream os) throws IOException {
        if (MapUtils.isEmpty(keyValueMap)) {
            return;
        }
        JSONArray result = TranslateUtil.getBatchTransResult(valueSb.toString(), Locale.CHINESE.getLanguage(), lan);
        Map<String, String> zh2lanMap = new HashMap<>();
        zh2lanMap.put(StringUtils.EMPTY, StringUtils.EMPTY);
        if (CollectionUtils.isNotEmpty(result)) {
            result.forEach(o -> {
                String lanVal = ((JSONObject) o).getString("dst");
                String zhVal = ((JSONObject) o).getString("src");
                if (StringUtils.isNotBlank(zhVal)) {
                    zh2lanMap.put(zhVal, lanVal.replaceAll("DATA. ","DATA."));
                }
            });
        }
        for (Map.Entry<String, String> entry : keyValueMap.entrySet()) {
            outPropertiesSb.append(entry.getKey()).append("=").append(zh2lanMap.get(entry.getValue())).append(System.lineSeparator());
        }
        IOUtils.copyLarge(IOUtils.toInputStream(outPropertiesSb, StandardCharsets.UTF_8), os);
        os.flush();
        outPropertiesSb.delete(0, outPropertiesSb.length());
        valueSb.delete(0, valueSb.length());
        zh2lanMap.clear();
        keyValueMap.clear();
    }
}

/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.apimanage;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.auth.label.INTERFACE_MODIFY;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.exception.type.ApiNotFoundException;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.IApiComponent;
import codedriver.framework.restful.core.IBinaryStreamApiComponent;
import codedriver.framework.restful.core.IJsonStreamApiComponent;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentFactory;
import codedriver.framework.restful.core.privateapi.PrivateBinaryStreamApiComponentBase;
import codedriver.framework.restful.dao.mapper.ApiMapper;
import codedriver.framework.restful.dto.ApiVo;
import codedriver.framework.restful.enums.ApiType;
import codedriver.framework.util.FileUtil;
import codedriver.framework.util.pdf.FontVo;
import codedriver.framework.util.pdf.PDFBuilder;
import codedriver.framework.util.pdf.ParagraphVo;
import codedriver.framework.util.pdf.TableVo;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@AuthAction(action = INTERFACE_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ApiHelpExportApi extends PrivateBinaryStreamApiComponentBase {
    static Logger logger = LoggerFactory.getLogger(ApiHelpExportApi.class);

    @Autowired
    private ApiMapper apiMapper;

    @Override
    public String getToken() {
        return "api/help/export";
    }

    @Override
    public String getName() {
        return "导出接口帮助";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "tokenList", type = ApiParamType.JSONARRAY, desc = "接口token 列表")
    })
    @Output({
            @Param(name = "Return", explode = ApiVo.class, isRequired = true, desc = "接口配置信息")
    })
    @Description(desc = "导出接口帮助接口")
    @Override
    public Object myDoService(JSONObject jsonObj, HttpServletRequest request, HttpServletResponse response) throws Exception {
        JSONArray tokenArray = jsonObj.getJSONArray("tokenList");
        JSONArray apiHelpJsonArray = new JSONArray();
        if (CollectionUtils.isNotEmpty(tokenArray)) {
            for (int t = 0; t < tokenArray.size(); t++) {
                String token = tokenArray.getString(t);
                ApiVo interfaceVo = PrivateApiComponentFactory.getApiByToken(token);
                if (interfaceVo == null) {
                    interfaceVo = apiMapper.getApiByToken(token);
                    if (interfaceVo == null || !interfaceVo.getIsActive().equals(1)) {
                        throw new ApiNotFoundException(token);
                    }
                }
                if (interfaceVo.getType().equals(ApiType.OBJECT.getValue())) {
                    IApiComponent restComponent = PrivateApiComponentFactory.getInstance(interfaceVo.getHandler());
                    JSONObject helpJson = restComponent.help();
                    helpJson.put("token", interfaceVo.getToken());
                    helpJson.put("name", interfaceVo.getName());
                    apiHelpJsonArray.add(helpJson);
                } else if (interfaceVo.getType().equals(ApiType.STREAM.getValue())) {
                    IJsonStreamApiComponent restComponent = PrivateApiComponentFactory.getStreamInstance(interfaceVo.getHandler());
                    JSONObject helpJson = restComponent.help();
                    helpJson.put("token", interfaceVo.getToken());
                    helpJson.put("name", interfaceVo.getName());
                    apiHelpJsonArray.add(helpJson);
                } else if (interfaceVo.getType().equals(ApiType.BINARY.getValue())) {
                    IBinaryStreamApiComponent restComponent = PrivateApiComponentFactory.getBinaryInstance(interfaceVo.getHandler());
                    JSONObject helpJson = restComponent.help();
                    helpJson.put("token", interfaceVo.getToken());
                    helpJson.put("name", interfaceVo.getName());
                    apiHelpJsonArray.add(helpJson);
                }
            }
        } else {
            for (Map.Entry<String, IApiComponent> entry : PrivateApiComponentFactory.getComponentMap().entrySet()) {
                JSONObject helpJson = entry.getValue().help();
                helpJson.put("token", entry.getKey());
                helpJson.put("name", entry.getValue().getName());
                apiHelpJsonArray.add(helpJson);
            }
            for (Map.Entry<String, IBinaryStreamApiComponent> entry : PrivateApiComponentFactory.getBinaryStreamComponentMap().entrySet()) {
                JSONObject helpJson = entry.getValue().help();
                helpJson.put("token", entry.getKey());
                helpJson.put("name", entry.getValue().getName());
                apiHelpJsonArray.add(helpJson);
            }
            for (Map.Entry<String, IJsonStreamApiComponent> entry : PrivateApiComponentFactory.getJsonStreamComponentMap().entrySet()) {
                JSONObject helpJson = entry.getValue().help();
                helpJson.put("token", entry.getKey());
                helpJson.put("name", entry.getValue().getName());
                apiHelpJsonArray.add(helpJson);
            }
        }
        if (CollectionUtils.isEmpty(apiHelpJsonArray)) {
            return null;
        }
        response.setContentType("application/vnd.ms-excel;charset=utf-8");
        response.setHeader("Content-Disposition", "attachment; filename=" + FileUtil.getEncodedFileName("codedriver接口帮助.pdf") + "\"");
        OutputStream os = response.getOutputStream();
        try {
            PDFBuilder pdfBuilder = new PDFBuilder();
            PdfWriter.getInstance(pdfBuilder.builder(), os);
            //自定义字体
            FontVo boldFontVo = new FontVo(8, true);
            FontVo normalFontVo = new FontVo(8);

            //创建文档、设置页面设置、打开文档
            PDFBuilder.Builder builder = pdfBuilder.setPageSizeVertical().setMargins(30f, 30f, 30f, 30f).open();
            //定义标题
            ParagraphVo tokenParagraphVo = new ParagraphVo("接口token", boldFontVo);
            ParagraphVo nameParagraphVo = new ParagraphVo("接口名称", boldFontVo);
            ParagraphVo descriptionParagraphVo = new ParagraphVo("描述", boldFontVo);
            ParagraphVo inputParagraphVo = new ParagraphVo("输入参数", boldFontVo);
            ParagraphVo outputParagraphVo = new ParagraphVo("输出参数", boldFontVo);
            //定义表头
            Map<Integer, ParagraphVo> headerMap = new HashMap<>();
            headerMap.put(1, new ParagraphVo("名称", normalFontVo));
            headerMap.put(2, new ParagraphVo("类型", normalFontVo));
            headerMap.put(3, new ParagraphVo("是否必填", normalFontVo));
            headerMap.put(4, new ParagraphVo("说明", normalFontVo));

            for (Object object : apiHelpJsonArray) {
                JSONObject jsonObject = (JSONObject) object;
                //接口token
                builder.addParagraph(tokenParagraphVo)
                        .addParagraph(new ParagraphVo(jsonObject.getString("token"), normalFontVo));
                //接口名称
                builder.addParagraph(nameParagraphVo)
                        .addParagraph(new ParagraphVo(jsonObject.getString("name"), normalFontVo));
                //接口描述
                if (StringUtils.isNotBlank(jsonObject.getString("description"))) {
                    builder.addParagraph(descriptionParagraphVo)
                            .addParagraph(new ParagraphVo(jsonObject.getString("description"), normalFontVo));
                }
                //输入参数
                if (CollectionUtils.isNotEmpty(jsonObject.getJSONArray("input"))) {
                    builder.addParagraph(inputParagraphVo)
                            .addTable(new TableVo(normalFontVo, 4, 16, 100.0F, headerMap, getDataList(jsonObject.getJSONArray("input"))), false);
                }
                //输出参数
                JSONArray outputArray = jsonObject.getJSONArray("output");
                if (CollectionUtils.isNotEmpty(outputArray)) {
                    builder.addParagraph(outputParagraphVo)
                            .addTable(new TableVo(normalFontVo, 4, 16, 100.0F, headerMap, getDataList(jsonObject.getJSONArray("output"))), false);
                }
                //分割线
                builder.addParagraph(new ParagraphVo("----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", normalFontVo));
            }
            builder.close();
        } catch (IOException e) {
            e.printStackTrace();
            logger.error(e.getMessage(), e);
        } finally {
            if (os != null) {
                os.flush();
                os.close();
            }
        }
        return null;
    }

    /**
     * 封装表格数据
     *
     * @param dataArray 数据array
     * @return 数据list
     * @throws DocumentException e
     * @throws IOException       e
     */
    private List<Map<Integer, ParagraphVo>> getDataList(JSONArray dataArray) throws DocumentException, IOException {
        List<Map<Integer, ParagraphVo>> returnList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(dataArray)) {
            FontVo fontVo = new FontVo(8);
            for (Object object : dataArray) {
                JSONObject jsonObject = (JSONObject) object;
                Map<Integer, ParagraphVo> dataMap = new HashMap<>();
                dataMap.put(1, new ParagraphVo(jsonObject.getString("name"), fontVo));
                dataMap.put(2, new ParagraphVo(jsonObject.getString("type"), fontVo));
                dataMap.put(3, new ParagraphVo(jsonObject.getString("isRequired"), fontVo));
                dataMap.put(4, new ParagraphVo(jsonObject.getString("description"), fontVo));
                returnList.add(dataMap);
            }
        }
        return returnList;
    }
}

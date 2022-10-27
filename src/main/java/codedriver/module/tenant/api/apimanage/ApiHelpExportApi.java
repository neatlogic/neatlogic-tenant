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
import codedriver.framework.util.pdf.PDFBuilder;
import codedriver.framework.util.pdf.ParagraphBuilder;
import codedriver.framework.util.pdf.TableBuilder;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;
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

        try {
            OutputStream os = response.getOutputStream();

            PDFBuilder pdfBuilder = new PDFBuilder();
            PdfWriter.getInstance(pdfBuilder.builder(), os);

            //粗体
            BaseFont bfChinese = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
            Font boldFont = new Font(bfChinese, 8, Font.BOLD);
            Font normalFont = new Font(bfChinese, 8, Font.NORMAL);

            PDFBuilder.Builder builder = pdfBuilder.setPageSizeVertical().setMargins(30f, 30f, 30f, 30f).open();

            //标题
            ParagraphBuilder tokenParagraphBuilder = new ParagraphBuilder();
            Paragraph tokenParagraph = tokenParagraphBuilder.setFont(boldFont).addText("接口token").setAlignment(Element.ALIGN_LEFT).setSpacingAfter(3).builder();
            ParagraphBuilder nameParagraphBuilder = new ParagraphBuilder();
            Paragraph nameParagraph = nameParagraphBuilder.setFont(boldFont).addText("接口名称").setAlignment(Element.ALIGN_LEFT).setSpacingAfter(3).builder();
            ParagraphBuilder descriptionParagraphBuilder = new ParagraphBuilder();
            Paragraph descriptionParagraph = descriptionParagraphBuilder.setFont(boldFont).addText("描述").setAlignment(Element.ALIGN_LEFT).setSpacingAfter(3).builder();
            ParagraphBuilder inputParagraphBuilder = new ParagraphBuilder();
            Paragraph inputParagraph = inputParagraphBuilder.setFont(boldFont).addText("输入参数").setAlignment(Element.ALIGN_LEFT).setSpacingAfter(3).builder();
            ParagraphBuilder outputParagraphBuilder = new ParagraphBuilder();
            Paragraph outputParagraph = outputParagraphBuilder.setFont(boldFont).addText("输出参数").setAlignment(Element.ALIGN_LEFT).setSpacingAfter(3).builder();

            //表头
            ParagraphBuilder tableNameParagraphBuilder = new ParagraphBuilder();
            Paragraph tableNameParagraph = tableNameParagraphBuilder.setFont(normalFont).addText("名称").setAlignment(Element.ALIGN_CENTER).builder();
            ParagraphBuilder tableTypeParagraphBuilder = new ParagraphBuilder();
            Paragraph tableTypeParagraph = tableTypeParagraphBuilder.setFont(normalFont).addText("类型").setAlignment(Element.ALIGN_CENTER).builder();
            ParagraphBuilder tableIsRequiredParagraphBuilder = new ParagraphBuilder();
            Paragraph tableIsRequiredParagraph = tableIsRequiredParagraphBuilder.setFont(normalFont).addText("是否必填").setAlignment(Element.ALIGN_CENTER).builder();
            ParagraphBuilder tableDescriptionParagraphBuilder = new ParagraphBuilder();
            Paragraph tableDescriptionParagraph = tableDescriptionParagraphBuilder.setFont(normalFont).addText("说明").setAlignment(Element.ALIGN_CENTER).builder();

            for (Object object : apiHelpJsonArray) {
                JSONObject jsonObject = (JSONObject) object;

                //接口token
                builder.addParagraph(tokenParagraph);
                ParagraphBuilder tokenTextParagraphBuilder = new ParagraphBuilder();
                Paragraph tokenTextParagraph = tokenTextParagraphBuilder.setFont(normalFont).addText(jsonObject.getString("token")).setAlignment(Element.ALIGN_LEFT).setSpacingAfter(3).builder();
                builder.addParagraph(tokenTextParagraph);

                //接口名称
                builder.addParagraph(nameParagraph);
                ParagraphBuilder nameTextParagraphBuilder = new ParagraphBuilder();
                Paragraph nameTextParagraph = nameTextParagraphBuilder.setFont(normalFont).addText(jsonObject.getString("name")).setAlignment(Element.ALIGN_LEFT).setSpacingAfter(3).builder();
                builder.addParagraph(nameTextParagraph);

                //接口描述
                if (StringUtils.isNotBlank(jsonObject.getString("description"))) {
                    builder.addParagraph(descriptionParagraph);
                    ParagraphBuilder descriptionTextParagraphBuilder = new ParagraphBuilder();
                    Paragraph descriptionTextParagraph = descriptionTextParagraphBuilder.setFont(normalFont).addText(jsonObject.getString("description")).setAlignment(Element.ALIGN_LEFT).setSpacingAfter(3).builder();
                    builder.addParagraph(descriptionTextParagraph);
                }

                //输入参数
                JSONArray inputArray = jsonObject.getJSONArray("input");
                if (CollectionUtils.isNotEmpty(inputArray)) {

                    builder.addParagraph(inputParagraph);
                    TableBuilder inputTableBuilder = new TableBuilder(16, 4);
                    inputTableBuilder.setHorizontalAlignment(Element.ALIGN_CENTER).setWidthPercentage(100.0F).addCell(tableNameParagraph).addCell(tableTypeParagraph).addCell(tableIsRequiredParagraph).addCell(tableDescriptionParagraph);

                    //输入参数值
                    for (Object inputObject : inputArray) {
                        JSONObject inputJSONObject = (JSONObject) inputObject;

                        ParagraphBuilder tableNameTextParagraphBuilder = new ParagraphBuilder();
                        Paragraph tableNameTextParagraph = tableNameTextParagraphBuilder.setFont(normalFont).addText(inputJSONObject.getString("name")).setAlignment(Element.ALIGN_CENTER).builder();
                        ParagraphBuilder tableTypeTextParagraphBuilder = new ParagraphBuilder();
                        Paragraph tableTypeTextParagraph = tableTypeTextParagraphBuilder.setFont(normalFont).addText(inputJSONObject.getString("type")).setAlignment(Element.ALIGN_CENTER).builder();
                        ParagraphBuilder tableIsRequiredTextParagraphBuilder = new ParagraphBuilder();
                        Paragraph tableIsRequiredTextParagraph = tableIsRequiredTextParagraphBuilder.setFont(normalFont).addText(inputJSONObject.getString("isRequired")).setAlignment(Element.ALIGN_CENTER).builder();
                        ParagraphBuilder tableDescriptionTextParagraphBuilder = new ParagraphBuilder();
                        Paragraph tableDescriptionTextParagraph = tableDescriptionTextParagraphBuilder.setFont(normalFont).addText(inputJSONObject.getString("description")).setAlignment(Element.ALIGN_CENTER).builder();

                        inputTableBuilder.addCell(tableNameTextParagraph).addCell(tableTypeTextParagraph).addCell(tableIsRequiredTextParagraph).addCell(tableDescriptionTextParagraph);
                    }

                    builder.addTable(inputTableBuilder.builder(), false);

                }

                //输出参数
                JSONArray outputArray = jsonObject.getJSONArray("output");
                if (CollectionUtils.isNotEmpty(outputArray)) {

                    builder.addParagraph(outputParagraph);
                    TableBuilder outputTableBuilder = new TableBuilder(16, 4);
                    outputTableBuilder.setHorizontalAlignment(Element.ALIGN_CENTER).setWidthPercentage(100.0F).addCell(tableNameParagraph).addCell(tableTypeParagraph).addCell(tableIsRequiredParagraph).addCell(tableDescriptionParagraph);

                    //输出参数值
                    for (Object outputObject : outputArray) {
                        JSONObject outputJSONObject = (JSONObject) outputObject;

                        ParagraphBuilder tableNameTextParagraphBuilder = new ParagraphBuilder();
                        Paragraph tableNameTextParagraph = tableNameTextParagraphBuilder.setFont(normalFont).addText(outputJSONObject.getString("name")).setAlignment(Element.ALIGN_CENTER).builder();
                        ParagraphBuilder tableTypeTextParagraphBuilder = new ParagraphBuilder();
                        Paragraph tableTypeTextParagraph = tableTypeTextParagraphBuilder.setFont(normalFont).addText(outputJSONObject.getString("type")).setAlignment(Element.ALIGN_CENTER).builder();
                        ParagraphBuilder tableIsRequiredTextParagraphBuilder = new ParagraphBuilder();
                        Paragraph tableIsRequiredTextParagraph = tableIsRequiredTextParagraphBuilder.setFont(normalFont).addText(outputJSONObject.getString("isRequired")).setAlignment(Element.ALIGN_CENTER).builder();
                        ParagraphBuilder tableDescriptionTextParagraphBuilder = new ParagraphBuilder();
                        Paragraph tableDescriptionTextParagraph = tableDescriptionTextParagraphBuilder.setFont(normalFont).addText(outputJSONObject.getString("description")).setAlignment(Element.ALIGN_CENTER).builder();

                        outputTableBuilder.addCell(tableNameTextParagraph).addCell(tableTypeTextParagraph).addCell(tableIsRequiredTextParagraph).addCell(tableDescriptionTextParagraph);
                    }
                    builder.addTable(outputTableBuilder.builder(), false);
                }
                builder.addParagraph("-------------------------------------------------------------------------------------------------------------------------------------");
            }
            builder.close();
            os.flush();
            os.close();
        } catch (
                IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}

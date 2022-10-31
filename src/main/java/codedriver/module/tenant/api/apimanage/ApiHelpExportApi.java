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
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPTable;
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
        OutputStream os = response.getOutputStream();
        try {
            PDFBuilder pdfBuilder = new PDFBuilder();
            PdfWriter.getInstance(pdfBuilder.builder(), os);
            //自定义字体
            BaseFont bfChinese = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
            Font boldFont = new Font(bfChinese, 8, Font.BOLD);
            Font normalFont = new Font(bfChinese, 8, Font.NORMAL);

            //创建文档、设置页面设置、打开文档
            PDFBuilder.Builder builder = pdfBuilder.setPageSizeVertical().setMargins(30f, 30f, 30f, 30f).open();
            //定义标题
            Paragraph tokenParagraph = new ParagraphBuilder("接口token", boldFont).builder();
            Paragraph nameParagraph = new ParagraphBuilder("接口名称", boldFont).builder();
            Paragraph descriptionParagraph = new ParagraphBuilder("描述", boldFont).builder();
            Paragraph inputParagraph = new ParagraphBuilder("输入参数", boldFont).builder();
            Paragraph outputParagraph = new ParagraphBuilder("输出参数", boldFont).builder();
            //定义表头
            Paragraph tableNameParagraph = new ParagraphBuilder("名称", normalFont).builder();
            Paragraph tableTypeParagraph = new ParagraphBuilder("类型", normalFont).builder();
            Paragraph tableIsRequiredParagraph = new ParagraphBuilder("是否必填", normalFont).builder();
            Paragraph tableDescriptionParagraph = new ParagraphBuilder("说明", normalFont).builder();

            for (Object object : apiHelpJsonArray) {
                JSONObject jsonObject = (JSONObject) object;
                //接口token
                builder.addParagraph(tokenParagraph);
                builder.addParagraph(new ParagraphBuilder(jsonObject.getString("token"), normalFont).builder());
                //接口名称
                builder.addParagraph(nameParagraph);
                builder.addParagraph(new ParagraphBuilder(jsonObject.getString("name"), normalFont).builder());
                //接口描述
                if (StringUtils.isNotBlank(jsonObject.getString("description"))) {
                    builder.addParagraph(descriptionParagraph);
                    builder.addParagraph(new ParagraphBuilder(jsonObject.getString("description"), normalFont).builder());
                }
                //输入参数
                if (CollectionUtils.isNotEmpty(jsonObject.getJSONArray("input"))) {
                    builder.addParagraph(inputParagraph);
                    builder.addTable(addTableData(jsonObject.getJSONArray("input"), tableNameParagraph, tableTypeParagraph, tableIsRequiredParagraph, tableDescriptionParagraph), false);
                }
                //输出参数
                JSONArray outputArray = jsonObject.getJSONArray("output");
                if (CollectionUtils.isNotEmpty(outputArray)) {
                    builder.addParagraph(outputParagraph);
                    builder.addTable(addTableData(jsonObject.getJSONArray("output"), tableNameParagraph, tableTypeParagraph, tableIsRequiredParagraph, tableDescriptionParagraph), false);
                }
                //分割线
                builder.addParagraph("-------------------------------------------------------------------------------------------------------------------------------------");
            }
            builder.close();
            os.flush();
            os.close();
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
     * 添加表格数据
     *
     * @param dataArray                 数据数组
     * @param tableNameParagraph        表头：名称
     * @param tableTypeParagraph        表头：类型
     * @param tableIsRequiredParagraph  表头：是否必填
     * @param tableDescriptionParagraph 表头：描述
     * @return PdfPTable
     * @throws IOException       e
     * @throws DocumentException e
     */
    private PdfPTable addTableData(JSONArray dataArray, Paragraph tableNameParagraph, Paragraph tableTypeParagraph, Paragraph tableIsRequiredParagraph, Paragraph tableDescriptionParagraph) throws IOException, DocumentException {
        //自定义字体
        BaseFont bfChinese = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
        Font normalFont = new Font(bfChinese, 8, Font.NORMAL);

        //创建表格、添加表头
        TableBuilder tableBuilder = new TableBuilder(16, 4);
        tableBuilder.setHorizontalAlignment(Element.ALIGN_CENTER).setWidthPercentage(100.0F)
                .addCell(tableNameParagraph)
                .addCell(tableTypeParagraph)
                .addCell(tableIsRequiredParagraph)
                .addCell(tableDescriptionParagraph);
        //添加数据
        for (Object object : dataArray) {
            JSONObject jsonObject = (JSONObject) object;
            tableBuilder.addCell(new ParagraphBuilder(jsonObject.getString("name"), normalFont).builder())
                    .addCell(new ParagraphBuilder(jsonObject.getString("type"), normalFont).builder())
                    .addCell(new ParagraphBuilder(jsonObject.getString("isRequired"), normalFont).builder())
                    .addCell(new ParagraphBuilder(jsonObject.getString("description"), normalFont).builder());
        }
        return tableBuilder.builder();
    }
}

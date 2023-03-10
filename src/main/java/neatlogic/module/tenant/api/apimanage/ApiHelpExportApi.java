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

package neatlogic.module.tenant.api.apimanage;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.INTERFACE_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.exception.type.ApiNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.IApiComponent;
import neatlogic.framework.restful.core.IBinaryStreamApiComponent;
import neatlogic.framework.restful.core.IJsonStreamApiComponent;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentFactory;
import neatlogic.framework.restful.core.privateapi.PrivateBinaryStreamApiComponentBase;
import neatlogic.framework.restful.dao.mapper.ApiMapper;
import neatlogic.framework.restful.dto.ApiVo;
import neatlogic.framework.restful.enums.ApiType;
import neatlogic.framework.util.FileUtil;
import neatlogic.framework.util.pdf.PDFBuilder;
import neatlogic.framework.util.pdf.ParagraphBuilder;
import neatlogic.framework.util.pdf.TableBuilder;
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
        return "??????????????????";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "tokenList", type = ApiParamType.JSONARRAY, desc = "??????token ??????")
    })
    @Output({
            @Param(name = "Return", explode = ApiVo.class, isRequired = true, desc = "??????????????????")
    })
    @Description(desc = "????????????????????????")
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
        response.setHeader("Content-Disposition", "attachment; filename=" + FileUtil.getEncodedFileName("neatlogic????????????.pdf") + "\"");
        OutputStream os = response.getOutputStream();
        try {
            PDFBuilder pdfBuilder = new PDFBuilder();
            PdfWriter.getInstance(pdfBuilder.builder(), os);
            //???????????????
            BaseFont bfChinese = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
            Font boldFont = new Font(bfChinese, 8, Font.BOLD);
            Font normalFont = new Font(bfChinese, 8, Font.NORMAL);

            //????????????????????????????????????????????????
            PDFBuilder.Builder builder = pdfBuilder.setPageSizeVertical().setMargins(30f, 30f, 30f, 30f).open();
            //????????????
            Paragraph tokenParagraph = new ParagraphBuilder("??????token", boldFont).builder();
            Paragraph nameParagraph = new ParagraphBuilder("????????????", boldFont).builder();
            Paragraph descriptionParagraph = new ParagraphBuilder("??????", boldFont).builder();
            Paragraph inputParagraph = new ParagraphBuilder("????????????", boldFont).builder();
            Paragraph outputParagraph = new ParagraphBuilder("????????????", boldFont).builder();
            //????????????
            Paragraph tableNameParagraph = new ParagraphBuilder("??????", normalFont).builder();
            Paragraph tableTypeParagraph = new ParagraphBuilder("??????", normalFont).builder();
            Paragraph tableIsRequiredParagraph = new ParagraphBuilder("????????????", normalFont).builder();
            Paragraph tableDescriptionParagraph = new ParagraphBuilder("??????", normalFont).builder();

            for (Object object : apiHelpJsonArray) {
                JSONObject jsonObject = (JSONObject) object;
                //??????token
                builder.addParagraph(tokenParagraph);
                builder.addParagraph(new ParagraphBuilder(jsonObject.getString("token"), normalFont).builder());
                //????????????
                builder.addParagraph(nameParagraph);
                builder.addParagraph(new ParagraphBuilder(jsonObject.getString("name"), normalFont).builder());
                //????????????
                if (StringUtils.isNotBlank(jsonObject.getString("description"))) {
                    builder.addParagraph(descriptionParagraph);
                    builder.addParagraph(new ParagraphBuilder(jsonObject.getString("description"), normalFont).builder());
                }
                //????????????
                if (CollectionUtils.isNotEmpty(jsonObject.getJSONArray("input"))) {
                    builder.addParagraph(inputParagraph);
                    builder.addTable(addTableData(jsonObject.getJSONArray("input"), tableNameParagraph, tableTypeParagraph, tableIsRequiredParagraph, tableDescriptionParagraph));
                }
                //????????????
                JSONArray outputArray = jsonObject.getJSONArray("output");
                if (CollectionUtils.isNotEmpty(outputArray)) {
                    builder.addParagraph(outputParagraph);
                    builder.addTable(addTableData(jsonObject.getJSONArray("output"), tableNameParagraph, tableTypeParagraph, tableIsRequiredParagraph, tableDescriptionParagraph));
                }
                //?????????
                builder.addParagraph("-------------------------------------------------------------------------------------------------------------------------------------");
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
     * ??????????????????
     *
     * @param dataArray                 ????????????
     * @param tableNameParagraph        ???????????????
     * @param tableTypeParagraph        ???????????????
     * @param tableIsRequiredParagraph  ?????????????????????
     * @param tableDescriptionParagraph ???????????????
     * @return PdfPTable
     * @throws IOException       e
     * @throws DocumentException e
     */
    private PdfPTable addTableData(JSONArray dataArray, Paragraph tableNameParagraph, Paragraph tableTypeParagraph, Paragraph tableIsRequiredParagraph, Paragraph tableDescriptionParagraph) throws IOException, DocumentException {
        //???????????????
        BaseFont bfChinese = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
        Font normalFont = new Font(bfChinese, 8, Font.NORMAL);

        //???????????????????????????
        TableBuilder tableBuilder = new TableBuilder(4);
        tableBuilder.setHorizontalAlignment(Element.ALIGN_CENTER).setWidthPercentage(100.0F)
                .addCell(tableNameParagraph)
                .addCell(tableTypeParagraph)
                .addCell(tableIsRequiredParagraph)
                .addCell(tableDescriptionParagraph);
        //????????????
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

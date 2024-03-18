/*Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.

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

package neatlogic.module.tenant.api.apimanage;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfAction;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.DottedLineSeparator;
import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.INTERFACE_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dto.module.ModuleGroupVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.IApiComponent;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentFactory;
import neatlogic.framework.restful.core.privateapi.PrivateBinaryStreamApiComponentBase;
import neatlogic.framework.restful.dto.ApiVo;
import neatlogic.framework.util.FileUtil;
import neatlogic.framework.util.pdf.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.*;

@Service
@AuthAction(action = INTERFACE_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ApiHelpExportApi extends PrivateBinaryStreamApiComponentBase {
    static Logger logger = LoggerFactory.getLogger(ApiHelpExportApi.class);

    @Override
    public String getToken() {
        return "api/help/export";
    }

    @Override
    public String getName() {
        return "nmtaa.apihelpexportapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "keyword", type = ApiParamType.STRING, xss = true, desc = "common.keyword"),
            @Param(name = "moduleGroup", type = ApiParamType.STRING, desc = "common.modulegroup"),
            @Param(name = "funcId", type = ApiParamType.STRING, desc = "nmtaa.apihelpexportapi.input.param.desc.funcid"),
//            @Param(name = "apiType", type = ApiParamType.STRING, isRequired = true, desc = "接口类型(system|custom)"),
    })
    @Output({})
    @Description(desc = "nmtaa.apihelpexportapi.getname")
    @Override
    public Object myDoService(JSONObject jsonObj, HttpServletRequest request, HttpServletResponse response) throws Exception {
        String keyword = jsonObj.getString("keyword");
        String moduleGroup = jsonObj.getString("moduleGroup");
        String funcId = jsonObj.getString("funcId");
        boolean moduleGroupIsNotBlank = StringUtils.isNotBlank(moduleGroup);
        boolean funcIdIsNotBlank = StringUtils.isNotBlank(funcId);
        boolean keywordIsNotBlank = StringUtils.isNotBlank(keyword);
        Map<String, List<ApiVo>> moduleGroup2TokenListMap = new HashMap<>();
        for (ApiVo api : PrivateApiComponentFactory.getTenantActiveApiList()) {
            if (moduleGroupIsNotBlank && !Objects.equals(api.getModuleGroup(), moduleGroup)) {
                continue;
            }
            String token = api.getToken();
            String name = api.getName();
            if (funcIdIsNotBlank) {
                if (token.contains("/")) {
                    if (!token.startsWith(funcId + "/")) {
                        continue;
                    }
                } else {
                    if (!token.equals(funcId)) {
                        continue;
                    }
                }
            }
            if (keywordIsNotBlank) {
                if (!(StringUtils.isNotBlank(name) && name.contains(keyword)) && !token.contains(keyword)) {
                    continue;
                }
            }
            moduleGroup2TokenListMap.computeIfAbsent(api.getModuleGroup(), key -> new ArrayList<>()).add(api);
        }
        for (Map.Entry<String, List<ApiVo>> entry : moduleGroup2TokenListMap.entrySet()) {
            List<ApiVo> apiList = entry.getValue();
            apiList.sort((o1, o2) -> String.CASE_INSENSITIVE_ORDER.compare(o1.getToken(), o2.getToken()));
        }
        if (MapUtils.isEmpty(moduleGroup2TokenListMap)) {
            return null;
        }
        response.setContentType("application/vnd.ms-excel;charset=utf-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + FileUtil.getEncodedFileName("neatlogic接口文档.pdf") + "\"");
        OutputStream os = response.getOutputStream();
        OutputStream baos = new ByteArrayOutputStream();
        try {
            CharterInfoEvent charterInfoEvent = new CharterInfoEvent();
            List<Chapter> chapterList = getChapterList(moduleGroup2TokenListMap, charterInfoEvent);
            PDFBuilder tempPdfBuilder = new PDFBuilder();
            //创建文档、设置页面设置、打开文档
            PDFBuilder.Builder tempBuilder = tempPdfBuilder.setPageSizeVertical().setMargins(48f, 48f, 60f, 60f).open();
            int catalogPageCount = createCatalog(baos, tempBuilder, charterInfoEvent, 0);
            tempBuilder.close();

            PDFBuilder pdfBuilder = new PDFBuilder();
            //创建文档、设置页面设置、打开文档
            PDFBuilder.Builder builder = pdfBuilder.setPageSizeVertical().setMargins(48f, 48f, 60f, 60f).open();
            createCatalog(os, builder, charterInfoEvent, catalogPageCount);
            builder.newPage();
            //添加内容
            for (Chapter c : chapterList) {
                builder.addCharter(c);
                builder.newPage();
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
            if (baos != null) {
                baos.flush();
                baos.close();
            }
        }
        return null;
    }
    //根据目录编号的长度判断菜单的等级   例：1.1.1.  长度为3
    public static int countInString(String str1, String str2) {
        int total = 0;
        for (String tmp = str1; tmp != null && tmp.length() >= str2.length();){
            if(tmp.indexOf(str2) == 0){
                total++;
                tmp = tmp.substring(str2.length());
            }else{
                tmp = tmp.substring(1);
            }
        }
        return total;
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
        TableBuilder tableBuilder = new TableBuilder(4);
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

    /**
     * 组装PDF章节内容列表，得到每个章节对应的页码及锚点信息
     * @param moduleGroup2TokenListMap
     * @param charterInfoEvent
     * @return
     * @throws DocumentException
     * @throws IOException
     */
    private List<Chapter> getChapterList(Map<String, List<ApiVo>> moduleGroup2TokenListMap, CharterInfoEvent charterInfoEvent) throws DocumentException, IOException {
        try (OutputStream baos = new ByteArrayOutputStream()){
            PDFBuilder pdfBuilder = new PDFBuilder();
            PdfWriter pdfWriter = PdfWriter.getInstance(pdfBuilder.builder(), baos);
            pdfWriter.setPageEvent(charterInfoEvent);
            //自定义字体
            BaseFont bfChinese = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
            Font boldFont = new Font(bfChinese, 8, Font.BOLD);
            Font charterFont = new Font(bfChinese, 16, Font.BOLD);
            Font sectionFont = new Font(bfChinese, 12, Font.BOLD);
            Font normalFont = new Font(bfChinese, 8, Font.NORMAL);

            //创建文档、设置页面设置、打开文档
            PDFBuilder.Builder builder = pdfBuilder.setPageSizeVertical().setMargins(48f, 48f, 60f, 60f).open();
            //定义标题
            Paragraph tokenParagraph = new ParagraphBuilder("url", boldFont).builder();
            Paragraph nameParagraph = new ParagraphBuilder("名称", boldFont).builder();
            Paragraph descriptionParagraph = new ParagraphBuilder("描述", boldFont).builder();
            Paragraph inputParagraph = new ParagraphBuilder("输入参数", boldFont).builder();
            Paragraph outputParagraph = new ParagraphBuilder("输出参数", boldFont).builder();
            //定义表头
            Paragraph tableNameParagraph = new ParagraphBuilder("名称", normalFont).builder();
            Paragraph tableTypeParagraph = new ParagraphBuilder("类型", normalFont).builder();
            Paragraph tableIsRequiredParagraph = new ParagraphBuilder("是否必填", normalFont).builder();
            Paragraph tableDescriptionParagraph = new ParagraphBuilder("说明", normalFont).builder();

            List<ModuleGroupVo> moduleGroupList = TenantContext.get().getActiveModuleGroupList();
            moduleGroupList.sort(Comparator.comparingInt(ModuleGroupVo::getGroupSort));
            List<Chapter> chapterList = new ArrayList<>();
            int firstIndex = 1;
            for (ModuleGroupVo moduleGroupVo : moduleGroupList) {
                List<ApiVo> apiList = moduleGroup2TokenListMap.get(moduleGroupVo.getGroup());
                if (CollectionUtils.isEmpty(apiList)) {
                    continue;
                }
                Chapter chapter = new Chapter(new Paragraph(new Chunk(moduleGroupVo.getGroupName(), charterFont).setLocalDestination(moduleGroupVo.getGroup())), firstIndex);
                for (ApiVo api : apiList) {
                    IApiComponent apiComponent = PrivateApiComponentFactory.getInstance(api.getHandler());
                    if (apiComponent == null) {
                        continue;
                    }
                    String name = api.getName();
                    if (name == null) {
                        continue;
                    }
                    String token = api.getToken();
                    Section section = chapter.addSection(new Paragraph(new Chunk(token + " " + name, sectionFont).setLocalDestination(token)), 2);
                    section.setIndentationLeft(10);
                    //接口token
                    section.add(tokenParagraph);
                    section.add(new ParagraphBuilder(token, normalFont).builder());
                    //接口名称
                    section.add(nameParagraph);
                    section.add(new ParagraphBuilder(name, normalFont).builder());
                    JSONObject helpObj = apiComponent.help();
                    //接口描述
                    if (StringUtils.isNotBlank(helpObj.getString("description"))) {
                        section.add(descriptionParagraph);
                        section.add(new ParagraphBuilder(helpObj.getString("description"), normalFont).builder());
                    }
                    //输入参数
                    if (CollectionUtils.isNotEmpty(helpObj.getJSONArray("input"))) {
                        section.add(inputParagraph);
                        section.add(addTableData(helpObj.getJSONArray("input"), tableNameParagraph, tableTypeParagraph, tableIsRequiredParagraph, tableDescriptionParagraph));
                    }
                    //输出参数
                    JSONArray outputArray = helpObj.getJSONArray("output");
                    if (CollectionUtils.isNotEmpty(outputArray)) {
                        section.add(outputParagraph);
                        section.add(addTableData(helpObj.getJSONArray("output"), tableNameParagraph, tableTypeParagraph, tableIsRequiredParagraph, tableDescriptionParagraph));
                    }
                    //分割线
                    section.add(new Paragraph("--------------------------------------------------------------------------------------------------------------------------"));
                    section.add(new Paragraph("\n"));
                }
                builder.addCharter(chapter);
                chapterList.add(chapter);
                firstIndex++;
            }
            builder.close();
            return chapterList;
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * 组装目录，得到目录页数
     * @param os
     * @param builder
     * @param charterInfoEvent
     * @param pageNumberOffset
     * @return
     * @throws IOException
     * @throws DocumentException
     */
    private int createCatalog(OutputStream os, PDFBuilder.Builder builder, CharterInfoEvent charterInfoEvent, int pageNumberOffset) throws IOException, DocumentException {
        //自定义字体
        BaseFont bfChinese = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
        Font normalFont = new Font(bfChinese, 8, Font.NORMAL);
        Font titleFont = new Font(bfChinese, 40, Font.NORMAL);

        PdfWriter pdfWriter = PdfWriter.getInstance(builder.builder(), os);
        AddPageNumberEvent addPageNumberEvent = new AddPageNumberEvent();
        pdfWriter.setPageEvent(addPageNumberEvent);
        builder.builder().open();
        builder.addParagraph(new ParagraphBuilder("neatlogic接口文档", titleFont).setAlignment(Element.ALIGN_CENTER).builder());

        Chapter indexChapter = new Chapter(new ParagraphBuilder("目录", normalFont).setAlignment(Element.ALIGN_CENTER).builder(), 0);
        indexChapter.setNumberDepth(-1);
        for (Map.Entry<String, Integer> index : charterInfoEvent.getIndex().entrySet()) {
            String key = index.getKey();
            String keyValue = key;
            Integer depth = charterInfoEvent.getDepth(key);
            depth = depth == null ? 1 : depth;
            for (int i = 1; i < depth; i++) {
                keyValue = "    " + keyValue;
            }
            String localDestination = charterInfoEvent.getLocalDestination(key);
            Paragraph paragraph = new Paragraph(new Chunk(keyValue, normalFont).setAction(PdfAction.gotoLocalPage(localDestination, false)));
            Chunk dottedLineSeparatorChunk = new Chunk(new DottedLineSeparator());
            Chunk pageNumberChunk = new Chunk(index.getValue() + pageNumberOffset + "");
            //加入点点
            paragraph.add(dottedLineSeparatorChunk);
            //加入页码
            paragraph.add(pageNumberChunk);
            indexChapter.add(paragraph);
        }
        builder.addCharter(indexChapter);
        return pdfWriter.getPageNumber();
    }
}

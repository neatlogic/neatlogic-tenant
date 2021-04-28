/*
 * Copyright(c) 2021 TechSureCo.,Ltd.AllRightsReserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.file;

import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.exception.file.FileExtNotAllowedException;
import codedriver.framework.exception.file.FileNotUploadException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateBinaryStreamApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * 检查导入的文件接口
 *
 * @author linbq
 * @since 2021/4/13 11:21
 **/
@Service
@Transactional
@OperationType(type = OperationTypeEnum.SEARCH)
public class FileImportCheckApi extends PrivateBinaryStreamApiComponentBase {
	
	@Override
	public String getToken() {
		return "file/import/check";
	}

	@Override
	public String getName() {
		return "检查导入的文件";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Output({
			@Param(explode = ValueTextVo[].class, desc = "文件名称列表")
	})
	@Description(desc = "检查导入的文件")
	@Override
	public Object myDoService(JSONObject paramObj, HttpServletRequest request, HttpServletResponse response) throws Exception {
		MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
		//获取所有导入文件
		Map<String, MultipartFile> multipartFileMap = multipartRequest.getFileMap();
		//如果没有导入文件, 抛异常
		if(multipartFileMap == null || multipartFileMap.isEmpty()) {
			throw new FileNotUploadException();
		}
		List<ValueTextVo> resultList = new ArrayList<>();
		//遍历导入文件, 目前只获取第一个文件内容, 其余的放弃
		for(Entry<String, MultipartFile> entry : multipartFileMap.entrySet()) {
			MultipartFile multipartFile = entry.getValue();
			try (ZipInputStream zipis = new ZipInputStream(multipartFile.getInputStream())) {
				ZipEntry zipEntry = null;
				while((zipEntry = zipis.getNextEntry()) != null){
					resultList.add(new ValueTextVo(zipEntry.getName(), zipEntry.getName()));
				}
			}catch(IOException e) {
				throw new FileExtNotAllowedException(multipartFile.getOriginalFilename());
			}
		}
		return resultList;
	}

	public static void main(String[] args) throws FileNotFoundException, IOException {
//		ValueTextVo valueTextVo = new ValueTextVo(11111111, "linbq_20210427");
//		String toJsonString = JSONObject.toJSONString(valueTextVo);
//		FileOutputStream fos = new FileOutputStream("linbq_20210427.zip");
//		GZIPOutputStream gzip = new GZIPOutputStream(fos);
//		gzip.write(toJsonString.getBytes(StandardCharsets.UTF_8));
//		gzip.close();

//		FileOutputStream fos = new FileOutputStream("linbq_20210427_2.zip");
//		ZipOutputStream zipos = new ZipOutputStream(fos);
//		ValueTextVo valueTextVo = new ValueTextVo(11111111, "linbq_20210427_1");
//		zipos.putNextEntry(new ZipEntry("linbq_20210427_1.json"));
//		zipos.write(JSONObject.toJSONBytes(valueTextVo));
//		zipos.closeEntry();
//		valueTextVo = new ValueTextVo(22222222, "linbq_20210427_2");
//		zipos.putNextEntry(new ZipEntry("linbq_20210427_2.json"));
//		zipos.write(JSONObject.toJSONBytes(valueTextVo));
//		zipos.closeEntry();
//		zipos.close();

		FileInputStream fis = new FileInputStream("linbq_20210427_2.zip");
		ZipInputStream zipis = new ZipInputStream(fis);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ZipEntry entry = null;
		byte[] buf = new byte[1024];
		while((entry = zipis.getNextEntry()) != null){
			System.out.println(entry.getName());
			int len = 0;
			while((len = zipis.read(buf)) != -1){
				out.write(buf, 0, len);
			}
			System.out.println(new String(out.toByteArray(), StandardCharsets.UTF_8));

			out.reset();
		}
		out.close();
		zipis.close();
		fis.close();
	}
}

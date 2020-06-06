package codedriver.module.tenant.api.test;

import java.io.File;
import java.io.FileInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.core.ApiComponentBase;

@Transactional
public class TestApi extends ApiComponentBase {

	@Autowired
	private FileSystem fileSystem;

	@Override
	public String getToken() {
		return "test";
	}

	@Override
	public String getName() {
		return "测试输出log接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Description(desc = "测试输出log接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		System.out.println(fileSystem.mkdirs(new Path("/codedriver/")));
		File file = new File("/Users/chenqiwei/Downloads/031001900111_88033198.jpg");
		FileInputStream reader = new FileInputStream(file);
		String fileName = file.getName();
		// 上传时默认当前目录，后面自动拼接文件的目录
		Path newPath = new Path("/codedriver22222/" + fileName);
		// 打开一个输出流
		FSDataOutputStream outputStream = fileSystem.create(newPath);
		IOUtils.copy(reader, outputStream);
		outputStream.close();
		return "OK";
	}

}

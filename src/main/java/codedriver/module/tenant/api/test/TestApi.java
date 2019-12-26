package codedriver.module.tenant.api.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;


import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.core.ApiComponentBase;


@Service
public class TestApi extends ApiComponentBase {

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

	public static String getURLContent(String requestUrl,String method) throws IOException {
		HttpURLConnection conn = null;
		BufferedReader br = null;
		StringBuffer sb = new StringBuffer();
		String content = null;
		// GET请求的url链接

		try {
			URL url = new URL(requestUrl);
			conn = (HttpURLConnection) url.openConnection();
			//设置请求方式
			if ("GET" == method || null == method) {
				conn.setRequestMethod("GET");
			} else {
				conn.setRequestMethod("POST");
				//使用URL连接输出
				conn.setDoOutput(true);
			}
			//设置请求内核
			conn.setRequestProperty("User-Agent",
					"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.81 Safari/537.36");
			//设置不使用缓存
			conn.setUseCaches(false);
			//设置链接超时时间,毫秒为单位
			conn.setConnectTimeout(1000);
			//设置读取超时时间，毫秒为单位
			conn.setReadTimeout(1000);
			//设置当前链接是否自动处理重定向。setFollowRedirects设置所有的链接是否自动处理重定向
			conn.setInstanceFollowRedirects(false);
			//开启链接
			conn.connect();
			//处理post请求时的参数

			//获取字符输入流
			br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
			String strContent = null;
			while ((strContent = br.readLine()) != null) {
				sb.append(strContent);
			}
			content = sb.toString();

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} finally {
			//关闭流和链接
			if (null != br) {
				br.close();
			}
			if (null != conn) {
				conn.disconnect();
			}
		}

		return content;
	}

	@Description(desc = "测试输出log接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String url = jsonObj.getString("url");
		String result = getURLContent(url, "GET");
		return result;
	}

}

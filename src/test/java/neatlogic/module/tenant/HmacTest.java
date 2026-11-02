/*
 * Copyright (C) 2026  深圳极向量科技有限公司 All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package neatlogic.module.tenant;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.Base64Utils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/** 使用现有 Spring HTTP 客户端进行 HMAC 手工联调，不作为自动化用例执行。 */
public class HmacTest {

    public static String encrypt(String secret, String sign) {
        try {
            SecretKeySpec signingKey = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(signingKey);
            byte[] rawHmac = mac.doFinal(sign.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : rawHmac) {
                String shaHex = Integer.toHexString(b & 0xFF);
                if (shaHex.length() < 2) {
                    hexString.append(0);
                }
                hexString.append(shaHex);
            }
            return hexString.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "0000000000000000000000000000000000000000000000000000000000000000";
    }

    public static void main(String[] args) throws IOException {
        testRestApi();


    }

    /**
     * 使用流式 multipart 上传文件，HMAC 签名仅包含普通表单字段，保留原有字段名和上传文件名。
     */
    private static void testUploadBinaryApi() {
        JSONObject postDataObj = new JSONObject();
//        postDataObj.put("name", "name");
//        postDataObj.put("label", "name1");
//        postDataObj.put("icon", "name1");
//        postDataObj.put("typeId", "441079140720640");
        System.out.println(postDataObj);//打印请求body
        String cutUrl = "/neatlogic/api/binary/autoexec/script/import/forautoexec";
        String url = "http://localhost:8080" + cutUrl;
        String tenant = "develop";//租户
        String userId = "admin";//登录UserId
        String token = "eb40625d3464db3b4ddf361969524bf7";//用户token

        FileSystemResource file = new FileSystemResource("/Users/cocokong/Desktop/scripts_Demo_demo-local.pl.json");
        HttpHeaders fileHeaders = new HttpHeaders();
        fileHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        fileHeaders.setContentDispositionFormData("scriptInfo.json", "scripts_Demo_demo-local.pl");
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("scriptInfo.json", new HttpEntity<>(file, fileHeaders));
        for (Map.Entry<String, Object> entry : postDataObj.entrySet()) {
            body.add(entry.getKey(), entry.getValue().toString());
        }

        String postDataBase64 = Base64Utils.encodeToString(postDataObj.toString().getBytes());
        String sign = userId + "#" + cutUrl + "#" + postDataBase64;
        System.out.println("sign:" + sign);
        String authorization = encrypt(token, sign);//认证
        System.out.println("authorization:" + authorization);

        HttpHeaders requestHeaders = new HttpHeaders();
        // multipart boundary 由 Spring 转换器生成，与实际请求体保持一致。
        requestHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
        requestHeaders.add("Tenant", tenant);
        requestHeaders.add("Authorization", "Hmac " + authorization);
        requestHeaders.add("AuthType", "hmac");
        requestHeaders.add("x-access-key", userId);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, requestHeaders);
        // 直接从文件流发送，避免默认请求工厂将整个上传缓存在内存中。
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setBufferRequestBody(false);
        RestTemplate restTemplate = new RestTemplate(requestFactory);
        String result = restTemplate.postForObject(url, requestEntity, String.class);
        System.out.println(result);
    }


    /**
     * 下载 binary
     */
    private static void testDownloadBinaryApi() throws IOException {
        //请求url
        String cutUrl = "/neatlogic/api/binary/file/download";
        String url = "http://localhost:8080" + cutUrl;
        String tenant = "develop";//租户
        String userId = "admin";//登录UserId
        String token = "eb40625d3464db3b4ddf361969524bf7";//用户token 从”页面右上角头像-》个人设置-〉基础信息“可以获取
        JSONObject postDataObj = JSONObject.parseObject("{\"id\":1084815183822848}");//请求post data
        System.out.println(postDataObj.toString());//打印请求body

        //将postdata加密 进而获取authorization
        String postDataBase64 = Base64Utils.encodeToString(JSON.toJSONString(postDataObj, false).getBytes());
        String sign = userId + "#" + cutUrl + "#" + postDataBase64;
        System.out.println("sign:" + sign);
        String authorization = encrypt(token, sign);//认证
        System.out.println("authorization:" + authorization);

        //设置请求头
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        requestHeaders.add("Tenant", tenant);
        requestHeaders.add("Authorization", "Hmac " + authorization);
        requestHeaders.add("AuthType", "hmac");
        requestHeaders.add("x-access-key", userId);
        HttpEntity<JSONObject> requestEntity = new HttpEntity<>(postDataObj, requestHeaders);
        RestTemplate restTemplate = new RestTemplate();

        //调接口
        ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, byte[].class);
        byte[] fileContent = response.getBody();
        Path destination = Paths.get("/Users/cocokong/Desktop/test.png");
        Files.write(destination, fileContent); // 将文件内容写入到目标路径中
    }

    /**
     * 普通接口
     */
    private static void testRestApi() {
        String cutUrl = "/neatlogic/api/rest/cmdb/cientity/search";
        String url = "http://localhost:8282" + cutUrl;
        String tenant = "develop";//租户
        String userId = "admin";//登录UserId
        String token = "fd544cb40846bfb5591eb718d1d8e3cd";//用户token 从”页面右上角头像-》个人设置-〉基础信息“可以获取
        JSONObject postDataObj = JSONObject.parseObject("{\"ciId\":479609502048256,\"keyword\":\"Test\"}");//请求post data
        System.out.println(postDataObj.toString());//打印请求body

        //将postdata加密 进而获取authorization
        String postDataBase64 = Base64Utils.encodeToString(JSON.toJSONString(postDataObj, false).getBytes());
        String sign = userId + "#" + cutUrl + "#" + postDataBase64;
        System.out.println("sign:" + sign);
        String authorization = encrypt(token, sign);//认证
        System.out.println("authorization:" + authorization);

        //设置请求头
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        requestHeaders.add("Tenant", tenant);
        requestHeaders.add("Authorization", "Hmac " + authorization);
        requestHeaders.add("AuthType", "hmac");
        requestHeaders.add("x-access-key", userId);
        HttpEntity<JSONObject> requestEntity = new HttpEntity<>(postDataObj, requestHeaders);
        RestTemplate restTemplate = new RestTemplate();

        //调接口
        JSONObject resultObj = restTemplate.postForObject(url, requestEntity, JSONObject.class);
        System.out.println(resultObj);
    }
}

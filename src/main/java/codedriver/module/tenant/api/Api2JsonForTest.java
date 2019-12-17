package codedriver.module.tenant.api;

import java.lang.reflect.Method;
import java.util.Set;

import org.reflections.Reflections;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.framework.restful.core.BinaryStreamApiComponentBase;

public class Api2JsonForTest {
	final static String baseUrl ="/codedriver/api/rest/";
	/**
	 *   根据pakage 转为 doclever Json
	 * @param pakage
	 * @param apiTypeClass
	 * @return
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public static JSONObject api2Json(String pakage,String pakageName,Class<?> apiTypeClass) throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException {
		Reflections reflections = new Reflections("codedriver.module.tenant.api."+pakage);
		Set<?> apiClass = reflections.getSubTypesOf(apiTypeClass);
		JSONObject apiJSON = new JSONObject();
		apiJSON.put("name", pakageName);
		apiJSON.put("flag", "SBDoc");
		JSONArray dataJSONArray = new JSONArray();
		int sort = 0;
		for (Object c: apiClass) {
			JSONObject dataJSON = new JSONObject();
			Method method = ((Class<?>) c).getMethod("myDoService", JSONObject.class);
			if (method != null) {
				Description desc = method.getAnnotation(Description.class);
				dataJSON.put("sort", sort++);
				dataJSON.put("name", desc.desc());
				if(apiTypeClass.toString().equals(ApiComponentBase.class.toString())) {
					ApiComponentBase apiComponentBase = (ApiComponentBase) ((Class<?>) c).newInstance();
					dataJSON.put("remark", apiComponentBase.getName());
					dataJSON.put("url", baseUrl+apiComponentBase.getToken());
				}
				if(apiTypeClass.getGenericSuperclass() instanceof BinaryStreamApiComponentBase) {
					BinaryStreamApiComponentBase binaryStreamApiComponentBase = (BinaryStreamApiComponentBase) ((Class<?>) c).newInstance();
					dataJSON.put("remark", binaryStreamApiComponentBase.getName());
					dataJSON.put("url",  baseUrl+binaryStreamApiComponentBase.getToken());
				}
				dataJSON.put("method",  "POST");
				JSONArray paramJSONArray = new JSONArray();
				JSONObject paramJSON = new JSONObject();
				paramJSON.put("name", "参数");
				//Input
				Input input = method.getAnnotation(Input.class);
				if (input != null) {
					Param[] params = input.value();
					if (params != null && params.length > 0) {
						JSONArray rawJSONArray = new JSONArray();
						JSONObject bodyInfoJSON = new JSONObject(); 
						bodyInfoJSON.put("type", 1);
						bodyInfoJSON.put("rawType", 2);
						for (Param p : params) {
							JSONObject rawJSON = new JSONObject();
							rawJSON.put("name", p.name());
							rawJSON.put("must", p.isRequired()?1:0);
							rawJSON.put("type", getDoCleverParamType(p.type()));
							rawJSON.put("remark", p.desc());
							rawJSONArray.add(rawJSON);
						}
						bodyInfoJSON.put("rawJSONType", 0);
						bodyInfoJSON.put("rawJSON", rawJSONArray);
						paramJSON.put("bodyInfo", bodyInfoJSON);
						JSONArray headerJSONArray = new JSONArray();
						JSONObject headerJSON = new JSONObject();
						headerJSON.put("name", "Content-Type");
						headerJSON.put("value", "application/json");
						headerJSONArray.add(headerJSON);
						paramJSON.put("header", headerJSONArray);
					}
				}
				//Output
				Output output = method.getAnnotation(Output.class);
				if (output != null) {
					Param[] params = output.value();
					if (params != null && params.length > 0) {
						JSONArray outParamJSONArray = new JSONArray();
						for (Param p : params) {
							JSONObject outParamJSON = new JSONObject();
							outParamJSON.put("name", p.name());
							outParamJSON.put("type", getDoCleverParamType(p.type()));
							outParamJSON.put("remark", p.desc());
							outParamJSON.put("must", 0);
							outParamJSONArray.add(outParamJSON);
						}
						paramJSON.put("outParam", outParamJSONArray);
					}
				}
				paramJSONArray.add(paramJSON);
				dataJSON.put("param",paramJSONArray);
			}
			dataJSONArray.add(dataJSON);
		}
		apiJSON.put("data", dataJSONArray);
		return apiJSON;
	}
	
	private static int getDoCleverParamType(ApiParamType apiParamType) {
		int type = 0;
		switch(apiParamType) {
			case INTEGER : type = 1;break;
			case ENUM : type = 0;break;
			case BOOLEAN : type = 2;break;
			case STRING : type = 0;break;
			case LONG : type = 1;break;
			case JSONOBJECT : type = 4;break;
			case JSONARRAY : type = 3;break;
			case IP : type = 0;break;
			case REGEX : type = 0;break;
			default: break;
		}
		return type;
	}
	
	public static void main(String[] args) {
		try {
			System.out.println(api2Json("user","查询用户接口",ApiComponentBase.class).toJSONString());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

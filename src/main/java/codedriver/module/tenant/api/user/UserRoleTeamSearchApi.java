package codedriver.module.tenant.api.user;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.framework.restful.groupsearch.core.GroupSearchHandlerFactory;
import codedriver.framework.restful.groupsearch.core.IGroupSearchHandler;
@Service
public class UserRoleTeamSearchApi extends ApiComponentBase {

	@Override
	public String getToken() {
		return "user/role/team/search";
	}

	@Override
	public String getName() {
		return "用户角色及组织架构查询接口";
	}

	@Override
	public String getConfig() {
		return null;
	}
	@Input({
		@Param(name = "keyword", type = ApiParamType.STRING, desc = "关键字(用户id或名称),模糊查询", isRequired = false, xss = true),
		@Param(name = "valueList", type = ApiParamType.JSONARRAY,  isRequired = false, desc = "用于回显的参数列表"),
		@Param(name = "excludeList", type = ApiParamType.JSONARRAY,  isRequired = false, desc = "用于过滤回显参数"),
		@Param(name = "groupList", type = ApiParamType.JSONARRAY,  isRequired = false, desc = "限制接口返回类型，['processUserType','user','team','role']"),
		@Param(name = "total", type = ApiParamType.INTEGER, desc = "公展示数量 默认18", isRequired = false)
		})
	@Output({
		@Param(name="text", type = ApiParamType.STRING, desc="类型中文名"),
		@Param(name="value", type = ApiParamType.STRING, desc="类型"),
		@Param(name="dataList[0].text", type = ApiParamType.STRING, desc="类型具体选项名"),
		@Param(name="dataList[0].value", type = ApiParamType.STRING, desc="类型具体选项值")
	})
	@Description(desc = "用户角色及组织架构查询接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		List<Object> groupList = jsonObj.getJSONArray("groupList");
		List<Object> excludeList = jsonObj.getJSONArray("excludeList");
		int groupCount = 0;
		JSONArray resultArray = new JSONArray();
		Map<String, IGroupSearchHandler>  handlerMap = GroupSearchHandlerFactory.getComponentMap();
		for (Map.Entry<String,IGroupSearchHandler> handlerEntry: handlerMap.entrySet()) {
			IGroupSearchHandler handler = handlerEntry.getValue();
			if(groupList != null && !groupList.contains(handler.getName())) {
				continue;
			}
			//如果group存在不需要限制总数的类型
			if(handler.isLimit()) {
				groupCount++;
			}
			List<Object> dataList = null;
			if(jsonObj.containsKey("keyword")) {
				dataList = handler.search(jsonObj);
			}else {
				if(jsonObj.containsKey("valueList") && !jsonObj.getJSONArray("valueList").isEmpty()) {
					dataList = handler.reload(jsonObj);
				}else {
					return resultArray;
				}
			}
			JSONObject resultObj = handler.repack(dataList);
			//过滤 excludeList
			dataList = resultObj.getJSONArray("dataList");
			if(excludeList != null &&!excludeList.isEmpty()) {
				for(Object exclude : excludeList) {
					dataList= dataList.stream().filter(data-> !((JSONObject)data).getString("value").equalsIgnoreCase(exclude.toString())).collect(Collectors.toList());
				}
			}
			resultObj.put("dataList", dataList);
			if(handler.isLimit()) {
				resultObj.put("index", 0);
			}else {
				resultObj.put("index", dataList.size());
			}
			resultObj.put("isLimit", handler.isLimit());
			resultObj.put("isMore", true);
			resultArray.add(resultObj);
			
		}
		//排序
		resultArray.sort(Comparator.comparing(obj -> ((JSONObject) obj).getInteger("sort")));
		//如果是搜索模式
		if(jsonObj.containsKey("keyword")) {
			//总显示选项个数,默认18个
			Integer total = jsonObj.getInteger("total");
			if(total == null) {
				total = 18;
			}
			//预留“更多”选项位置
			total = total - groupCount;
			//计算index位置
			int i = 0;
			int totalTmp = 0;
			HashSet<String> set = new HashSet<>();
			out : while(totalTmp < total) {
				for(Object ob: resultArray) {
					if( ((JSONObject)ob).getBoolean("isLimit")) {
						JSONArray dataList = ((JSONObject)ob).getJSONArray("dataList");
						if(i < dataList.size()) {
							int index = ((JSONObject)ob).getInteger("index");
							((JSONObject)ob).put("index",++index);
							//判断是否还有多余项
							if(dataList.size() == index) {
								((JSONObject)ob).put("isMore", false);
								total++;
							}
							dataList.get(i);
							if(totalTmp < (total-1)) {
								totalTmp++;
							}else {
								break out;
							}
							
						}else {
							set.add(((JSONObject)ob).getString("value"));
						}
					}
				}
				if(set.size() == groupCount) {
					break out;
				}
				i++;
			}
			//则根据index删掉多余数据
			for(Object ob: resultArray) {
				JSONArray dataList = ((JSONObject)ob).getJSONArray("dataList");
				int index = ((JSONObject)ob).getInteger("index");
				((JSONObject)ob).put("dataList", dataList.subList(0, (index==0?0:index)));
			}
		}
		return resultArray;
	}

}

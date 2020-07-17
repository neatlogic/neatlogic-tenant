package codedriver.module.tenant.api.apiaudit;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.util.DateUtil;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.framework.restful.dto.ApiAuditVo;
import codedriver.module.tenant.service.apiaudit.ApiAuditService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 操作审计统计接口
 * 基本思路：
 * 1、根据模块、功能、关键词和操作类型先从所有的内存/数据库API中初筛
 * 2、根据初筛出来的API，再从api_audit表中进一步筛选符合其他条件的apiAudit
 * 3、根据筛选的起止时间和结束时间判断时间跨度，从而决定按日或按月分组
 * 4、利用时间跨度切片，分别统计日/月操作数、新增操作数、删除操作数、更新操作数、查询操作数
 */

@Service
public class ApiAuditGroupSearchApi extends ApiComponentBase {

	@Autowired
	private ApiAuditService apiAuditService;

	@Override
	public String getToken() {
		return "apiaudit/group/search";
	}

	@Override
	public String getName() {
		return "操作审计分组查询接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "moduleGroup", type = ApiParamType.STRING, desc = "API所属模块"),
		@Param(name = "funcId", type = ApiParamType.STRING, desc = "API所属功能"),
		@Param(name = "userUuid", type = ApiParamType.STRING, desc = "访问者UUID"),
		@Param(name = "operationType", type = ApiParamType.STRING, desc = "操作类型"),
		@Param(name = "timeRange", type = ApiParamType.INTEGER, desc="时间跨度"),
		@Param(name = "timeUnit", type = ApiParamType.STRING, desc="时间跨度单位(day|month)"),
		@Param(name = "orderType", type = ApiParamType.STRING, desc="排序类型(asc|desc)"),
		@Param(name = "startTime", type = ApiParamType.LONG, desc="开始时间"),
		@Param(name = "endTime", type = ApiParamType.LONG, desc="结束时间"),
		@Param(name = "keyword", type = ApiParamType.STRING, desc="搜索关键词")
	})
	@Output({
		@Param(name = "time", type = ApiParamType.LONG, desc="分组时间"),
		@Param(name = "count", type = ApiParamType.INTEGER, desc = "API操作总数"),
		@Param(name = "createCount", type = ApiParamType.INTEGER, desc = "新增操作数量"),
		@Param(name = "deleteCount", type = ApiParamType.INTEGER, desc = "删除操作数量"),
		@Param(name = "updateCount", type = ApiParamType.INTEGER, desc = "更新操作数量"),
		@Param(name = "retrieveCount", type = ApiParamType.INTEGER , desc = "查询操作数量")
	})
	@Description(desc = "操作审计分组查询接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		ApiAuditVo apiAuditVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<ApiAuditVo>() {});

		//筛选出符合条件的所有记录
		List<ApiAuditVo> apiAuditVoList = apiAuditService.searchApiAuditVo(apiAuditVo);

		SimpleDateFormat sdfOfDay = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat sdfOfMonth = new SimpleDateFormat("yyyy-MM");
		//计算时间跨度来决定按日还是按月分组
		List<Date> timeRangeForDay = DateUtil.calBetweenDaysDate(sdfOfDay.format(apiAuditVo.getStartTime()),sdfOfDay.format(apiAuditVo.getEndTime()));

		List<Map<String,Object>> resultList = new ArrayList<>();

		if(timeRangeForDay.size() <= 31){
			for(Date date : timeRangeForDay){
				Map<String,Object> countMap = new HashMap<>();
				countMap.put("time",sdfOfDay.format(date));
				countMap.put("count",0);
				countMap.put("createCount",0);
				countMap.put("deleteCount",0);
				countMap.put("updateCount",0);
				countMap.put("retrieveCount",0);
				for(ApiAuditVo vo : apiAuditVoList){
					if(sdfOfDay.format(vo.getStartTime()).equals(sdfOfDay.format(date))){
						countMap.put("count",Integer.parseInt(countMap.get("count").toString()) + 1);
						//TODO 按操作类型计数
						countMap.put("createCount",1);
						countMap.put("deleteCount",1);
						countMap.put("updateCount",1);
						countMap.put("retrieveCount",1);
					}
				}
				resultList.add(countMap);
			}
		}else{
			//根据起始时间和结束时间计算月度
			List<String> timeRangeForMonth = DateUtil.calculateMonthly(sdfOfDay.format(apiAuditVo.getStartTime()),sdfOfDay.format(apiAuditVo.getEndTime()));
			for(String month : timeRangeForMonth){
				Map<String,Object> countMap = new HashMap<>();
				countMap.put("time",month);
				countMap.put("count",0);
				countMap.put("createCount",0);
				countMap.put("deleteCount",0);
				countMap.put("updateCount",0);
				countMap.put("retrieveCount",0);
				for(ApiAuditVo vo : apiAuditVoList){
					if(sdfOfMonth.format(vo.getStartTime()).equals(month)){
						countMap.put("count",Integer.parseInt(countMap.get("count").toString()) + 1);
						//TODO 按操作类型计数
						countMap.put("createCount",1);
						countMap.put("deleteCount",1);
						countMap.put("updateCount",1);
						countMap.put("retrieveCount",1);
					}
				}
				resultList.add(countMap);
			}
		}

		return resultList;
	}

}

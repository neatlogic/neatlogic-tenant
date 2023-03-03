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

package neatlogic.module.tenant.api.apiaudit;

import neatlogic.framework.auth.core.AuthAction;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;

/**
 * 操作审计统计接口
 * 基本思路：
 * 1、根据模块、功能、关键词和操作类型先从所有的内存/数据库API中初筛
 * 2、根据初筛出来的API，再从api_audit表中进一步筛选符合其他条件的apiAudit
 * 3、根据筛选的起止时间和结束时间判断时间跨度，从而决定按日或按月分组
 * 4、利用时间跨度切片，分别统计日/月操作数、新增操作数、删除操作数、更新操作数、查询操作数
 */

@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class ApiAuditGroupSearchApi extends PrivateApiComponentBase {

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
//		ApiAuditVo apiAuditVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<ApiAuditVo>() {});
//
//		//筛选出符合条件的所有记录
//		List<ApiAuditVo> apiAuditVoList = apiAuditService.searchApiAuditVo(apiAuditVo);
//
//		if(CollectionUtils.isNotEmpty(apiAuditVoList)){
//			SimpleDateFormat sdfOfDay = new SimpleDateFormat("yyyy-MM-dd");
//			SimpleDateFormat sdfOfMonth = new SimpleDateFormat("yyyy-MM");
//			//计算时间跨度
//			List<Date> timeRangeForDay = TimeUtil.calBetweenDaysDate(sdfOfDay.format(apiAuditVo.getStartTime()),sdfOfDay.format(apiAuditVo.getEndTime()));
//
//			List<Map<String,Object>> resultList = new ArrayList<>();
//
//			if(timeRangeForDay.size() <= 31){
//				/**
//				 * 按天统计各类型操作数
//				 */
//				for(Date date : timeRangeForDay){
//					Map<String,Object> countMap = new HashMap<>();
//					countMap.put("startTime",sdfOfDay.format(date));
//					countMap.put("endTime",sdfOfDay.format(date));
//					countMap.put("time",sdfOfDay.format(date));
//					countMap.put("count",0);
//					countMap.put("createCount",0);
//					countMap.put("deleteCount",0);
//					countMap.put("updateCount",0);
//					countMap.put("retrieveCount",0);
//					for(ApiAuditVo vo : apiAuditVoList){
//						if(sdfOfDay.format(vo.getStartTime()).equals(sdfOfDay.format(date))){
//							countMap.put("count",Integer.parseInt(countMap.get("count").toString()) + 1);
//							//TODO 按操作类型计数
//							if(OperationTypeEnum.CREATE.getValue().equals(vo.getOperationType())){
//								countMap.put("createCount",Integer.parseInt(countMap.get("createCount").toString()) + 1);
//							}
//							if(OperationTypeEnum.DELETE.getValue().equals(vo.getOperationType())){
//								countMap.put("deleteCount",Integer.parseInt(countMap.get("deleteCount").toString()) + 1);
//							}
//							if(OperationTypeEnum.UPDATE.getValue().equals(vo.getOperationType())){
//								countMap.put("updateCount",Integer.parseInt(countMap.get("updateCount").toString()) + 1);
//							}
//							if(OperationTypeEnum.SEARCH.getValue().equals(vo.getOperationType())){
//								countMap.put("retrieveCount",Integer.parseInt(countMap.get("retrieveCount").toString()) + 1);
//							}
//						}
//					}
//					resultList.add(countMap);
//				}
//			}else{
//				/**
//				 * 根据起始时间和结束时间计算月度
//				 * timeRangeForMonth中记录了起始时间与结束时间之间的每个月
//				 * 遍历每一个筛选出来的ApiAuditVo，按月统计各类型操作数
//				 */
//				List<String> timeRangeForMonth = TimeUtil.calculateMonthly(sdfOfDay.format(apiAuditVo.getStartTime()),sdfOfDay.format(apiAuditVo.getEndTime()));
//				String currentMonth = sdfOfMonth.format(Calendar.getInstance().getTime());
//				for(String month : timeRangeForMonth){
//					Map<String,Object> countMap = new HashMap<>();
//					if(currentMonth.equals(month)){
//						Date firstDayOfMonth = TimeUtil.firstDayOfMonth(Calendar.getInstance().getTime());
//						countMap.put("startTime",sdfOfDay.format(firstDayOfMonth));
//						countMap.put("endTime",sdfOfDay.format(Calendar.getInstance().getTime()));
//					}else if(sdfOfMonth.format(apiAuditVo.getStartTime()).equals(month)){
//						Date lastDayOfMonth = TimeUtil.lastDayOfMonth(sdfOfDay.parse(month + "-01 00:00:00"));
//						countMap.put("startTime",sdfOfDay.format(apiAuditVo.getStartTime()));
//						countMap.put("endTime",sdfOfDay.format(lastDayOfMonth));
//					}else{
//						Date firstDayOfMonth = TimeUtil.firstDayOfMonth(sdfOfDay.parse(month + "-01 00:00:00"));
//						Date lastDayOfMonth = TimeUtil.lastDayOfMonth(sdfOfDay.parse(month + "-01 00:00:00"));
//						countMap.put("startTime",sdfOfDay.format(firstDayOfMonth));
//						countMap.put("endTime",sdfOfDay.format(lastDayOfMonth));
//					}
//					countMap.put("time",month);
//					countMap.put("count",0);
//					countMap.put("createCount",0);
//					countMap.put("deleteCount",0);
//					countMap.put("updateCount",0);
//					countMap.put("retrieveCount",0);
//					for(ApiAuditVo vo : apiAuditVoList){
//						if(sdfOfMonth.format(vo.getStartTime()).equals(month)){
//							countMap.put("count",Integer.parseInt(countMap.get("count").toString()) + 1);
//							//TODO 按操作类型计数
//							if(OperationTypeEnum.CREATE.getValue().equals(vo.getOperationType())){
//								countMap.put("createCount",Integer.parseInt(countMap.get("createCount").toString()) + 1);
//							}
//							if(OperationTypeEnum.DELETE.getValue().equals(vo.getOperationType())){
//								countMap.put("deleteCount",Integer.parseInt(countMap.get("deleteCount").toString()) + 1);
//							}
//							if(OperationTypeEnum.UPDATE.getValue().equals(vo.getOperationType())){
//								countMap.put("updateCount",Integer.parseInt(countMap.get("updateCount").toString()) + 1);
//							}
//							if(OperationTypeEnum.SEARCH.getValue().equals(vo.getOperationType())){
//								countMap.put("retrieveCount",Integer.parseInt(countMap.get("retrieveCount").toString()) + 1);
//							}
//						}
//					}
//					resultList.add(countMap);
//				}
//			}
//
//			return resultList;
//		}

		return null;
	}

}

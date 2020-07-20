package codedriver.module.tenant.service.apiaudit;

import codedriver.framework.restful.core.ApiComponentFactory;
import codedriver.framework.restful.dao.mapper.ApiMapper;
import codedriver.framework.restful.dto.ApiAuditVo;
import codedriver.framework.restful.dto.ApiVo;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

@Service
public class ApiAuditServiceImpl implements ApiAuditService{

    private static final String TIME_UINT_OF_DAY = "day";
    private static final String TIME_UINT_OF_MONTH = "month";

    @Autowired
    private ApiMapper apiMapper;

    @Override
    public List<ApiAuditVo> searchApiAuditVo(ApiAuditVo apiAuditVo) {
        List<ApiVo> apiList = new ArrayList<>();
        assembleParamsAndFilterApi(apiAuditVo,apiList);
        List<ApiAuditVo> apiAuditVoList = apiMapper.searchApiAuditList(apiAuditVo);

        for(ApiAuditVo vo : apiAuditVoList){
            for(ApiVo api : apiList){
                if(vo.getToken().equals(api.getToken())){
                    vo.setApiName(api.getName());
                    vo.setModuleGroup(api.getModuleGroup());
                    break;
                }
            }
        }
        return apiAuditVoList;
    }

    @Override
    public List<Map<String, String>> searchApiAuditMapList(ApiAuditVo apiAuditVo) {
        List<ApiVo> apiList = new ArrayList<>();
        assembleParamsAndFilterApi(apiAuditVo,apiList);
        List<Map<String, String>> apiAuditMapList = apiMapper.searchApiAuditMapList(apiAuditVo);
        for(Map<String, String> map : apiAuditMapList){
            for(ApiVo api : apiList){
                if(map.get("token").equals(api.getToken())){
                    map.put("apiName",api.getName());
                    map.put("moduleGroup",api.getModuleGroup());
                    break;
                }
            }
        }

        return apiAuditMapList;
    }

    private void assembleParamsAndFilterApi(ApiAuditVo apiAuditVo,List<ApiVo> apiList) {
        /**
         * 如果选择按下拉框上的时间跨度筛选，那么就要计算出筛选的起止时间
         */
        if (apiAuditVo.getTimeRange() != null && StringUtils.isNotBlank(apiAuditVo.getTimeUnit())) {
            if (TIME_UINT_OF_DAY.equals(apiAuditVo.getTimeUnit())) {
                apiAuditVo.setStartTime(DateUtils.addDays(Calendar.getInstance().getTime(), -apiAuditVo.getTimeRange()));
            } else if (TIME_UINT_OF_MONTH.equals(apiAuditVo.getTimeUnit())) {
                apiAuditVo.setStartTime(DateUtils.addMonths(Calendar.getInstance().getTime(), -apiAuditVo.getTimeRange()));
            }
            apiAuditVo.setEndTime(Calendar.getInstance().getTime());
        }

        List<ApiVo> apiVoList = ApiComponentFactory.getApiList();
        List<ApiVo> dbApiList = apiMapper.getAllApi();
        Map<String, ApiVo> ramApiMap = ApiComponentFactory.getApiMap();
        for (ApiVo vo : dbApiList) {
            if (ramApiMap.get(vo.getToken()) != null) {
                apiVoList.add(vo);
            }
        }
//        List<ApiVo> apiList = new ArrayList<>();
        List<String> apiTokenList = new ArrayList<>();
        for (ApiVo api : apiVoList) {
            //根据模块筛选接口
            if (StringUtils.isNotBlank(apiAuditVo.getModuleGroup()) && !apiAuditVo.getModuleGroup().equals(api.getModuleGroup())) {
                continue;
            }
            //根据功能筛选接口
            if (StringUtils.isNotBlank(apiAuditVo.getFuncId()) && !api.getToken().contains(apiAuditVo.getFuncId())) {
                continue;
            }
            if (StringUtils.isNotBlank(apiAuditVo.getKeyword())) {
                if (!api.getName().contains(apiAuditVo.getKeyword()) && !api.getToken().contains(apiAuditVo.getKeyword())) {
                    continue;
                }
            }
            //TODO 根据操作类型筛选
            apiList.add(api);
            apiTokenList.add(api.getToken());
        }
        //把筛选出来的api的token塞到apiAuditVo，以便之后的数据库查询
        apiAuditVo.setTokenList(apiTokenList);
    }
}

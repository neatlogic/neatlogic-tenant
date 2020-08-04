package codedriver.module.tenant.service.apiaudit;

import codedriver.framework.common.config.Config;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.core.ApiComponentFactory;
import codedriver.framework.restful.dao.mapper.ApiMapper;
import codedriver.framework.restful.dto.ApiAuditVo;
import codedriver.framework.restful.dto.ApiVo;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

@Service
public class ApiAuditServiceImpl implements ApiAuditService{

    Logger logger = LoggerFactory.getLogger(ApiAuditServiceImpl.class);

    private static final String TIME_UINT_OF_DAY = "day";
    private static final String TIME_UINT_OF_MONTH = "month";

    @Autowired
    private ApiMapper apiMapper;

    @Override
    public List<ApiAuditVo> searchApiAuditVo(ApiAuditVo apiAuditVo) throws ClassNotFoundException {
        List<ApiVo> apiList = new ArrayList<>();
        assembleParamsAndFilterApi(apiAuditVo,apiList);
        if(CollectionUtils.isEmpty(apiAuditVo.getTokenList())){
            return null;
        }
        List<ApiAuditVo> apiAuditVoList = apiMapper.searchApiAuditList(apiAuditVo);
        if(apiAuditVo.getNeedPage()){
            apiAuditVo.setRowNum(apiMapper.searchApiAuditListCount(apiAuditVo));
        }
        /**
         * 补充从数据库无法获取的字段
         */
        addFields(apiList, apiAuditVoList);
        return apiAuditVoList;
    }

    @Override
    public List<ApiAuditVo> searchApiAuditForExport(ApiAuditVo apiAuditVo) throws Exception {
        List<ApiVo> apiList = new ArrayList<>();
        assembleParamsAndFilterApi(apiAuditVo,apiList);
        if(CollectionUtils.isEmpty(apiAuditVo.getTokenList())){
            return null;
        }
        List<ApiAuditVo> apiAuditList = apiMapper.searchApiAuditForExport(apiAuditVo);

        /**
         * 读取文件中的参数/结果/错误
         */
        if(CollectionUtils.isNotEmpty(apiAuditList)){
            for(ApiAuditVo vo : apiAuditList){
                String paramFilePath = vo.getParamFilePath();
                String resultFilePath = vo.getResultFilePath();
                String errorFilePath = vo.getErrorFilePath();

                if(StringUtils.isNotBlank(paramFilePath)){
                    String param = getAuditContentOnFile(paramFilePath);
                    vo.setParam(param);
                }
                if(StringUtils.isNotBlank(resultFilePath)){
                    String result = getAuditContentOnFile(resultFilePath);
                    vo.setResult(result);
                }
                if(StringUtils.isNotBlank(errorFilePath)){
                    String error = getAuditContentOnFile(errorFilePath);
                    vo.setError(error);
                }
            }
        }


        /**
         * 补充从数据库无法获取的字段
         */
        addFields(apiList, apiAuditList);

        return apiAuditList;
    }

    @Override
    public int searchApiAuditVoCount(ApiAuditVo vo) throws ClassNotFoundException {
        List<ApiVo> apiList = new ArrayList<>();
        assembleParamsAndFilterApi(vo,apiList);
        if(CollectionUtils.isEmpty(vo.getTokenList())){
            return 0;
        }
        int apiAuditVoCount = apiMapper.searchApiAuditListCount(vo);
        return apiAuditVoCount;

    }

    /**
     * 筛选出api_audit表中有记录的API
     * @return
     */
    @Override
    public List<ApiVo> getApiListForTree() {
        List<String> distinctTokenInApiAudit = apiMapper.getDistinctTokenInApiAudit();
        List<ApiVo> apiList = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(distinctTokenInApiAudit)){
            Map<String, ApiVo> ramApiMap = ApiComponentFactory.getApiMap();
            List<ApiVo> dbApiList = apiMapper.getAllApi();
            if(CollectionUtils.isNotEmpty(dbApiList)){
                List<ApiVo> customApiVos = new ArrayList<>();
                for (ApiVo vo : dbApiList) {
                    if (ramApiMap.get(vo.getToken()) == null) {
                        customApiVos.add(vo);
                    }
                }
                /** 从内存和数据库中筛选api，如果内存中找不到，那就从数据库中找*/
                for (String token : distinctTokenInApiAudit) {
                    ApiVo apiVo = ramApiMap.get(token);
                    if (apiVo != null) {
                        apiList.add(apiVo);
                    }else {
                        for (ApiVo vo : customApiVos) {
                            if (vo.getToken().equals(token)) {
                                apiList.add(vo);
                            }
                        }
                    }
                }
            }
        }
        return apiList;
    }

    @Override
    public String getAuditContentOnFile(String filePath){
        if(StringUtils.isBlank(filePath) || !filePath.contains("?")
                || !filePath.contains("&") || !filePath.contains("=")){
            return null;
        }
        String result = null;
//        raf.seek(0);
        String path = filePath.split("\\?")[0];
//        String indexStr = filePath.split("\\?")[1];
        String[] indexs = filePath.split("\\?")[1].split("&");
//        String startIndexStr = indexs[0].split("=")[1];
//        String offsetStr = indexs[1].split("=")[1];
        Long startIndex = Long.parseLong(indexs[0].split("=")[1]);
        Long offset = Long.parseLong(indexs[1].split("=")[1]);

        RandomAccessFile raf = null;
        File file = new File(Config.DATA_HOME() + path.substring(5));
        if(file.exists() && file.isFile()){
            try {
                raf = new RandomAccessFile(file,"r");
            } catch (FileNotFoundException e) {
                logger.error("文件：" + filePath + "不存在");
                e.printStackTrace();
                return null;
            }
        }
        if(raf != null){
            try {
                raf.seek(startIndex);
            } catch (IOException e) {
                logger.error("文件指针移动失败：" + filePath + "\n" + e.getMessage());
                return null;
            }
            byte[] buff = new byte[offset.intValue()];
            try {
                raf.read(buff);
            } catch (IOException e) {
                logger.error("文件读取失败：" + filePath + "\n" + e.getMessage());
                return null;
            }
            result = new String(buff,0,buff.length, StandardCharsets.UTF_8);
            try {
                raf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    private void assembleParamsAndFilterApi(ApiAuditVo apiAuditVo, List<ApiVo> apiList) throws ClassNotFoundException {
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

        /** 首先筛选出api_audit表中有记录的API，再用这些API做进一步的筛选 */
        List<ApiVo> apiVoList = getApiListForTree();
//        List<ApiVo> dbApiList = apiMapper.getAllApi();
//        Map<String, ApiVo> ramApiMap = ApiComponentFactory.getApiMap();
//        for (ApiVo vo : dbApiList) {
//            if (ramApiMap.get(vo.getToken()) == null) {
//                apiVoList.add(vo);
//            }
//        }
//        List<ApiVo> apiList = new ArrayList<>();
        List<String> apiTokenList = new ArrayList<>();
        for (ApiVo api : apiVoList) {
            //根据模块筛选接口
            if (StringUtils.isNotBlank(apiAuditVo.getModuleGroup()) && !apiAuditVo.getModuleGroup().equals(api.getModuleGroup())) {
                continue;
            }
            //根据功能筛选接口
//            if (StringUtils.isNotBlank(apiAuditVo.getFuncId()) && !api.getToken().contains(apiAuditVo.getFuncId())) {
//                continue;
//            }
            if (StringUtils.isNotBlank(apiAuditVo.getFuncId())) {
                if(api.getToken().contains("/")){
                    if(!api.getToken().startsWith(apiAuditVo.getFuncId() + "/")){
                        continue;
                    }
                }else{
                    if(!api.getToken().equals(apiAuditVo.getFuncId())){
                        continue;
                    }
                }

            }
            if (StringUtils.isNotBlank(apiAuditVo.getKeyword())) {
                if (!api.getName().contains(apiAuditVo.getKeyword()) && !api.getToken().contains(apiAuditVo.getKeyword())) {
                    continue;
                }
            }
            //TODO 根据操作类型筛选
            if(StringUtils.isNotBlank(apiAuditVo.getOperationType())){
                Class<?> apiClass = Class.forName(api.getHandler());
                OperationType annotation = apiClass.getAnnotation(OperationType.class);
                if(annotation == null || !apiAuditVo.getOperationType().equals(annotation.type().getValue())){
                    continue;
                }
            }
            apiList.add(api);
            apiTokenList.add(api.getToken());
        }
        //把筛选出来的api的token塞到apiAuditVo，以便之后的数据库查询
        apiAuditVo.setTokenList(apiTokenList);
    }

    private void addFields(List<ApiVo> apiList, List<ApiAuditVo> apiAuditVoList) throws ClassNotFoundException {
        if(CollectionUtils.isNotEmpty(apiList) && CollectionUtils.isNotEmpty(apiAuditVoList)){
            for (ApiAuditVo vo : apiAuditVoList) {
                for (ApiVo api : apiList) {
                    if (vo.getToken().equals(api.getToken())) {
                        vo.setApiName(api.getName());
                        vo.setModuleGroup(api.getModuleGroup());
                        Class<?> apiClass = Class.forName(api.getHandler());
                        OperationType annotation = apiClass.getAnnotation(OperationType.class);
                        if (annotation != null) {
                            vo.setOperationType(annotation.type().getValue());
                        }else{
                            //如果API没有加操作类型的注解，那么默认视为SEARCH
                            vo.setOperationType(OperationTypeEnum.SEARCH.getValue());
                        }
                        break;
                    }
                }
            }
        }

    }
}

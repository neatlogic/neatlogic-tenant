package codedriver.module.tenant.service.counter;

import codedriver.framework.dto.ModuleVo;
import codedriver.framework.counter.dto.GlobalCounterSubscribeVo;
import codedriver.framework.counter.dto.GlobalCounterVo;

import java.util.List;

public interface GlobalCounterService {
    /** 
    * @Description: 检索消息统计
    * @Param: [counterVo] 
    * @return: java.util.List<com.techsure.balantflow.dto.globalcounter.GlobalCounterVo>  
    */ 
    List<GlobalCounterVo> searchCounterVo(GlobalCounterVo counterVo);

    /** 
    * @Description: 获取消息统计的模块集合
    * @Param: [] 
    * @return: java.util.List<com.techsure.balantflow.dto.ModuleVo>  
    */ 
    List<ModuleVo> getActiveCounterModuleList();

    /** 
    * @Description: 获取订阅的消息统计集合
    * @Param: [userUuid] 
    * @return: java.util.List<com.techsure.balantflow.dto.globalcounter.GlobalCounterVo>  
    */ 
    List<GlobalCounterVo> getSubscribeCounterListByUserUuid(String userUuid);

    /** 
    * @Description: 消息统计消息订阅
    * @Param: [counterSubscribeVo] 
    * @return: void  
    */ 
    void updateCounterSubscribe(GlobalCounterSubscribeVo counterSubscribeVo);

    /** 
    * @Description: 重排序
    * @Param: [userUuid, afterSort] 
    * @return: void  
    */ 
    void updateCounterUserSort(String userUuid, String sortPluginIdStr);
}

package neatlogic.module.tenant.schedule.plugin;

import neatlogic.module.framework.login.handler.LoginController;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@DisallowConcurrentExecution
public class ResetTenantSchedule implements Job {
    private final Logger logger = LoggerFactory.getLogger(ResetTenantSchedule.class);

    @Override
    public void execute(JobExecutionContext context) {
        //清空访问记录
        LoginController.tenantVisitSet.clear();
    }

    @PostConstruct
    public void init() {
        try {
            // 创建调度器
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();

            // 定义JobDetail
            JobDetail jobDetail = JobBuilder.newJob(ResetTenantSchedule.class)
                    .withIdentity("dailyJob", "reset-tenant")
                    .build();

            // 定义Trigger
            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("dailyTrigger", "reset-tenant")
                    .withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(1, 0)) // 每天1点执行
                    .build();

            // 将JobDetail和Trigger注册到调度器中
            scheduler.scheduleJob(jobDetail, trigger);

            // 启动调度器
            scheduler.start();
        } catch (SchedulerException e) {
            logger.error(e.getMessage(), e);
            e.printStackTrace();
        }
    }
}

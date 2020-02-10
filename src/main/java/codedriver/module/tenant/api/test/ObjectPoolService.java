package codedriver.module.tenant.api.test;

import codedriver.framework.common.config.Config;
import com.alibaba.fastjson.JSONObject;
import com.techsure.multiattrsearch.*;
import com.techsure.multiattrsearch.query.QueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Map;

@Service
public class ObjectPoolService {
    private static final Logger logger = LoggerFactory.getLogger(ObjectPoolService.class);

    private static final String POOL_NAME = "test";

    private MultiAttrsObjectPool objectPool;

    @PostConstruct
    public void init() {
       /* Map<String, String> esClusters = Config.ES_CLUSTERS;
        if (esClusters.isEmpty()) {
            throw new IllegalStateException("ES集群信息未配置，es.cluster.<cluster-name>=<ip:port>[,<ip:port>...]");
        }

        MultiAttrsSearchConfig config = new MultiAttrsSearchConfig();
        config.setPoolName(POOL_NAME);

        Map.Entry<String, String> cluster = esClusters.entrySet().iterator().next();
        config.addCluster(cluster.getKey(), cluster.getValue());
        if (esClusters.size() > 1) {
            logger.warn("multiple clusters available, only cluster {} was used (picked randomly) for testing", cluster.getKey());
        }

        objectPool = MultiAttrsSearch.getObjectPool(config);*/
    }

    public void saveTask(String tenantId, String taskId, JSONObject data) {
        objectPool.checkout(tenantId, null);
        MultiAttrsObjectPatch patch = objectPool.save(taskId);
        TaskSchema.inflateSavePatch(patch, data);
        try {
            patch.commit();
        } catch (Exception e) {
            logger.error("failed to save task{id={}}, reason: {}", taskId, e.getMessage());
        }
    }

    public void updateTaskTitle(String tenantId, String taskId, String newTitle) {
        objectPool.checkout(tenantId, null);
        MultiAttrsObjectPatch patch = objectPool.update(taskId);
        patch.set("title", newTitle);
        try {
            patch.commit();
        } catch (Exception e) {
            logger.error("failed to update title of task{id={}}, reason: {}", taskId, e.getMessage());
        }
    }

    public void deleteTask(String tenantId, String taskId) {
        objectPool.checkout(tenantId, null);
        try {
            objectPool.delete(taskId);
        } catch (Exception e) {
            logger.error("failed to delete task{id={}}, reason: {}", taskId, e.getMessage());
        }
    }

    public TaskSchema getTask(String tenantId, String taskId) {
        objectPool.checkout(tenantId, null);
        try {
            MultiAttrsObject result = objectPool.get(taskId);
            if (result == null) {
                return null;
            }
            TaskSchema task = new TaskSchema();
            task.inflate(result);
            return task;
        } catch (Exception e) {
            logger.error("failed to get task{id={}}, reason: {}", taskId, e.getMessage());
        }
        return null;
    }

    public QueryBuilder createQueryBuilder(String tenantId) {
        return objectPool.createQueryBuilder().from(tenantId);
    }
}

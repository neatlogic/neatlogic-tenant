package codedriver.module.tenant.api.test;

import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;


public class EsSearchApi extends PrivateApiComponentBase {
    private static final Logger logger = LoggerFactory.getLogger(EsSearchApi.class);

    @Override
    public String getToken() {
        return "test/es/search";
    }

    @Override
    public String getName() {
        return "测试es查询接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Autowired
    private ObjectPoolService objectPoolService;

    @Description(desc = "测试es查询接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        /*Integer status = jsonObj.getInteger("status");
        List<String> tags = Util.getList(jsonObj, "tags");
        String title = jsonObj.getString("title");
        String handler = jsonObj.getString("handler");
        Integer skip = jsonObj.getInteger("skip");
        Integer size = jsonObj.getInteger("size");

        QueryBuilder.ConditionBuilder cond = null;
        if (status != null) {
            cond = attr("status").eq(status);
        }
        if (!tags.isEmpty()) {
            cond = cond == null ? attr("tags").containsAny(tags) : cond.and().attr("tags").containsAny(tags);
        }
        if (title != null && !StringUtils.isBlank(title)) {
            cond = cond == null ? attr("title").contains(title) : cond.and().attr("title").contains(title);
        }
        if (handler != null && !StringUtils.isBlank(handler)) {
            cond = cond == null ? attr("handler").eq(handler) : cond.and().attr("handler").eq(handler);
        }

        QueryBuilder builder = objectPoolService.createQueryBuilder(TenantContext.get().getTenantUuid())
//                 .select("title", "status", "created_at")
                .orderBy("created_at", false)
                .limit(skip == null ? 0 : skip, size == null ? 100 : size);
        if (cond != null) {
            builder.where(cond);
        }
        QueryResult result = builder.build().execute();

        logger.debug("page {}/{} size at '{}' is {}", result.getSkip(), result.getTotal(), result.getObjectSpace(), result.getData().size());
        if (!result.getData().isEmpty()) {
            Map<String, String> titles = new HashMap<>(result.getData().size());
            for (MultiAttrsObject el : result.getData()) {
                titles.put(el.getId(), el.getString("title"));
            }
            return titles;
        }*/
        return Collections.emptyMap();
    }

}

package neatlogic.module.tenant.api.test;

import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class EsSqlApi extends PrivateApiComponentBase {
    private static final Logger logger = LoggerFactory.getLogger(EsSearchApi.class);

    @Override
    public String getToken() {
        return "test/es/sql";
    }

    @Override
    public String getName() {
        return "测试es SQL查询接口";
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
        /*String sql = jsonObj.getString("sql");
        if (sql == null || "".equals((sql = sql.trim()))) {
            return Collections.emptyMap();
        }
        QueryParser parser = objectPoolService.createQueryParser();

        try {
            MultiAttrsQuery query = parser.parse(sql);
            QueryResult result = query.execute();

            logger.debug("page {}/{} size at '{}' is {}", result.getSkip(), result.getTotal(), result.getObjectSpace(), result.getData().size());
            if (!result.getData().isEmpty()) {
                Map<String, String> titles = new HashMap<>(result.getData().size());
                for (MultiAttrsObject el : result.getData()) {
                    titles.put(el.getId(), el.getString("title"));
                }
                return titles;
            }

            return Collections.emptyMap();
        } catch (QuerySyntaxException e) {
            logger.debug("invalid sql: {}", sql);
            return e.getMessage();
        } catch (ObjectPoolDataException e) {
            logger.debug("data exception", e);
            return e.getMessage();
        }*/
        return null;
    }

}


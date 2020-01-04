package codedriver.module.tenant.api.test;

import static com.techsure.multiattrsearch.query.QueryBuilder.attr;
import static com.techsure.multiattrsearch.query.QueryBuilder.not;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.techsure.multiattrsearch.MultiAttrsObject;
import com.techsure.multiattrsearch.MultiAttrsQuery;
import com.techsure.multiattrsearch.query.QueryResult;

import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.common.config.Config;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.core.ApiComponentBase;


@Service
public class esSearchApi extends ApiComponentBase {

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


	@Description(desc = "测试es查询接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		MultiAttrsQuery query = Config.pool.createQueryBuilder()
		        // .select("not_exists", "title", "status")
		        .from(TenantContext.get().getTenantUuid())
		        // 只有 like 操作符会使用全文搜索
		        .where(attr("title").like("你好")
		                .and()
		                .attr("tags").contains("主机")
		                /*.or(	
		                		attr("status").between(1, 4)
		                		//.and(not(attr("enabled").eq(false)))
		                	)*/
		        )
		        .orderBy("create_time", false)
		        .limit(0, 20)
		        .build();
		QueryResult result = query.execute();
		//System.out.println(String.format("page %d/%d at '%s'", result.getSkip(), result.getTotal(), result.getObjectSpace()));

		for (MultiAttrsObject el : result.getData()) {
		    System.out.println(el.getId() + " => " + el.getString("title"));
		}

		return result.getData();
	}

}

/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.globalsearch.document;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@OperationType(type = OperationTypeEnum.DELETE)
public class DocumentRebuildApi extends PrivateApiComponentBase {
	Logger logger = LoggerFactory.getLogger(DocumentRebuildApi.class);

//	@Autowired
//	private GlobalSearchService globalSearchService;

	@Override
	public String getToken() {
		return "globalsearch/document/rebuild";
	}

	@Override
	public String getName() {
		return "全局搜索文档索引重建接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({@Param(name = "type", type = ApiParamType.STRING, isRequired = true, desc = "关键字"), @Param(name = "isRebuildAll", type = ApiParamType.BOOLEAN, isRequired = true, desc = "是否全部重建，false代表增量重建，true代表全量重建")})
	@Description(desc = "全局搜索文档索引重建接口")
	@Override
	public Object myDoService(JSONObject jsonObj) {
		String type = jsonObj.getString("type");
		Boolean isRebuildAll = jsonObj.getBoolean("isRebuildAll");
		/*DocumentRebuildHandler thread = new DocumentRebuildHandler() {
			@Override
			public void execute() {
				String oldName = Thread.currentThread().getName();
				Thread.currentThread().setName("GLOBALSEARCH-REBUILDDOCUMENT-" + type);
				try {
					IDocumentHandler<?> handler = DocumentHandlerFactory.getComponent(type);
					if (handler != null) {
						handler.rebuildDocument(isRebuildAll);
					} else {
						throw new RuntimeException("找不到索引处理器：" + type);
					}
				} catch (Exception ex) {
					logger.error(ex.getMessage(), ex);
				} finally {
					Thread.currentThread().setName(oldName);
				}
			}
		};
		CachedThreadPool.execute(thread);*/
		return null;
	}

}

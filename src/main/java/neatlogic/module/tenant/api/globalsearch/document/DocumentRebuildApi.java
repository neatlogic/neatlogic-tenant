/*Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

package neatlogic.module.tenant.api.globalsearch.document;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
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

/*
 * Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

/*
 *
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 */

package neatlogic.module.tenant.api.globalsearch.document;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.NoAuth;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@AuthAction(action = NoAuth.class)
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

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

package neatlogic.module.tenant.api.notify;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthActionChecker;
import neatlogic.framework.notify.core.INotifyPolicyHandler;
import neatlogic.framework.notify.core.NotifyPolicyHandlerFactory;
import neatlogic.framework.notify.dto.NotifyTreeVo;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

//@Service
@Deprecated
@OperationType(type = OperationTypeEnum.SEARCH)
public class NotifyPolicyHandlerTreeApi extends PrivateApiComponentBase {

	@Override
	public String getToken() {
		return "notify/policy/handler/tree";
	}

	@Override
	public String getName() {
		return "通知策略分类树接口";
	}

	@Override
	public String getConfig() {
		return null;
	}
	
	@Output({
		@Param(explode = NotifyTreeVo[].class, desc = "通知策略分类树")
	})
	@Description(desc = "通知策略分类树接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
//		List<NotifyTreeVo> notifyPolicyTreeVoList = copy(NotifyPolicyHandlerFactory.getNotifyPolicyTreeVoList());
//		authActionChecker(notifyPolicyTreeVoList);
//		if(CollectionUtils.isEmpty(notifyPolicyTreeVoList)){
//			throw new PermissionDeniedException();
//		}
//		return notifyPolicyTreeVoList;
		return null;
	}

	/**
	 * @Description: 检查权限，没有权限的移除
	 * @Author: linbq
	 * @Date: 2021/3/8 18:22
	 * @Params:[notifyPolicyTreeVoList]
	 * @Returns:void
	 **/
	private void authActionChecker(List<NotifyTreeVo> notifyPolicyTreeVoList){
		Iterator<NotifyTreeVo> iterator = notifyPolicyTreeVoList.iterator();
		while (iterator.hasNext()){
			NotifyTreeVo next = iterator.next();
			INotifyPolicyHandler handler = NotifyPolicyHandlerFactory.getHandler(next.getUuid());
			if(handler == null){
				List<NotifyTreeVo> children = next.getChildren();
				if(CollectionUtils.isNotEmpty(children)){
					authActionChecker(children);
					if(CollectionUtils.isEmpty(children)){
						iterator.remove();
					}
				}else{
					iterator.remove();
				}
			}else{
				/** 通知策略与权限绑定，例如没有流程管理权限则无法编辑流程及流程步骤通知策略 */
				if(!AuthActionChecker.check(handler.getAuthName())){
					iterator.remove();
				}
			}
		}
	}
	/**
	 * @Description: 深拷贝
	 * @Author: linbq
	 * @Date: 2021/3/8 18:22
	 * @Params:[notifyTreeVoList]
	 * @Returns:java.util.List<neatlogic.framework.notify.dto.NotifyTreeVo>
	 **/
	private List<NotifyTreeVo> copy(List<NotifyTreeVo> notifyTreeVoList){
		List<NotifyTreeVo> resultList = new ArrayList<>(notifyTreeVoList.size());
		for(NotifyTreeVo notifyTreeVo : notifyTreeVoList){
			NotifyTreeVo newNotifyTreeVo = new NotifyTreeVo(notifyTreeVo.getUuid(), notifyTreeVo.getName());
			resultList.add(newNotifyTreeVo);
			if(notifyTreeVo.getChildren() != null){
				newNotifyTreeVo.setChildren(copy(notifyTreeVo.getChildren()));
			}
		}
		return resultList;
	}
}

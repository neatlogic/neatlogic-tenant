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

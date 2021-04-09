package codedriver.module.tenant.api.notify;

import codedriver.framework.auth.core.AuthActionChecker;
import codedriver.framework.exception.type.PermissionDeniedException;
import codedriver.framework.notify.core.INotifyPolicyHandler;
import codedriver.framework.notify.core.NotifyPolicyHandlerFactory;
import codedriver.framework.notify.dto.NotifyTreeVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @Title: NotifyPolicyHandlerTreeApi
 * @Package codedriver.module.tenant.api.notify
 * @Description: 通知策略分类树接口
 * @Author: linbq
 * @Date: 2021/2/24 12:03
 * Copyright(c) 2021 TechSureCo.,Ltd.AllRightsReserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 **/
@Service
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
		List<NotifyTreeVo> notifyPolicyTreeVoList = copy(NotifyPolicyHandlerFactory.getNotifyPolicyTreeVoList());
		authActionChecker(notifyPolicyTreeVoList);
		if(CollectionUtils.isEmpty(notifyPolicyTreeVoList)){
			throw new PermissionDeniedException();
		}
		return notifyPolicyTreeVoList;
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
	 * @Returns:java.util.List<codedriver.framework.notify.dto.NotifyTreeVo>
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

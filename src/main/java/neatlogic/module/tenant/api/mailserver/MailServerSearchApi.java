/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package neatlogic.module.tenant.api.mailserver;

import java.util.List;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.common.util.PageUtil;
import neatlogic.framework.dao.mapper.MailServerMapper;
import neatlogic.framework.dto.MailServerVo;
@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class MailServerSearchApi extends PrivateApiComponentBase {

	@Autowired
	private MailServerMapper mailServerMapper;

	@Override
	public String getToken() {
		return "mailserver/search";
	}

	@Override
	public String getName() {
		return "邮件服务器信息列表查询接口";
	}

	@Override
	public String getConfig() {
		return null;
	}
	
	@Input({
		@Param(name = "keyword", type = ApiParamType.STRING, xss = true, desc = "关键字，名称模糊查询"),
		@Param(name = "currentPage", type = ApiParamType.INTEGER, desc="当前页码，默认值1"),
		@Param(name = "pageSize", type = ApiParamType.INTEGER, desc="页大小，默认值10"),
		@Param(name = "needPage", type = ApiParamType.BOOLEAN, desc="是否分页，默认值true")
	})
	@Output({
		@Param(explode = BasePageVo.class),
		@Param(name = "tbodyList", explode = MailServerVo[].class, desc = "邮件服务器信息列表")
	})
	@Description(desc = "邮件服务器信息列表查询接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		MailServerVo mailServerVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<MailServerVo>() {});
		JSONObject resultObj = new JSONObject();
		if(mailServerVo.getNeedPage()) {
			int rowNum = mailServerMapper.searchMailServerCount(mailServerVo);
			int pageCount = PageUtil.getPageCount(rowNum, mailServerVo.getPageSize());
			mailServerVo.setPageCount(pageCount);
			mailServerVo.setRowNum(rowNum);
			resultObj.put("currentPage", mailServerVo.getCurrentPage());
			resultObj.put("pageSize", mailServerVo.getPageSize());
			resultObj.put("pageCount", pageCount);
			resultObj.put("rowNum", rowNum);
		}
		List<MailServerVo> MailServerList = mailServerMapper.searchMailServerList(mailServerVo);
		resultObj.put("tbodyList", MailServerList);
		return resultObj;
	}

}

package codedriver.module.tenant.api.mailserver;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.dao.mapper.MailServerMapper;
import codedriver.framework.dto.MailServerVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
@Service
public class MailServerSearchApi extends ApiComponentBase {

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

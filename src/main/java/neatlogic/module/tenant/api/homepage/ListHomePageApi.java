package neatlogic.module.tenant.api.homepage;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.HOME_PAGE_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.dto.HomePageVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.TableResultUtil;
import neatlogic.framework.dao.mapper.HomePageMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
@AuthAction(action = HOME_PAGE_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ListHomePageApi extends PrivateApiComponentBase {

	@Resource
	private HomePageMapper homePageMapper;

	@Override
	public String getToken() {
		return "homepage/list";
	}

	@Override
	public String getName() {
		return "首页配置列表";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "keyword", type = ApiParamType.STRING, desc = "common.keyword"),
		@Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "common.pagesize"),
		@Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "common.currentpage")
		})
	@Output({
		@Param(explode = BasePageVo.class),
		@Param(name="tbodyList",explode= HomePageVo[].class,desc="common.tbodylist")
	})
	@Description(desc = "nmpac.channelsearchapi.getname")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		BasePageVo basePageVo = jsonObj.toJavaObject(BasePageVo.class);
		int rowNum = homePageMapper.getHomePageCount(basePageVo);
		if (rowNum > 0) {
			basePageVo.setRowNum(rowNum);
			List<HomePageVo> tbodyList = homePageMapper.getHomePageList(basePageVo);
			return TableResultUtil.getResult(tbodyList, basePageVo);
		}
		return TableResultUtil.getResult(new ArrayList<>(), basePageVo);
	}

}

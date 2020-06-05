package codedriver.module.tenant.api.auth;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.auth.core.AuthBase;
import codedriver.framework.auth.core.AuthFactory;
import codedriver.framework.common.util.ModuleUtil;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.ModuleGroupVo;
import codedriver.framework.dto.UserAuthVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.tenant.auth.label.DASHBOARD_MODIFY;

/**
 * @program: codedriver
 * @description:
 * @create: 2020-06-04
 **/
@Service
public class AuthModuleGetApi extends ApiComponentBase {
	
	@Autowired
	UserMapper userMapper;
    @Override
    public String getToken() {
        return "auth/module/get";
    }

    @Override
    public String getName() {
        return "获取用户模块对应权限接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input( {
            
    })

    @Output({
            @Param( explode = ModuleGroupVo.class),
            @Param( name = "authList[].authDisplayName", desc = "权限名", type = ApiParamType.STRING),
            @Param( name = "authList[].authGroup", desc = "模块分组", type = ApiParamType.STRING),
            @Param( name = "authList[].authIntroduction", desc = "权限介绍", type = ApiParamType.STRING),
            @Param( name = "authList[].authName", desc = "权限", type = ApiParamType.STRING),
    })

    @Description(desc = "根据用户获取模块以及对应的权限列表")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONArray retrunArray = new JSONArray();
        Set<String> authGroupSet = new HashSet<String>();
        Set<String> authSet = new HashSet<String>();
        //获取用户权限
        List<UserAuthVo>  userAuthList = userMapper.searchUserAllAuthByUserAuth(new UserAuthVo(UserContext.get().getUserUuid()));
        for(UserAuthVo userAuth:userAuthList) {
        	authGroupSet.add(userAuth.getAuthGroup());
        	authSet.add(userAuth.getAuth());
        }
        List<Object> dashboardAuthlist = new ArrayList<Object>();
        Map<String, List<AuthBase>>  authModuleMap = AuthFactory.getAuthGroupMap();
        for(Entry<String, List<AuthBase>> entry : authModuleMap.entrySet()) {
        	if(!authGroupSet.contains(entry.getKey())) {
        		continue;
        	}
        	ModuleGroupVo moduleGroup = ModuleUtil.getModuleGroup(entry.getKey());
        	JSONObject moduleGroupJson = new JSONObject();
        	moduleGroupJson.put("group", moduleGroup.getGroup());
        	moduleGroupJson.put("groupName", moduleGroup.getGroupName());
        	moduleGroupJson.put("groupSort", moduleGroup.getGroupSort());
        	moduleGroupJson.put("description", ModuleUtil.getModuleGroup(moduleGroup.getGroup()).getGroupDescription());
        	List<AuthBase> authBaseList = new ArrayList<AuthBase>();
        	Iterator<AuthBase> authIterator = entry.getValue().iterator();
        	while(authIterator.hasNext()) {
        		AuthBase tmpAuth = authIterator.next();
        		if(authSet.contains(tmpAuth.getAuthName())) {
        			authBaseList.add(tmpAuth);
        		}
        		if(tmpAuth.getAuthName().equals(DASHBOARD_MODIFY.class.getSimpleName())) {
        			dashboardAuthlist.add(JSON.toJSON(tmpAuth));
        		}
        	}
        	
        	moduleGroupJson.put("authList", authBaseList);
        	
        	retrunArray.add(moduleGroupJson);
        }
        //补充dashboard
    	JSONObject moduleGroupJson = new JSONObject();
    	moduleGroupJson.put("group", "dashboard");
    	moduleGroupJson.put("groupName", "仪表板");
    	moduleGroupJson.put("groupSort", 0);
    	moduleGroupJson.put("description", "图形化编辑页面，轻松实现数据可视化交互");
    	moduleGroupJson.put("authList", dashboardAuthlist);
    	retrunArray.add(moduleGroupJson);
    	retrunArray.sort(Comparator.comparing(obj-> ((JSONObject) obj).getInteger("groupSort")));
        return retrunArray;
    }
}

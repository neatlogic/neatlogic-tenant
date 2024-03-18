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

package neatlogic.module.tenant.schedule.plugin;

import neatlogic.framework.dao.mapper.TeamMapper;
import neatlogic.framework.dto.TeamVo;
import neatlogic.framework.exception.core.ApiRuntimeException;
import neatlogic.framework.exception.type.ParamNotExistsException;
import neatlogic.framework.lrcode.LRCodeManager;
import neatlogic.framework.scheduler.annotation.Param;
import neatlogic.framework.scheduler.annotation.Prop;
import neatlogic.framework.scheduler.core.PublicJobBase;
import neatlogic.framework.scheduler.dto.JobObject;
import org.apache.commons.lang3.StringUtils;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.*;
import java.util.*;

@Component
@DisallowConcurrentExecution
public class SyncLdapTeamSchedule extends PublicJobBase {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private TeamMapper teamMapper;

    @Override
    public String getName() {
        return "全量同步LDAP的组织架构";
    }

    @Prop({
            @Param(name = "ldapUrl", controlType = "text", description = "ldap地址", required = true, sort = 0, help = "例如：ldap://192.168.1.99"),
            @Param(name = "userDn", controlType = "text", description = "同步账号dn", required = true, sort = 1, help = "例如：cn=Manager,dc=neatlogic,dc=com"),
            @Param(name = "userSecret", controlType = "text", description = "登录密码", required = true, sort = 2, help = "例如：123456"),
            @Param(name = "baseDN", controlType = "text", description = "基准DN", required = true, sort = 3, help = "例如：dc=neatlogic,dc=com"),
            @Param(name = "searchFilter", controlType = "text", description = "过滤条件", required = true, sort = 4, help = "将满足该过滤条件的ou，同步到系统分组"),
            @Param(name = "rootParentUUid", controlType = "text", description = "根节点", required = false, sort = 5, help = "默认：0"),
            @Param(name = "uuid", controlType = "text", description = "分组UUID", required = true, sort = 6, help = "指定分组主键映射字段"),
    })
    @Override
    public void executeInternal(JobExecutionContext context, JobObject jobObject) throws Exception {

        String ldapUrl = getPropValue(jobObject, "ldapUrl");
        String userDn = getPropValue(jobObject, "userDn");
        String userSecret = getPropValue(jobObject, "userSecret");
        String baseDN = getPropValue(jobObject, "baseDN"); //从xx顶层目录快速查找
        String searchFilter = getPropValue(jobObject, "searchFilter"); //LDAP搜索过滤器类
        String rootParentUUid = getPropValue(jobObject, "rootParentUUid");
        if (StringUtils.isBlank(rootParentUUid)) {
            rootParentUUid = TeamVo.ROOT_UUID;
        }

        if (StringUtils.isBlank(searchFilter)) {
            throw new ParamNotExistsException("过滤条件");
        }
        if (StringUtils.isBlank(ldapUrl)) {
            throw new ParamNotExistsException("ldap地址");
        }
        if (StringUtils.isBlank(userDn)) {
            throw new ParamNotExistsException("同步账号dn");
        }
        if (StringUtils.isBlank(userSecret)) {
            throw new ParamNotExistsException("登录密码");
        }
        //parent不存在
        if (teamMapper.getTeamByUuid(rootParentUUid) == null) {
            rootParentUUid = TeamVo.ROOT_UUID;
        }

        int pageSize = 1000;//分页查询大小
        Hashtable<String, String> HashEnv = new Hashtable<>();
        HashEnv.put(Context.SECURITY_AUTHENTICATION, "simple"); // LDAP访问安全级别
        HashEnv.put(Context.SECURITY_PRINCIPAL, userDn);
        HashEnv.put(Context.SECURITY_CREDENTIALS, userSecret);
        HashEnv.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        HashEnv.put(Context.PROVIDER_URL, ldapUrl);
        HashEnv.put(Context.BATCHSIZE, pageSize + "");
        List<String> returnedAttList = new ArrayList<>();
        String _uuid = getPropValue(jobObject, "uuid");
        if (StringUtils.isNotBlank(_uuid)) {
            returnedAttList.add(_uuid);
        }
        LdapContext ctx = new InitialLdapContext(HashEnv, null);
        // 搜索控制器
        SearchControls searchCtls = new SearchControls();
        // 创建搜索控制器
        searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        // 对象的每个属性名
        String[] returnedAtts = new String[returnedAttList.size()];
        returnedAttList.toArray(returnedAtts);
        searchCtls.setReturningAttributes(returnedAtts);
        //开启分页查询
        ctx.setRequestControls(new Control[]{new PagedResultsControl(pageSize, Control.CRITICAL)});
        Date lcd = new Date();
        byte[] cookie = null;
        int totalResults = 0;
        Map<String, String> uuidMap = new HashMap<>();
        String baseDnName = "";
        String msg = "baseDN=" + baseDN;
        msg = msg + ", filter=" + searchFilter;
        msg = msg + ", searchScope=SUBTREE_SCOPE";
        msg = msg + ", returningAttributes=" + String.join("、", returnedAttList);
        do {
            // 根据设置的域节点、过滤器类和搜索控制器搜索LDAP得到结果
            NamingEnumeration answer = ctx.search(baseDN, searchFilter, searchCtls);
            while (answer.hasMoreElements()) {
                totalResults++;
                SearchResult sr = (SearchResult) answer.next();
                String uuid = null, teamName = null, parentName = null, upwardNamePath = null;
                String fullName = sr.getNameInNamespace();
                Attributes Attrs = sr.getAttributes();
                if (Attrs != null) {
                    try {
                        for (NamingEnumeration ne = Attrs.getAll(); ne.hasMore(); ) {
                            Attribute Attr = (Attribute) ne.next();
                            String attrName = Attr.getID();
                            String attrValue = null;
                            for (NamingEnumeration e = Attr.getAll(); e.hasMore(); ) {
                                attrValue = e.next().toString();
                            }
                            if (attrName.equals(_uuid)) {
                                uuid = attrValue.replace("-", "");
                            }
                        }
                    } catch (NamingException e) {
                        logger.error("[Sync Ldap Team Error]:" + e.getMessage(), e);
                    }
                }
                if (StringUtils.isBlank(uuid)) {
                    msg = msg + ", attribute " + _uuid + " does not exist";
                    throw new ApiRuntimeException(msg);
                }
                if (Objects.equals(baseDN, fullName)) {
                    teamName = getTeamName(fullName);
                    parentName = "";
                    upwardNamePath = teamName;
                    baseDnName = teamName;
                } else {
                    String pathName = fullName.replace(baseDN, "");
                    teamName = getTeamName(pathName);
                    parentName = getParentName(pathName);
                    if (StringUtils.isBlank(parentName)) {
                        parentName = baseDnName;
                    }
                    upwardNamePath = getUpwardTeamName(pathName, baseDnName);
                }

                uuidMap.put(teamName, uuid);
                String upwardUuidPath = getUpwardUuidPath(uuidMap, upwardNamePath);
                TeamVo teamVo = new TeamVo();
                teamVo.setSource("ldap");
                teamVo.setUuid(uuid);
                teamVo.setName(teamName);
                teamVo.setUpwardNamePath(upwardNamePath);
                teamVo.setUpwardUuidPath(upwardUuidPath);
                String parentUuid = uuidMap.get(parentName);
                parentUuid = parentUuid == null ? rootParentUUid : parentUuid;
                teamVo.setParentUuid(parentUuid);
                teamVo.setLcd(lcd);
                this.teamMapper.insertTeamForLdap(teamVo);
            }
            cookie = parseControls(ctx.getResponseControls());
            ctx.setRequestControls(new Control[]{new PagedResultsControl(pageSize, cookie, Control.CRITICAL)});
        } while ((cookie != null) && (cookie.length != 0));
        ctx.close();
        if (totalResults == 0) {
            msg = msg + ", searchResultCount=0";
            throw new ApiRuntimeException(msg);
        }
        //数据清理
        this.teamMapper.updateTeamIsDeleteBySourceAndLcd("ldap", lcd);
        //重算左右编码
        LRCodeManager.rebuildLeftRightCode("team", "uuid", "parent_uuid", "`is_delete` = 0");
    }

    private String getTeamName(String pathName) {
        String[] split = pathName.split(",");
        if (split.length > 0) {
            String[] names = split[0].split("=");
            if (names.length > 1) {
                return names[1];
            }
        }
        return null;
    }

    private String getParentName(String pathName) {
        String[] split = pathName.split(",");
        if (split.length > 1) {
            String[] names = split[1].split("=");
            if (names.length > 1) {
                return names[1];
            }
        }
        return null;
    }

    private String getUpwardTeamName(String pathName, String baseDnName) {
        List<String> upwardTeamNameList = new ArrayList<>();
        if (StringUtils.isNotBlank(baseDnName)) {
            upwardTeamNameList.add(baseDnName);
        }
        String[] split = pathName.split(",");
        for (int i = split.length - 1; i >= 0; i--) {
            String str = split[i];
            if (StringUtils.isNotBlank(str) && str.contains("=")) {
                String[] names = str.split("=");
                if (names.length > 1) {
                    upwardTeamNameList.add(names[1]);
                }
            }
        }
        return String.join("/", upwardTeamNameList);
    }

    private static byte[] parseControls(Control[] controls) throws NamingException {
        byte[] cookie = null;
        if (controls != null) {
            for (int i = 0; i < controls.length; i++) {
                if (controls[i] instanceof PagedResultsResponseControl) {
                    PagedResultsResponseControl prrc = (PagedResultsResponseControl) controls[i];
                    cookie = prrc.getCookie();
                }
            }
        }
        return (cookie == null) ? new byte[0] : cookie;
    }

    private String getUpwardUuidPath(Map<String, String> uuidMap, String upwardNamePath) {
        List<String> upwardTeamUuidList = new ArrayList<>();
        if (StringUtils.isNotBlank(upwardNamePath)) {
            String[] namePaths = upwardNamePath.split("/");
            for (String name : namePaths) {
                if (StringUtils.isNotBlank(name) && uuidMap.containsKey(name)) {
                    upwardTeamUuidList.add(uuidMap.get(name));
                }
            }
        }
        return String.join(",", upwardTeamUuidList);
    }

    /**
     * 获取参数
     *
     * @param jobObject
     * @param key
     * @return
     */
    private static String getPropValue(JobObject jobObject, String key) {
        Object value = jobObject.getProp(key);
        if (value == null) {
            return "";
        } else {
            return value.toString();
        }
    }
}

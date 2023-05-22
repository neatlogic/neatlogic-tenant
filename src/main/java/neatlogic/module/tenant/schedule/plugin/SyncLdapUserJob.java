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

package neatlogic.module.tenant.schedule.plugin;

import neatlogic.framework.dao.mapper.RoleMapper;
import neatlogic.framework.dao.mapper.TeamMapper;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dto.TeamVo;
import neatlogic.framework.dto.UserVo;
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
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

@Component
@DisallowConcurrentExecution
public class SyncLdapUserJob extends PublicJobBase {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private TeamMapper teamMapper;

    @Resource
    private UserMapper userMapper ;

    @Resource
    private RoleMapper roleMapper;

    @Override
    public String getName() {
        return "同步LDAP的用户信息";
    }

    @Override
    public void reloadJob(JobObject jobObject) {

    }

    @Override
    public void initJob(String tenantUuid) {

    }

    @Prop({
            @Param(name = "ldapUrl", controlType = "text", description = "ldap地址", required = true),
            @Param(name = "userDn", controlType = "text", description = "同步账号dn", required = true),
            @Param(name = "userSecret", controlType = "text", description = "登录密码", required = true),
            @Param(name = "searchBase", controlType = "text", description = "从指定目录开始查找", required = true),
            @Param(name = "searchFilter", controlType = "text", description = "过滤类型，默认：objectclass=organizationalUnit",required=false),
            @Param(name = "defaultRole", controlType = "text", description = "默认角色,多个,分隔，如：R_A,R_B",required=false),
    })
    @Override
    public void executeInternal(JobExecutionContext context, JobObject jobObject) throws Exception {

        String batchSize = "20000"; //分页数量，默认是1000
        String ldapUrl = getPropValue(jobObject, "ldapUrl");
        String userDn = getPropValue(jobObject, "userDn");
        String userSecret = getPropValue(jobObject, "userSecret");
        String searchBase = getPropValue(jobObject, "searchBase"); //从xx顶层目录快速查找
        String searchFilter = getPropValue(jobObject, "searchFilter"); //LDAP搜索过滤器类
        String defaultRole = getPropValue(jobObject, "defaultRole");
        if (StringUtils.isBlank(searchFilter)) {
            searchFilter = "objectclass=person";
        }

        if (StringUtils.isNotBlank(ldapUrl) && StringUtils.isNotBlank(userDn) && StringUtils.isNotBlank(userSecret)) {
            Hashtable<String, String> HashEnv = new Hashtable<String, String>();
            HashEnv.put(Context.SECURITY_AUTHENTICATION, "simple"); // LDAP访问安全级别
            HashEnv.put(Context.SECURITY_PRINCIPAL, userDn);
            HashEnv.put(Context.SECURITY_CREDENTIALS, userSecret);
            HashEnv.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
            HashEnv.put(Context.PROVIDER_URL, ldapUrl);
            HashEnv.put(Context.BATCHSIZE, batchSize);
            try {
                LdapContext ctx = new InitialLdapContext(HashEnv, null);
                // 搜索控制器
                SearchControls searchCtls = new SearchControls();
                // 创建搜索控制器
                searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
                // 对象的每个属性名
                String[] returnedAtts = { "entryUUID","uid","sAMAccountName","cn","mobile","mail" ,"telephoneNumber","description"};
                searchCtls.setReturningAttributes(returnedAtts);

                // 根据设置的域节点、过滤器类和搜索控制器搜索LDAP得到结果
                NamingEnumeration answer = ctx.search(searchBase, searchFilter, searchCtls);
                while (answer.hasMoreElements()) {
                    SearchResult sr = (SearchResult) answer.next();
                    String fullName = sr.getNameInNamespace();
                    String parentName = getParentName(fullName);
                    String upwardTeamName = getUpwardTeamName(fullName);
                    String uuid=null , userId = null , userName = null , email =null ,phone = null ;
                    Attributes Attrs = sr.getAttributes();
                    if (Attrs != null) {
                        try {
                            for (NamingEnumeration ne = Attrs.getAll(); ne.hasMore(); ) {
                                Attribute Attr = (Attribute) ne.next();
                                String attrName = Attr.getID().toString();
                                String attrValue = null;
                                for (NamingEnumeration e = Attr.getAll(); e.hasMore(); ) {
                                    attrValue = e.next().toString();
                                }
                                if (attrName.equals("uid") || attrName.equals("sAMAccountName")) {
                                    userId = attrValue;
                                }else if(attrName.equals("name") || attrName.equals("cn")){
                                    userName = attrValue;
                                }else if(attrName.equals("email") || attrName.equals("mail")){
                                    email = attrValue;
                                }else if(attrName.equals("telephoneNumber")){
                                    phone = attrValue;
                                }else if(attrName.equals("entryUUID")){
                                    uuid = attrValue.replace("-", "");
                                }
                            }
                        } catch (NamingException e) {
                            logger.error("[Sync Ldap User Error]:" + e.getMessage());
                        }
                    }

                    if(StringUtils.isNotBlank(userId)){

                        String teamUuid = null ;
                        TeamVo teamVo = new TeamVo();
                        teamVo.setName(parentName);
                        teamVo.setUpwardNamePath(upwardTeamName);
                        List<String>  teamUUidList = this.teamMapper.getTeamUUIDbyNameAndUpwardNamePath(teamVo);
                        if(teamUUidList != null && teamUUidList.size() > 0 ){
                            teamUuid = teamUUidList.get(0);
                        }

                        UserVo userVo = new UserVo();
                        userVo.setUuid(uuid);
                        userVo.setUserId(userId);
                        userVo.setUserName(userName);
                        userVo.setEmail(email);
                        userVo.setPhone(phone);
                        if(this.userMapper.checkUserIsExists(uuid) == 0 ){
                            this.userMapper.insertUser(userVo);
                            if(StringUtils.isNotBlank(teamUuid)){
                                this.userMapper.insertUserTeam(uuid , teamUuid);
                            }
                            //新加追加默認角色
                            if(StringUtils.isNotBlank(defaultRole)){
                                String[] roles = defaultRole.split(",");
                                for (String role: roles) {
                                    if(StringUtils.isNotBlank(role)){
                                        List<String> roleList = this.roleMapper.getRoleUuidByName(role);
                                        if(roleList != null && roleList.size() > 0){
                                            this.userMapper.insertUserRole(uuid , roleList.get(0));
                                        }
                                    }
                                }
                            }
                        }else{
                            this.userMapper.updateUser(userVo);
                            this.userMapper.deleteUserTeamByUserUuid(uuid);
                            if(StringUtils.isNotBlank(teamUuid)){
                                this.userMapper.insertUserTeam(uuid , teamUuid);
                            }
                        }
                    }
                }
                ctx.close();
            } catch (NamingException e) {
                logger.error("[Sync Ldap User Error]:" + e.getMessage());
            }
        }else{
            logger.error("[Sync Ldap User Error]: Incomplete Plugin parameters.");
        }
    }

    /**
     * 获取参数
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

    private static String getTeamName(String name){
        if(StringUtils.isBlank(name)){
            return "";
        }
        return name.split("ou=")[1].split(",")[0];
    }

    private static String getParentName(String fullName) {
        if (StringUtils.isBlank(fullName)) {
            return "";
        }
        String parentFullName = fullName.substring(fullName.indexOf(",") + 1, fullName.length());
        return getTeamName(parentFullName);
    }

    private static String getUpwardTeamName(String fullName){
        if(StringUtils.isNotBlank(fullName)){
            String[] names = fullName.split(",");
            List<String> upwardTeamNameList = new ArrayList<>();
            for (String name: names) {
                if(name.startsWith("ou=")){
                    String[] ou = name.split("=");
                    if(!"groups".equals(ou[1])){
                        upwardTeamNameList.add(ou[1]);
                    }
                }
            }
            Collections.reverse(upwardTeamNameList);
            return String.join("/", upwardTeamNameList);
        }
        return null ;
    }

}

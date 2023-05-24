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
import neatlogic.framework.dto.RoleUserVo;
import neatlogic.framework.dto.TeamUserVo;
import neatlogic.framework.dto.TeamVo;
import neatlogic.framework.dto.UserVo;
import neatlogic.framework.scheduler.annotation.Param;
import neatlogic.framework.scheduler.annotation.Prop;
import neatlogic.framework.scheduler.core.PublicJobBase;
import neatlogic.framework.scheduler.dto.JobAuditVo;
import neatlogic.framework.scheduler.dto.JobObject;
import neatlogic.framework.transaction.util.TransactionUtil;
import org.apache.commons.lang3.StringUtils;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

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
import java.util.*;

@Component
@DisallowConcurrentExecution
public class SyncLdapUserSchedule extends PublicJobBase {

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
            @Param(name = "ldapUrl", controlType = "text", description = "ldap地址", required = true,sort=0),
            @Param(name = "userDn", controlType = "text", description = "同步账号dn", required = true,sort=1),
            @Param(name = "userSecret", controlType = "text", description = "登录密码", required = true,sort=2),
            @Param(name = "searchBase", controlType = "text", description = "从指定目录开始查找", required = true,sort=3),
            @Param(name = "searchFilter", controlType = "text", description = "过滤类型，默认:objectclass=person",required=false,sort=4),
            @Param(name = "defaultRole", controlType = "text", description = "默认角色,如：R_A,R_B",required=false,sort=5),
    })
    @Override
    public void executeInternal(JobExecutionContext context, JobObject jobObject) throws Exception {

        JobDetail jobDetail = context.getJobDetail();
        String jobUuid = jobDetail.getKey().getName();

        String batchSize = "2000"; //分页数量，默认是1000
        String ldapUrl = getPropValue(jobObject, "ldapUrl");
        String userDn = getPropValue(jobObject, "userDn");
        String userSecret = getPropValue(jobObject, "userSecret");
        String searchBase = getPropValue(jobObject, "searchBase"); //从xx顶层目录快速查找
        String searchFilter = getPropValue(jobObject, "searchFilter"); //LDAP搜索过滤器类
        String defaultRole = getPropValue(jobObject, "defaultRole");
        if (StringUtils.isBlank(searchFilter)) {
            searchFilter = "objectclass=person";
        }

        //如果为空全量，不为空以上一次执行成功的作业开始时间检索
        String lastStartTime = schedulerMapper.getJobLastExecAuditStartTime(jobUuid , JobAuditVo.Status.SUCCEED.getValue());
        if(StringUtils.isNotBlank(lastStartTime)){
            //查询格式:(&(objectClass=*)(modifyTimestamp>=20230523000000.0Z))
            lastStartTime = lastStartTime.split(" ")[0].replace("-","").trim();
            lastStartTime = lastStartTime+"000000.0Z";
            searchFilter = "(&("+ searchFilter +")(modifyTimestamp>="+ lastStartTime +"))";
        }

        if (StringUtils.isNotBlank(ldapUrl) && StringUtils.isNotBlank(userDn) && StringUtils.isNotBlank(userSecret)) {
            TransactionStatus transactionStatus = TransactionUtil.openTx();
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

                List<UserVo> userList = new ArrayList<>();
                List<String> teamUpNamePathList = new ArrayList<>();
                // 根据设置的域节点、过滤器类和搜索控制器搜索LDAP得到结果
                NamingEnumeration answer = ctx.search(searchBase, searchFilter, searchCtls);
                while (answer.hasMoreElements()) {
                    SearchResult sr = (SearchResult) answer.next();
                    String fullName = sr.getNameInNamespace();
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
                        UserVo userVo = new UserVo();
                        userVo.setUuid(uuid);
                        userVo.setUserId(userId);
                        userVo.setUserName(userName);
                        userVo.setEmail(email);
                        userVo.setPhone(phone);
                        //中转
                        userVo.setTeamUuid(upwardTeamName);
                        teamUpNamePathList.add(upwardTeamName);
                        userList.add(userVo);
                    }
                }
                ctx.close();

                Map<String , String> upNamePathMap = new HashMap<>();
                List<TeamVo>  teamList = this.teamMapper.getTeamUuidbyUpwardNamePath(teamUpNamePathList);
                for (TeamVo teamVo: teamList) {
                    upNamePathMap.put(teamVo.getUpwardNamePath() , teamVo.getUuid());
                }

                List<String> roleUuidList = null; ;
                if(StringUtils.isNotBlank(defaultRole)){
                    String[] roleNames = defaultRole.split(",");
                    List<String> nameList = Arrays.asList(roleNames);
                    if(nameList != null && nameList.size() > 0 ){
                        roleUuidList = this.roleMapper.getRoleUuidByNameList(nameList);
                    }
                }

                List<RoleUserVo> userRoleList = new ArrayList<>();
                List<TeamUserVo> userTeamList = new ArrayList<TeamUserVo>();
                List<String> userUuidList = new ArrayList<>();
                for (UserVo userVo: userList) {
                    userUuidList.add(userVo.getUuid());

                    //人与组织关系
                    String teamUuid = upNamePathMap.get(userVo.getTeamUuid());
                    if(StringUtils.isNotBlank(teamUuid)){
                        userTeamList.add(new TeamUserVo(teamUuid , userVo.getUuid()));
                    }

                    //人员默认角色
                    if(roleUuidList != null ){
                        for (String roleUuid:roleUuidList) {
                            userRoleList.add(new RoleUserVo(roleUuid , userVo.getUuid()));
                        }
                    }
                }
                this.userMapper.bacthDeleteUserTeam(userUuidList , "ldap");
                this.userMapper.batchInsertUser(userList);
                this.userMapper.batchInsertUserRole(userRoleList);
                this.userMapper.batchInsertUserTeam(userTeamList);
                TransactionUtil.commitTx(transactionStatus);
            } catch (NamingException e) {
                logger.error("[Sync Ldap User Error]:" + e.getMessage());
                TransactionUtil.rollbackTx(transactionStatus);
            }finally {

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

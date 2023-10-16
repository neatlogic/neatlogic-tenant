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

import neatlogic.framework.common.constvalue.SystemUser;
import neatlogic.framework.dao.mapper.RoleMapper;
import neatlogic.framework.dao.mapper.TeamMapper;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dto.RoleUserVo;
import neatlogic.framework.dto.UserVo;
import neatlogic.framework.exception.type.ParamNotExistsException;
import neatlogic.framework.scheduler.annotation.Param;
import neatlogic.framework.scheduler.annotation.Prop;
import neatlogic.framework.scheduler.core.PublicJobBase;
import neatlogic.framework.scheduler.dto.JobObject;
import neatlogic.framework.transaction.util.TransactionUtil;
import neatlogic.framework.util.TimeUtil;
import org.apache.commons.collections4.CollectionUtils;
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
import javax.naming.ldap.*;
import java.util.*;

@Component
@DisallowConcurrentExecution
public class SyncLdapUserSchedule extends PublicJobBase {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private TeamMapper teamMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private RoleMapper roleMapper;

    @Override
    public String getName() {
        return "同步LDAP的用户信息";
    }

    @Prop({
            @Param(name = "ldapUrl", controlType = "text", description = "ldap地址", required = true, sort = 0, help = "ldap://192.168.1.99"),
            @Param(name = "userDn", controlType = "text", description = "同步账号dn", required = true, sort = 1, help = "cn=Manager,dc=neatlogic,dc=com"),
            @Param(name = "userSecret", controlType = "text", description = "登录密码", required = true, sort = 2, help = "123456"),
            @Param(name = "searchBase", controlType = "text", description = "从指定目录开始查找", required = true, sort = 3, help = "dc=neatlogic,dc=com"),
            @Param(name = "searchFilter", controlType = "text", description = "过滤条件", required = true, sort = 4, help = "过滤条件把所有ou查询出来，导入到系统用户"),
            @Param(name = "defaultRole", controlType = "text", description = "用户默认角色", required = false, sort = 5, help = "neatlogic-系统配置-角色管理中的角色名字段，多个角色名之间用逗号隔开，如：R_ADMIN,R_ITSM_ADMIN"),
            @Param(name = "uuid", controlType = "text", description = "用户UUID", required = true, sort = 6, help = "指定用户主键映射字段"),
            @Param(name = "userId", controlType = "text", description = "用户ID", required = true, sort = 7, help = "指定neatlogic-系统配置-用户管理中的用户ID属性映射字段"),
            @Param(name = "userName", controlType = "text", description = "用户名", required = true, sort = 8, help = "指定neatlogic-系统配置-用户管理中的用户名属性映射字段"),
            @Param(name = "email", controlType = "text", description = "邮箱", required = false, sort = 9, help = "指定neatlogic-系统配置-用户管理中的邮箱属性映射字段"),
            @Param(name = "phone", controlType = "text", description = "电话", required = false, sort = 10, help = "指定neatlogic-系统配置-用户管理中的电话属性映射字段"),
    })
    @Override
    public void executeInternal(JobExecutionContext context, JobObject jobObject) throws Exception {
        JobDetail jobDetail = context.getJobDetail();
//        String jobUuid = jobDetail.getKey().getName();

        int pageSize = 1000;//分页查询大小
        String ldapUrl = getPropValue(jobObject, "ldapUrl");
        String userDn = getPropValue(jobObject, "userDn");
        String userSecret = getPropValue(jobObject, "userSecret");
        String searchBase = getPropValue(jobObject, "searchBase"); //从xx顶层目录快速查找
        String searchFilter = getPropValue(jobObject, "searchFilter"); //LDAP搜索过滤器类
        String defaultRole = getPropValue(jobObject, "defaultRole");
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

        //如果为空全量，不为空以上一次执行成功的作业开始时间检索
//        String lastStartTime = schedulerMapper.getJobLastExecAuditStartTime(jobUuid, JobAuditVo.Status.SUCCEED.getValue());
//        if(StringUtils.isNotBlank(lastStartTime)){
//            //查询格式:(&(objectClass=*)(modifyTimestamp>=20230523000000.0Z))
//            lastStartTime = lastStartTime.split(" ")[0].replace("-","").trim();
//            lastStartTime = lastStartTime+"000000.0Z";
//            searchFilter = "(&("+ searchFilter +")(modifyTimestamp>="+ lastStartTime +"))";
//        }
        Hashtable<String, String> HashEnv = new Hashtable<String, String>();
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
        String _userId = getPropValue(jobObject, "userId");
        if (StringUtils.isNotBlank(_userId)) {
            returnedAttList.add(_userId);
        }
        String _userName = getPropValue(jobObject, "userName");
        if (StringUtils.isNotBlank(_userName)) {
            returnedAttList.add(_userName);
        }
        String _email = getPropValue(jobObject, "email");
        if (StringUtils.isNotBlank(_email)) {
            returnedAttList.add(_email);
        }
        String _phone = getPropValue(jobObject, "phone");
        if (StringUtils.isNotBlank(_phone)) {
            returnedAttList.add(_phone);
        }
        int totalResults = 0;
        byte[] cookie = null;

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
        List<String> roleUuidList = new ArrayList<>();
        if (StringUtils.isNotBlank(defaultRole)) {
            String[] roleNames = defaultRole.split(",");
            List<String> nameList = Arrays.asList(roleNames);
            if (CollectionUtils.isNotEmpty(nameList)) {
                roleUuidList = this.roleMapper.getRoleUuidByNameList(nameList);
            }
        }
        Date lcd = new Date();
        do {
            // 根据设置的域节点、过滤器类和搜索控制器搜索LDAP得到结果
            NamingEnumeration answer = ctx.search(searchBase, searchFilter, searchCtls);
            while (answer.hasMoreElements()) {
                totalResults++;
                SearchResult sr = (SearchResult) answer.next();
                String fullName = sr.getNameInNamespace();
                String upwardTeamName = getUpwardTeamName(fullName);
                String uuid = null, userId = null, userName = null, email = null, phone = null;
                Attributes Attrs = sr.getAttributes();
                if (Attrs != null) {
                    for (NamingEnumeration ne = Attrs.getAll(); ne.hasMore(); ) {
                        Attribute Attr = (Attribute) ne.next();
                        String attrName = Attr.getID();
                        String attrValue = null;
                        for (NamingEnumeration e = Attr.getAll(); e.hasMore(); ) {
                            attrValue = e.next().toString();
                        }
                        if (attrName.equals(_uuid)) {
                            uuid = attrValue.replace("-", "");
                        } else if (attrName.equals(_userId)) {
                            userId = attrValue;
                        } else if (attrName.equals(_userName)) {
                            userName = attrValue;
                        } else if (attrName.equals(_email)) {
                            email = attrValue;
                        } else if (attrName.equals(_phone)) {
                            phone = attrValue;
                        }
                    }
                }

                if (StringUtils.isNotBlank(userId) && StringUtils.isNotBlank(uuid)) {
                    TransactionStatus transactionStatus = null;
                    try {
                        transactionStatus = TransactionUtil.openTx();
                        UserVo userVo = new UserVo();
                        userVo.setUuid(uuid);
                        userVo.setUserId(userId);
                        userVo.setUserName(userName);
                        userVo.setEmail(email);
                        userVo.setPhone(phone);
                        userVo.setSource("ldap");
                        userVo.setIsActive(1);
                        userVo.setFcu(SystemUser.SYSTEM.getUserUuid());
                        userVo.setLcu(SystemUser.SYSTEM.getUserUuid());
                        userVo.setLcd(lcd);
                        this.userMapper.bacthDeleteUserTeamByUserUuid(uuid, "ldap");
                        this.userMapper.insertUserForLdap(userVo);
                        //人员默认角色
                        if (CollectionUtils.isNotEmpty(roleUuidList)) {
                            List<RoleUserVo> userRoleList = new ArrayList<>();
                            for (String roleUuid : roleUuidList) {
                                userRoleList.add(new RoleUserVo(roleUuid, userVo.getUuid()));
                            }
                            this.userMapper.batchInsertUserRole(userRoleList);
                        }
                        String teamUuid = this.teamMapper.getTeamUuidbyUpwardNamePath(upwardTeamName);
                        if (StringUtils.isNotBlank(teamUuid)) {
                            this.userMapper.insertUserTeam(uuid, teamUuid);
                        }
                        TransactionUtil.commitTx(transactionStatus);
                    } catch (Exception e) {
                        TransactionUtil.rollbackTx(transactionStatus);
                        throw e;
                    }
                }
            }
            cookie = parseControls(ctx.getResponseControls());
            ctx.setRequestControls(new Control[]{new PagedResultsControl(pageSize, cookie, Control.CRITICAL)});
        } while ((cookie != null) && (cookie.length != 0));
        logger.info("时间：" + TimeUtil.getDateString("yyyy-MM-dd hh:mm:ss") + "，从ldap同步用户总数=" + totalResults);
        ctx.close();
        userMapper.updateUserIsDeletedBySourceAndLcd("ldap", lcd);
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

    private static String getTeamName(String name) {
        if (StringUtils.isBlank(name)) {
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

    private static String getUpwardTeamName(String fullName) {
        if (StringUtils.isNotBlank(fullName)) {
            String[] names = fullName.split(",");
            List<String> upwardTeamNameList = new ArrayList<>();
            for (String name : names) {
                if (name.startsWith("ou=")) {
                    String[] ou = name.split("=");
                    if (!"groups".equals(ou[1])) {
                        upwardTeamNameList.add(ou[1]);
                    }
                }
            }
            Collections.reverse(upwardTeamNameList);
            return String.join("/", upwardTeamNameList);
        }
        return null;
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
}

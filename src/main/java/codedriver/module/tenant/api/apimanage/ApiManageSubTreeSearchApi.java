/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.apimanage;

import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dto.ModuleVo;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentFactory;
import codedriver.framework.restful.dao.mapper.ApiMapper;
import codedriver.framework.restful.dto.ApiVo;
import codedriver.framework.restful.enums.TreeMenuType;
import codedriver.module.tenant.service.apiaudit.ApiAuditService;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class ApiManageSubTreeSearchApi extends PrivateApiComponentBase {

    private static final Pattern pattern = Pattern.compile("^[A-Za-z_\\d]+$");

    @Autowired
    private ApiMapper apiMapper;

    @Autowired
    private ApiAuditService apiAuditService;

    @Override
    public String getToken() {
        return "apimanage/subtree/search";
    }

    @Override
    public String getName() {
        return "获取接口管理与或操作审计子目录";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "moduleGroup", type = ApiParamType.STRING, desc = "所属模块", isRequired = true),
            @Param(name = "funcId", type = ApiParamType.STRING, desc = "所属功能", isRequired = true),
            @Param(name = "type", type = ApiParamType.STRING, desc = "目录类型(system|custom|audit)", isRequired = true),
    })
    @Output({})
    @Description(desc = "获取接口管理与或操作审计子目录")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        // TODO 先根据type、moduleGroup、funcId筛选出API
        String moduleGroup = jsonObj.getString("moduleGroup");
        String funcId = jsonObj.getString("funcId");
        String type = jsonObj.getString("type");
        List<String> tokenList = new ArrayList<>();
        if (TreeMenuType.SYSTEM.getValue().equals(type)) {
            List<ApiVo> ramApiList = PrivateApiComponentFactory.getTenantActiveApiList();
            for (ApiVo vo : ramApiList) {
                if (vo.getModuleGroup().equals(moduleGroup) && vo.getToken().startsWith(funcId + "/")) {
                    tokenList.add(vo.getToken());
                }
            }
        } else if (TreeMenuType.CUSTOM.getValue().equals(type)) {
            //获取数据库中所有的API
            List<String> activeModuleIdList = TenantContext.get().getActiveModuleList().stream().map(ModuleVo::getId).collect(Collectors.toList());
            List<ApiVo> activeDbApiVoList = apiMapper.getAllApiByModuleId(activeModuleIdList);
            Map<String, ApiVo> ramApiMap = PrivateApiComponentFactory.getApiMap();
            //如果外部API的token与内部的相同，就跳过此API
            for (ApiVo vo : activeDbApiVoList) {
                if (ramApiMap.get(vo.getToken()) == null) {
                    if (vo.getModuleGroup().equals(moduleGroup) && vo.getToken().startsWith(funcId + "/")) {
                        tokenList.add(vo.getToken());
                    }
                }
            }
        } else if (TreeMenuType.AUDIT.getValue().equals(type)) {
            List<ApiVo> apiList = apiAuditService.getApiListForTree();
            for (ApiVo vo : apiList) {
                if (vo.getModuleGroup().equals(moduleGroup) && vo.getToken().startsWith(funcId + "/")) {
                    tokenList.add(vo.getToken());
                }
            }
        }

        Set<Func> result = new HashSet<>();
        String[] funcSplit = funcId.split("/");
        /**
         * 与入参的funcId根据"/"拆分后的数组比较长度
         * 如果大于1，说明下面还有func
         * 比如入参funcId为"process/channel"，当前token为"process/channel/form/get"
         * 那么下一层func就是form
         */
        for (String token : tokenList) {
            String[] split = token.split("/");
            if (split.length - funcSplit.length > 1) {
                Func func = new Func();
                String s = split[funcSplit.length];
                if (!pattern.matcher(s).matches()) {
                    continue;
                }
                func.setFuncId(s);
                if (result.contains(func)) {
                    if (split.length - funcSplit.length > 2) {
                        result.stream().forEach(vo -> {
                            if (vo.getFuncId().equals(func.funcId) && vo.getIsHasChild() == 0) {
                                vo.setIsHasChild(1);
                            }
                        });
                    }
                } else {
                    if (split.length - funcSplit.length > 2 && pattern.matcher(split[funcSplit.length + 1]).matches()) {
                        func.setIsHasChild(1);
                    }
                    result.add(func);
                }
            }
        }

        return result;
    }


    class Func {
        private String funcId;

        private int isHasChild = 0;

        public String getFuncId() {
            return funcId;
        }

        public void setFuncId(String funcId) {
            this.funcId = funcId;
        }

        public int getIsHasChild() {
            return isHasChild;
        }

        public void setIsHasChild(int isHasChild) {
            this.isHasChild = isHasChild;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ApiManageSubTreeSearchApi.Func func = (ApiManageSubTreeSearchApi.Func) o;
            return Objects.equals(funcId, func.funcId);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((funcId == null) ? 0 : funcId.hashCode());
            return result;
        }
    }

}

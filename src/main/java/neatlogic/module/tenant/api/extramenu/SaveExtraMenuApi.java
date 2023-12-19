/*
 * Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package neatlogic.module.tenant.api.extramenu;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.EXTRA_MENU_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dto.AuthorityVo;
import neatlogic.framework.dto.FieldValidResultVo;
import neatlogic.framework.extramenu.constvalue.ExtraMenuType;
import neatlogic.framework.extramenu.exception.*;
import neatlogic.framework.lrcode.LRCodeManager;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.IValid;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.RegexUtils;
import neatlogic.module.tenant.dao.mapper.ExtraMenuMapper;
import neatlogic.framework.extramenu.dto.ExtraMenuVo;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service

@AuthAction(action = EXTRA_MENU_MODIFY.class)
@OperationType(type = OperationTypeEnum.UPDATE)
@Transactional
public class SaveExtraMenuApi extends PrivateApiComponentBase {

    @Resource
    private ExtraMenuMapper extraMenuMapper;

    @Override
    public String getToken() {
        return "/extramenu/save";
    }

    @Override
    public String getName() {
        return "nmtae.extramenusaveapi.getname";
    }

    @Input({@Param(name = "id", type = ApiParamType.LONG, desc = "id"),
        @Param(name = "name", type = ApiParamType.STRING, xss = true, isRequired = true, maxLength = 50,
            desc = "common.name"),
        @Param(name = "type", type = ApiParamType.ENUM, rule = "0,1", isRequired = true,
            desc = "nmtae.extramenusaveapi.input.param.type.desc"),
        @Param(name = "isActive", type = ApiParamType.ENUM, rule = "0,1", isRequired = true, desc = "common.isactive"),
        @Param(name = "url", type = ApiParamType.REGEX, desc = "URL", rule = RegexUtils.CONNECT_URL),
        @Param(name = "description", type = ApiParamType.STRING, desc = "common.description"),
        @Param(name = "authorityList", type = ApiParamType.JSONARRAY, isRequired = true, desc = "common.authlist"),
        @Param(name = "parentId", type = ApiParamType.LONG, desc = "common.parentid")})
    @Output({@Param(name = "id", type = ApiParamType.LONG, desc = "id")})
    @Description(desc = "nmtae.extramenusaveapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        Long id = paramObj.getLong("id");
        ExtraMenuVo vo = JSON.toJavaObject(paramObj, ExtraMenuVo.class);
        if (vo.getParentId() == null) {
            vo.setParentId(ExtraMenuVo.ROOT_ID);
        } else {
            // 判断父节点是否为目录
            ExtraMenuVo parentVo = extraMenuMapper.getExtraMenuById(vo.getParentId());
            if (parentVo == null || parentVo.getType() != null && parentVo.getType() == ExtraMenuType.MENU.getType()) {
                throw new ExtraMenuNotAllowedAddException();
            }
        }
        if (extraMenuMapper.checkExtraMenuNameIsRepeat(vo) > 0) {
            throw new ExtraMenuNameRepeatException(vo.getName());
        }
        if (vo.getType() != null && ExtraMenuType.MENU.getType() == vo.getType().intValue()) {
            if (StringUtils.isBlank(vo.getUrl())) {
                throw new ExtraMenuParamException("url");
            }
        }
        if (id != null) {
            if (extraMenuMapper.checkExtraMenuIsExists(id) == 0) {
                throw new ExtraMenuNotFoundException(id);
            }
            extraMenuMapper.deleteExtraMenuAuthorityByMenuId(id);
            extraMenuMapper.updateExtraMenuById(vo);
        } else {
            if (!ExtraMenuVo.ROOT_ID.equals(vo.getParentId())) {
                if (extraMenuMapper.checkExtraMenuIsExists(vo.getParentId()) == 0) {
                    throw new ExtraMenuNotFoundException(vo.getParentId());
                }
            } else {
                if (extraMenuMapper.checkExtraMenuRootCount(ExtraMenuVo.ROOT_ID) == 1) {
                    throw new ExtraMenuRootException();
                }
            }
            int lft = LRCodeManager.beforeAddTreeNode("extramenu", "id", "parent_id", vo.getParentId());
            vo.setParentId(vo.getParentId());
            vo.setLft(lft);
            vo.setRht(lft + 1);
            extraMenuMapper.insertExtraMenu(vo);
        }

        List<AuthorityVo> authorityList = vo.getAuthorityVoList();
        if (CollectionUtils.isNotEmpty(authorityList)) {
            for (AuthorityVo authorityVo : authorityList) {
                extraMenuMapper.insertExtraMenuAuthority(authorityVo, vo.getId());
            }
        }
        return vo.getId();
    }

    public IValid name() {
        return value -> {
            ExtraMenuVo vo = JSON.toJavaObject(value, ExtraMenuVo.class);
            if (vo.getParentId() == null) {
                vo.setParentId(ExtraMenuVo.ROOT_ID);
            }
            if (extraMenuMapper.checkExtraMenuNameIsRepeat(vo) > 0) {
                return new FieldValidResultVo(new ExtraMenuNameRepeatException(vo.getName()));
            }
            return new FieldValidResultVo();
        };
    }
}

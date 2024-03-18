/*Copyright (C) $today.year  深圳极向量科技有限公司 All Rights Reserved.

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
        @Param(name = "url", type = ApiParamType.REGEX, desc = "URL", rule = RegexUtils.URL),
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

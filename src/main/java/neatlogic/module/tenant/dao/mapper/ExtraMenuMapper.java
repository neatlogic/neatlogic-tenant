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

package neatlogic.module.tenant.dao.mapper;

import neatlogic.framework.dto.AuthorityVo;
import neatlogic.framework.extramenu.dto.ExtraMenuVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtraMenuMapper {

    Integer getMaxRhtCode();

    int insertExtraMenu(ExtraMenuVo vo);

    int insertExtraMenuAuthority(@Param("authorityVo") AuthorityVo authorityVo, @Param("menuId") Long menuId);

    int checkExtraMenuNameIsRepeat(ExtraMenuVo vo);

    int checkExtraMenuIsExists(Long id);

    int updateExtraMenuById(ExtraMenuVo vo);

    int deleteExtraMenuAuthorityByMenuId(Long id);

    List<ExtraMenuVo> getExtraMenuForTree(@Param("lft") Integer lft, @Param("rht") Integer rht);

    int checkExtraMenuRootCount(Long parentId);

    ExtraMenuVo getExtraMenuById(Long id);

    List<AuthorityVo> getExtraMenuAuthorityListByMenuId(Long id);

    int deleteExtraMenuById(Long id);

    List<Long> getAuthorizedExtraMenuIdList(@Param("userUuid") String userUuid,
        @Param("teamUuidList") List<String> teamUuidList, @Param("roleUuidList") List<String> roleUuidList);
}

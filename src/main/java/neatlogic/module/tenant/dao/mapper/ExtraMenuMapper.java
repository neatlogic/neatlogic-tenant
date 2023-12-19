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

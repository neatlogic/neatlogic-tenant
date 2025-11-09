/*
 *
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 */

package neatlogic.module.tenant.dao.mapper;

import neatlogic.framework.dto.AuthorityVo;
import neatlogic.framework.extramenu.dto.ExtraMenuVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtraMenuMapper {

    Integer getMaxRhtCode();

    int insertExtraMenu(ExtraMenuVo vo);

    Integer getMaxSort();

    int insertExtraMenuAuthority(@Param("authorityVo") AuthorityVo authorityVo, @Param("menuId") Long menuId);

    int checkExtraMenuNameIsRepeat(ExtraMenuVo vo);

    int checkExtraMenuIsExists(Long id);

    int updateExtraMenuById(ExtraMenuVo vo);

    void updateExtraMenuSort(ExtraMenuVo vo);

    int deleteExtraMenuAuthorityByMenuId(Long id);

    List<ExtraMenuVo> getExtraMenuForTree(@Param("lft") Integer lft, @Param("rht") Integer rht);

    int checkExtraMenuRootCount(Long parentId);

    ExtraMenuVo getExtraMenuById(Long id);

    List<ExtraMenuVo> getExtraMenuByIdList(@Param("idList") List<Long> idList);

    List<AuthorityVo> getExtraMenuAuthorityListByMenuId(Long id);

    int deleteExtraMenuById(Long id);

    List<Long> getAuthorizedExtraMenuIdList(@Param("userUuid") String userUuid,
                                            @Param("teamUuidList") List<String> teamUuidList, @Param("roleUuidList") List<String> roleUuidList);
}

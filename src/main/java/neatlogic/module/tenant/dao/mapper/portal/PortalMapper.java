/*
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package neatlogic.module.tenant.dao.mapper.portal;

import neatlogic.framework.dto.AuthenticationInfoVo;
import neatlogic.framework.dto.AuthorityVo;
import neatlogic.module.tenant.dto.portal.PortalSearchVo;
import neatlogic.module.tenant.dto.portal.PortalVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PortalMapper {
    PortalVo getPortalById(Long id);

    int getPortalCount(PortalSearchVo searchVo);

    List<PortalVo> searchPortal(PortalSearchVo searchVo);

    Integer getMaxSort();

    List<AuthorityVo> getPortalAuthorityListByPortalId(Long portalId);

    int insertPortal(PortalVo portalVo);

    int insertPortalAuthority(@Param("portalId") Long portalId, @Param("authorityVo") AuthorityVo authorityVo);

    int updatePortalActive(PortalVo portalVo);

    int deletePortalById(Long id);

    int deletePortalAuthorityByPortalId(Long portalId);

    List<AuthorityVo> getPortalWidgetAuthorityListByName(String name);

    List<String> getPortalWidgetNameListByAuthority(AuthenticationInfoVo authenticationInfoVo);

    List<Long> getPortalIdListByAuthority(AuthenticationInfoVo authenticationInfoVo);

    Long getUserEnablePortalId(@Param("moduleGroup") String moduleGroup, @Param("userUuid") String userUuid);

    int deleteUserEnablePortal(@Param("moduleGroup") String moduleGroup, @Param("userUuid") String userUuid);

    int insertUserEnablePortal(@Param("portalId") Long portalId, @Param("moduleGroup") String moduleGroup, @Param("userUuid") String userUuid);

    int insertPortalWidgetAuthority(@Param("portalWidgetName") String portalWidgetName, @Param("authorityVo") AuthorityVo authorityVo);

    int deletePortalWidgetAuthorityByName(String name);
}

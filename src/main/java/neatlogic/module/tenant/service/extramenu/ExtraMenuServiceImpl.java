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

package neatlogic.module.tenant.service.extramenu;

import neatlogic.framework.extramenu.constvalue.ExtraMenuType;
import neatlogic.framework.extramenu.dto.ExtraMenuVo;
import neatlogic.module.tenant.dao.mapper.ExtraMenuMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Iterator;

@Service
public class ExtraMenuServiceImpl implements ExtraMenuService {
    @Resource
    ExtraMenuMapper extraMenuMapper;

    @Override
    public ExtraMenuVo buildRootExtraMenu() {
        Integer maxRhtCode = extraMenuMapper.getMaxRhtCode();
        ExtraMenuVo extraMenuVo = new ExtraMenuVo();
        extraMenuVo.setId(ExtraMenuVo.ROOT_ID);
        extraMenuVo.setName(ExtraMenuVo.ROOT_NAME);
        extraMenuVo.setParentId(ExtraMenuVo.ROOT_PARENTID);
        extraMenuVo.setIsActive(1);
        extraMenuVo.setLft(1);
        extraMenuVo.setRht(maxRhtCode == null ? 2 : maxRhtCode + 1);
        return extraMenuVo;
    }

    @Override
    public ExtraMenuVo removeEmptyDirectory(ExtraMenuVo vo) {
        if (vo == null) {
            return null;
        }
        if (vo.getType() != null && vo.getType() == ExtraMenuType.DIRECTORY.getType()) {
            if (vo.getChildren() == null) {
                return null;
            }
            Iterator<ExtraMenuVo> iterator = vo.getChildren().listIterator();
            while (iterator.hasNext()) {
                ExtraMenuVo childrenVo = iterator.next();
                if (removeEmptyDirectory(childrenVo) == null) {
                    iterator.remove();
                }
            }
            if (vo.getChildren().size() > 0) {
                return vo;
            } else {
                return null;
            }
        } else {
            return vo;
        }
    }
}

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

package neatlogic.module.tenant.service.extramenu;

import neatlogic.framework.extramenu.constvalue.ExtraMenuType;
import neatlogic.module.tenant.dao.mapper.ExtraMenuMapper;
import neatlogic.framework.extramenu.dto.ExtraMenuVo;
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
        extraMenuVo.setRht(maxRhtCode == null ? 2 : maxRhtCode.intValue() + 1);
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

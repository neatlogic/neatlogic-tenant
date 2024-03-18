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

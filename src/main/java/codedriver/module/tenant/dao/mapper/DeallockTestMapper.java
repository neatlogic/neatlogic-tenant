/*
 * Copyright(c) 2021 TechSureCo.,Ltd.AllRightsReserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.dao.mapper;

import codedriver.framework.restful.counter.DelayedItem;
import codedriver.module.tenant.dto.DeallockTestVo;

public interface DeallockTestMapper {

    DeallockTestVo getLockById(Long id);

    int insert(DeallockTestVo deallockTestVo);

    int update(DeallockTestVo deallockTestVo);

    int delete(Long id);
}

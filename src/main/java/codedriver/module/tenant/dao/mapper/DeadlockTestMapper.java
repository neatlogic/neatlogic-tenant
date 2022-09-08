/*
 * Copyright(c) 2021 TechSureCo.,Ltd.AllRightsReserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.dao.mapper;

import codedriver.module.tenant.dto.DeadlockTestVo;

public interface DeadlockTestMapper {

    DeadlockTestVo getLockById(Long id);

    int insert(DeadlockTestVo deadlockTestVo);

    int update(DeadlockTestVo deadlockTestVo);

    int delete(Long id);
}


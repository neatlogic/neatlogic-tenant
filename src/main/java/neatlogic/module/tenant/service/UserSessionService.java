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

package neatlogic.module.tenant.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public interface UserSessionService {

    /**
     * 清除用户会话缓存
     *
     * @param userUuid  用户uuid
     * @param isPassive 是否被动清理，0：否，1：是，默认 0
     */
    JSONObject clearUserSessionCache(String userUuid, int isPassive, JSONArray removeTokenList);

    /**
     * 删除用户会话
     *
     * @param userUuid  用户uuid
     */
    JSONObject deleteUserSessionAndCache(String userUuid);

    /**
     * 清除用户会话缓存
     *
     * @param userUuid  用户uuid
     */
    JSONObject clearUserSessionCache(String userUuid);
}

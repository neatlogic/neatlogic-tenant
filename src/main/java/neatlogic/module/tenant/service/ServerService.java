/*
 * Copyright (C) 2025  深圳极向量科技有限公司 All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package neatlogic.module.tenant.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.heartbeat.dto.ServerClusterVo;

import javax.servlet.ServletOutputStream;

public interface ServerService {

    /**
     * 请求其他节点同个接口
     *
     * @param paramObj 入参
     * @param serverId 节点 id
     */
    JSONObject postOtherServerApi(JSONObject paramObj, Integer serverId);


    /**
     * 请求其他节点同个接口
     *
     * @param paramObj        入参
     * @param serverClusterVo 节点
     */
    JSONObject postOtherServerApi(JSONObject paramObj, ServerClusterVo serverClusterVo);

    /**
     * 请求其他节点同个接口
     *
     * @param paramObj        入参
     * @param serverClusterVo 节点
     */
    JSONObject postOtherServerApi(JSONObject paramObj, ServerClusterVo serverClusterVo, String uri);

    /**
     * 请求其他节点同个接口
     *
     * @param paramObj 入参
     * @param serverId 节点 id
     */
    JSONArray postOtherServersApi(JSONObject paramObj, int serverId);

    /**
     * 请求其他节点同个接口
     *
     * @param paramObj 入参
     * @param serverId 节点 id
     */
    JSONArray postOtherServersApi(JSONObject paramObj, int serverId, String uri);


    /**
     *
     * @param paramObj        入参
     * @param serverClusterVo 节点
     * @param os              输出流
     */
    String downloadOtherServerApi(JSONObject paramObj, ServerClusterVo serverClusterVo, ServletOutputStream os);


    /**
     *
     * @param paramObj 入参
     * @param serverId 节点id
     * @param os       输出流
     */
    String downloadOtherServerApi(JSONObject paramObj, int serverId, ServletOutputStream os);

}

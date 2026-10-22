/*
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package neatlogic.module.tenant.exception.file;

import neatlogic.framework.exception.core.ApiRuntimeException;

/** 网络图片重定向超限或缺少目标地址时抛出的异常。 */
public class NetworkImageRedirectException extends ApiRuntimeException {
    private static final long serialVersionUID = 1L;

    /** 沿用现有重定向错误文案，供调用方区分跳转失败。 */
    public NetworkImageRedirectException() {
        super("图片下载重定向次数超限或缺少目标地址");
    }
}

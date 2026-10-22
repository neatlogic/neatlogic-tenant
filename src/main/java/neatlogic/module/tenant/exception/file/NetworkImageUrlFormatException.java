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

/** 网络图片地址无法按 URI 格式解析时抛出的异常。 */
public class NetworkImageUrlFormatException extends ApiRuntimeException {
    private static final long serialVersionUID = 1L;

    /** 保留原始解析异常及现有地址格式错误文案。 */
    public NetworkImageUrlFormatException(Throwable cause) {
        super("nmtef.networkimageurlformatexception.networkimageurlformatexception", cause);
    }
}

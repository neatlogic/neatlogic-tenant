/*
 * Copyright (C) 2025  TechSure Co., Ltd. All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package neatlogic.module.tenant.exception.file;

import neatlogic.framework.exception.core.ApiRuntimeException;

/** 网络图片签名、结构或解码结果无效时抛出的异常。 */
public class NetworkImageInvalidContentException extends ApiRuntimeException {
    private static final long serialVersionUID = 1L;

    /** 保留图片校验或解码失败的原始异常。 */
    public NetworkImageInvalidContentException(Throwable cause) {
        super("nmtef.networkimageinvalidcontentexception.networkimageinvalidcontentexception", cause);
    }

    /** 没有底层异常时仍以专属类型报告无效图片内容。 */
    public NetworkImageInvalidContentException() {
        super("nmtef.networkimageinvalidcontentexception.networkimageinvalidcontentexception");
    }
}

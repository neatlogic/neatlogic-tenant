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

/** 网络图片无法重新编码为标准图片时抛出的异常。 */
public class NetworkImageEncodingException extends ApiRuntimeException {
    private static final long serialVersionUID = 1L;

    /** 保留编码器异常，便于定位图片重新编码失败原因。 */
    public NetworkImageEncodingException(Throwable cause) {
        super("nmtef.networkimageencodingexception.networkimageencodingexception", cause);
    }
}

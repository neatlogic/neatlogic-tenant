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

/** 网络图片响应内容类型不是图片时抛出的异常。 */
public class NetworkImageContentTypeException extends ApiRuntimeException {
    private static final long serialVersionUID = 1L;

    /** 沿用现有内容类型错误文案。 */
    public NetworkImageContentTypeException() {
        super("网络地址返回的内容不是图片");
    }
}

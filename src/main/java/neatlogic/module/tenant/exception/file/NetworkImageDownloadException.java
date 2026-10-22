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

import java.io.IOException;

/** 网络图片下载发生输入输出错误时抛出的异常。 */
public class NetworkImageDownloadException extends ApiRuntimeException {
    private static final long serialVersionUID = 1L;

    /** 保留原始输入输出异常及其说明，便于定位下载失败原因。 */
    public NetworkImageDownloadException(IOException cause) {
        super("nmtef.networkimagedownloadexception.networkimagedownloadexception", cause, cause.getMessage());
    }
}

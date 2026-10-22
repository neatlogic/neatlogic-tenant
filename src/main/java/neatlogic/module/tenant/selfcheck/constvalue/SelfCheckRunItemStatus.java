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
package neatlogic.module.tenant.selfcheck.constvalue;

public enum SelfCheckRunItemStatus {
    PENDING,
    RUNNING,
    SUCCEED,
    ASSERT_FAILED,
    TARGET_ERROR,
    TARGET_TIMEOUT,
    SCRIPT_SYNTAX_ERROR,
    SCRIPT_RUNTIME_ERROR,
    SCRIPT_RESULT_ERROR,
    SCRIPT_TIMEOUT,
    BUSY,
    CLEANUP_FAILED,
    ABORTED
}

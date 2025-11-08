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

package neatlogic.module.tenant.service.documentonline;

import neatlogic.framework.documentonline.dto.DocumentOnlineConfigVo;
import neatlogic.framework.documentonline.dto.DocumentOnlineDirectoryVo;
import neatlogic.framework.documentonline.dto.DocumentOnlineVo;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface DocumentOnlineService {
    /**
     * 从流中先跳过skip个字符，再截取number个字符返回
     * @param inputStream
     * @param skip 跳过字符个数
     * @param number 截取字符个数
     * @return 返回结果
     * @throws IOException
     */
    String interceptsSpecifiedNumberOfCharacters(InputStream inputStream, int skip, int number) throws IOException;

    /**
     * 通过递归，获取某个目录下的指定模块、指定菜单下的文件
     * @param directory 目录
     * @param moduleGroup 指定模块
     * @param menu 指定菜单
     * @return 返回文件列表
     */
    List<DocumentOnlineVo> getAllFileList(DocumentOnlineDirectoryVo directory, String moduleGroup, String menu);

    /**
     * 通过递归，获取某个目录下的所有文件
     * @param directory 目录
     * @return 返回文件列表
     */
    List<DocumentOnlineVo> getAllFileList(DocumentOnlineDirectoryVo directory);

    /**
     * 根据文件路径在目录树中找到该文件信息
     * @param filePath 文件路径
     * @return 对应文件信息
     */
    DocumentOnlineDirectoryVo getDocumentOnlineDirectoryByFilePath(String filePath);

    /**
     * 保存在线帮助文档与模块菜单的映射配置
     * @param directory
     * @param newConfigVo
     */
    void saveDocumentOnlineConfig(DocumentOnlineDirectoryVo directory, DocumentOnlineConfigVo newConfigVo);

    /**
     * 删除在线帮助文档与模块菜单的映射配置
     * @param directory
     * @param oldConfigVo
     */
    void deleteDocumentOnlineConfig(DocumentOnlineDirectoryVo directory, DocumentOnlineConfigVo oldConfigVo);
}

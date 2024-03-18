/*Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

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

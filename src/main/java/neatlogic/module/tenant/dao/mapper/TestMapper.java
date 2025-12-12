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

package neatlogic.module.tenant.dao.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface TestMapper {
    public List<HashMap> testBg();

    String getContent();

    void insertContent(String content);

    Map<String, Object> getProcessTaskByIdForUpdate(Long id);

    List<Map<String, Object>> getDatabaseTableStructure(@Param("tableName") String tableName);

    int getGzipContentCountByTableNameAndColumnName(@Param("tableName") String tableName, @Param("columnName") String columnName, @Param("action") String action);

    List<Map<String, String>> getGzipContentListByTableNameAndColumnName(@Param("tableName") String tableName, @Param("columnName") String columnName, @Param("action") String action);

    int updateGzipContentByTableNameAndColumnName(@Param("tableName") String tableName, @Param("columnName") String columnName, @Param("oldContent") String oldContent, @Param("newContent") String newContent);
}

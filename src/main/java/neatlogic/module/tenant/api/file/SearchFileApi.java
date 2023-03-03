/*
 * Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package neatlogic.module.tenant.api.file;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.FILE_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.file.dao.mapper.FileMapper;
import neatlogic.framework.file.dto.FileVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.TableResultUtil;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
@AuthAction(action = FILE_MODIFY.class)
public class SearchFileApi extends PrivateApiComponentBase {

    @Resource
    private FileMapper fileMapper;

    @Override
    public String getToken() {
        return "file/search";
    }

    @Override
    public String getName() {
        return "搜索附件";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Override
    @Input({@Param(name = "keyword", type = ApiParamType.STRING, desc = "关键字"),
            @Param(name = "sortList", type = ApiParamType.JSONARRAY, desc = "排序规则"),
            @Param(name = "userUuid", type = ApiParamType.STRING, desc = "上传用户"),
            @Param(name = "uploadTimeRange", type = ApiParamType.JSONOBJECT, desc = "上传时间范围")})
    @Output({@Param(explode = BasePageVo.class)})
    @Description(desc = "搜索附件接口")
    public Object myDoService(JSONObject jsonObj) {
        FileVo fileVo = JSONObject.toJavaObject(jsonObj, FileVo.class);
        int rowNum = fileMapper.searchFileCount(fileVo);
        fileVo.setRowNum(rowNum);
        List<FileVo> fileList = fileMapper.searchFile(fileVo);
        return TableResultUtil.getResult(fileList, fileVo);
    }

}

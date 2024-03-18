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

package neatlogic.module.tenant.api.file;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.file.dao.mapper.FileMapper;
import neatlogic.framework.file.dto.FileVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class ListFileApi extends PrivateApiComponentBase {

    @Resource
    private FileMapper fileMapper;

    @Override
    public String getToken() {
        return "file/list";
    }

    @Override
    public String getName() {
        return "根据附件id列表获取附件信息";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Override
    @Input({
            @Param(name = "idList", type = ApiParamType.JSONARRAY, isRequired = true, desc = "附件id")
    })
    @Output({@Param(explode = FileVo[].class)})
    @Description(desc = "根据附件id列表获取附件信息接口")
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONArray jsonList = jsonObj.getJSONArray("idList");
        List<Long> fileIdList = new ArrayList<>();
        for (int i = 0; i < jsonList.size(); i++) {
            fileIdList.add(jsonList.getLong(i));
        }
        return fileMapper.getFileListByIdList(fileIdList);
    }

}

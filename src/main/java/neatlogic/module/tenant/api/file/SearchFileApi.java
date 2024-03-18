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

/*
 * Copyright(c) 2022 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.file;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.file.dao.mapper.FileMapper;
import codedriver.framework.file.dto.FileVo;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
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

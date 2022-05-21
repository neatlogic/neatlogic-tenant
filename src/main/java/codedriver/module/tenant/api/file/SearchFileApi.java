/*
 * Copyright(c) 2022 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.file;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.auth.label.FILE_MODIFY;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.file.dao.mapper.FileMapper;
import codedriver.framework.file.dto.FileVo;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.util.TableResultUtil;
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

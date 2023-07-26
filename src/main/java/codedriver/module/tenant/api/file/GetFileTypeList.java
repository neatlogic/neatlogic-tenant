/*
 * Copyright(c) 2022 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.file;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.auth.label.FILE_MODIFY;
import codedriver.framework.file.core.FileTypeHandlerFactory;
import codedriver.framework.file.dto.FileTypeVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
@AuthAction(action = FILE_MODIFY.class)
public class GetFileTypeList extends PrivateApiComponentBase {


    @Override
    public String getName() {
        return "获取附件归属列表";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Override
    @Output({@Param(explode = FileTypeVo[].class)})
    @Description(desc = "获取附件归属列表接口")
    public Object myDoService(JSONObject paramObj) throws Exception {
        return FileTypeHandlerFactory.getActiveFileTypeHandler();
    }

    @Override
    public String getToken() {
        return "file/type/list";
    }
}
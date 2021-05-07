/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.matrix.test;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 用于矩阵外部数据源测试
 */
@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class MatrixExternalTest4Api extends PrivateApiComponentBase {

    private final static Logger logger = LoggerFactory.getLogger(MatrixExternalTest4Api.class);

    @Override
    public String getToken() {
        return "matrix/external/test4";
    }

    @Override
    public String getName() {
        return "返回结果为空串";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Description(desc = "返回结果为空串")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        return "";
    }

}

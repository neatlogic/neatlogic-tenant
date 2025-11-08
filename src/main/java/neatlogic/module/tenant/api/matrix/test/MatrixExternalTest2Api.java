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

package neatlogic.module.tenant.api.matrix.test;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.NoAuth;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 用于矩阵外部数据源测试
 */
@Service
@AuthAction(action = NoAuth.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class MatrixExternalTest2Api extends PrivateApiComponentBase {

    private final static Logger logger = LoggerFactory.getLogger(MatrixExternalTest2Api.class);

    @Override
    public String getToken() {
        return "matrix/external/test2";
    }

    @Override
    public String getName() {
        return "返回结果缺少theadList";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Description(desc = "返回结果结构完整")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject returnObj = new JSONObject();
        returnObj.put("currentPage",1);
        returnObj.put("rowNum",5);
        returnObj.put("pageSize",5);
        returnObj.put("pageCount",1);
//        returnObj.put("theadList",new JSONArray(){
//            {
//                this.add(new JSONObject(){
//                    {
//                        this.put("key","label");
//                        this.put("title","标题");
//                    }
//                });
//            }
//        });
        returnObj.put("tbodyList",new JSONArray(){
            {
                this.add(new JSONObject(){
                    {
                        this.put("label","q");
                    }
                });
                this.add(new JSONObject(){
                    {
                        this.put("label","w");
                    }
                });
                this.add(new JSONObject(){
                    {
                        this.put("label","e");
                    }
                });
            }
        });
        return returnObj;
    }

}

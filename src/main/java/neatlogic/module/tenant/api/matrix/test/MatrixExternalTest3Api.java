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

package neatlogic.module.tenant.api.matrix.test;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 用于矩阵外部数据源测试
 */
@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class MatrixExternalTest3Api extends PrivateApiComponentBase {

    private final static Logger logger = LoggerFactory.getLogger(MatrixExternalTest3Api.class);

    @Override
    public String getToken() {
        return "matrix/external/test3";
    }

    @Override
    public String getName() {
        return "返回结果theadLit缺少key或title";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Description(desc = "返回结果theadLit缺少key或title")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject returnObj = new JSONObject();
        returnObj.put("currentPage",1);
        returnObj.put("rowNum",5);
        returnObj.put("pageSize",5);
        returnObj.put("pageCount",1);
        returnObj.put("theadList",new JSONArray(){
            {
                this.add(new JSONObject(){
                    {
//                        this.put("key","label");
                        this.put("title","标题");
                    }
                });
            }
        });
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

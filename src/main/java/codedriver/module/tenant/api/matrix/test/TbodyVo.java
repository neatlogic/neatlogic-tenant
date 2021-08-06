/*
 * Copyright(c) 2021 TechSureCo.,Ltd.AllRightsReserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.matrix.test;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.restful.annotation.EntityField;

/**
 * @author linbq
 * @since 2021/8/5 17:18
 **/
public class TbodyVo {
    @EntityField(name = "第一列", type = ApiParamType.STRING)
    private String column1;
    @EntityField(name = "第二列", type = ApiParamType.STRING)
    private String column2;
    @EntityField(name = "第三列", type = ApiParamType.STRING)
    private String column3;
    @EntityField(name = "第四列", type = ApiParamType.STRING)
    private String column4;
    @EntityField(name = "第五列", type = ApiParamType.STRING)
    private String column5;

    public TbodyVo() {

    }
    public TbodyVo(String column1, String column2, String column3, String column4, String column5) {
        this.column1 = column1;
        this.column2 = column2;
        this.column3 = column3;
        this.column4 = column4;
        this.column5 = column5;
    }

    public String getColumn1() {
        return column1;
    }

    public void setColumn1(String column1) {
        this.column1 = column1;
    }

    public String getColumn2() {
        return column2;
    }

    public void setColumn2(String column2) {
        this.column2 = column2;
    }

    public String getColumn3() {
        return column3;
    }

    public void setColumn3(String column3) {
        this.column3 = column3;
    }

    public String getColumn4() {
        return column4;
    }

    public void setColumn4(String column4) {
        this.column4 = column4;
    }

    public String getColumn5() {
        return column5;
    }

    public void setColumn5(String column5) {
        this.column5 = column5;
    }
}

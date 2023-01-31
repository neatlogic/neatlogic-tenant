/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package neatlogic.module.tenant.api.dependency;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.common.dto.ValueTextVo;
import neatlogic.framework.common.util.PageUtil;
import neatlogic.framework.dependency.core.DependencyManager;
import neatlogic.framework.dependency.core.FromTypeFactory;
import neatlogic.framework.dependency.core.IFromType;
import neatlogic.framework.dependency.dto.DependencyInfoVo;
import neatlogic.framework.exception.type.ParamIrregularException;
import neatlogic.framework.exception.type.ParamNotExistsException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 通用的查询引用列表接口
 *
 * @author: linbq
 * @since: 2021/4/2 11:06
 **/
@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class DependencyListApi extends PrivateApiComponentBase {

    /**
     * @return String
     * @Author: chenqiwei
     * @Time:Jun 19, 2020
     * @Description: 接口唯一标识，也是访问URI
     */
    @Override
    public String getToken() {
        return "dependency/list";
    }

    /**
     * @return String
     * @Author: chenqiwei
     * @Time:Jun 19, 2020
     * @Description: 接口中文名
     */
    @Override
    public String getName() {
        return "查询引用列表";
    }

    /**
     * @return String
     * @Author: chenqiwei
     * @Time:Jun 19, 2020
     * @Description: 额外配置
     */
    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "id", type = ApiParamType.LONG, desc = "被调用者唯一标识是数字类型的时候，通过id参数传入"),
            @Param(name = "uuid", type = ApiParamType.STRING, desc = "被调用者唯一标识是字符串类型的时候，通过uuid参数传入"),
            @Param(name = "calleeType", type = ApiParamType.STRING, isRequired = true, desc = "被调用者类型"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页条目"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页")
    })
    @Output({
            @Param(explode = BasePageVo.class),
            @Param(name = "list", explode = ValueTextVo[].class, desc = "引用列表")
    })
    @Description(desc = "查询引用列表")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Object callee;
        Long id = jsonObj.getLong("id");
        if (id != null) {
            callee = id;
        } else {
            String uuid = jsonObj.getString("uuid");
            if (StringUtils.isNotBlank(uuid)) {
                callee = uuid;
            } else {
                throw new ParamNotExistsException("id", "uuid");
            }
        }
        IFromType calleeType = FromTypeFactory.getCalleeType(jsonObj.getString("calleeType"));
        if (calleeType == null) {
            throw new ParamIrregularException("calleeType（被调用者类型）", FromTypeFactory.getAllCalleeTypeToString());
        }
        BasePageVo basePageVo = JSON.toJavaObject(jsonObj, BasePageVo.class);
        JSONObject resultObj = new JSONObject();
        int pageCount = 0;
        int rowNum = DependencyManager.getDependencyCount(calleeType, callee);
        if (rowNum > 0) {
            pageCount = PageUtil.getPageCount(rowNum, basePageVo.getPageSize());
            List<DependencyInfoVo> list = DependencyManager.getDependencyList(calleeType, callee, basePageVo);
            resultObj.put("list", list);
        } else {
            resultObj.put("list", new ArrayList<>());
        }
        resultObj.put("currentPage", basePageVo.getCurrentPage());
        resultObj.put("pageSize", basePageVo.getPageSize());
        resultObj.put("pageCount", pageCount);
        resultObj.put("rowNum", rowNum);
        return resultObj;
    }
}

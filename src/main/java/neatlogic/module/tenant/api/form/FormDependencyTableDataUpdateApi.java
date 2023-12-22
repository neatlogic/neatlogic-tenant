package neatlogic.module.tenant.api.form;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.FORM_MODIFY;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.dependency.core.DependencyManager;
import neatlogic.framework.form.dao.mapper.FormMapper;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.framework.dependency.handler.Matrix2FormAttributeDependencyHandler;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service
@OperationType(type = OperationTypeEnum.UPDATE)
@AuthAction(action = FORM_MODIFY.class)
public class FormDependencyTableDataUpdateApi extends PrivateApiComponentBase {

    @Resource
    private FormMapper formMapper;


    @Override
    public String getName() {
        return "nmtaf.formdependencytabledataupdateapi.getname";
    }

    @Input({})
    @Output({})
    @Description(desc = "nmtaf.formdependencytabledataupdateapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        JSONObject resultObj = new JSONObject();
        int rowNum = formMapper.getFormAttributeMatrixCount();
        resultObj.put("rowNum", rowNum);
        if (rowNum == 0) {
            return null;
        }
        BasePageVo searchVo = new BasePageVo();
        searchVo.setRowNum(rowNum);
        searchVo.setPageSize(100);
        int pageCount = searchVo.getPageCount();
        for (int currentPage = 1; currentPage <= pageCount; currentPage++) {
            searchVo.setCurrentPage(currentPage);
            List<Map<String, Object>> formAttributeMatrixList = formMapper.getFormAttributeMatrixList(searchVo);
            for (Map<String, Object> formAttributeMatrix : formAttributeMatrixList) {
                JSONObject dependencyConfig = new JSONObject();
                dependencyConfig.put("formVersionUuid", formAttributeMatrix.get("formVersionUuid"));
                DependencyManager.insert(Matrix2FormAttributeDependencyHandler.class, formAttributeMatrix.get("matrixUuid"), formAttributeMatrix.get("formAttributeUuid"), dependencyConfig);
            }
        }
        return resultObj;
    }

    @Override
    public String getToken() {
        return "form/dependency/data/update";
    }
}

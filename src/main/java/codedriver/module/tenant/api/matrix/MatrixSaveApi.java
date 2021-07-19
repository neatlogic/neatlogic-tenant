package codedriver.module.tenant.api.matrix;

import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.util.FileUtil;
import codedriver.framework.dao.mapper.SchemaMapper;
import codedriver.framework.dto.FieldValidResultVo;
import codedriver.framework.exception.database.DataBaseNotFoundException;
import codedriver.framework.exception.file.FileNotFoundException;
import codedriver.framework.exception.type.ParamNotExistsException;
import codedriver.framework.file.dao.mapper.FileMapper;
import codedriver.framework.file.dto.FileVo;
import codedriver.framework.matrix.constvalue.MatrixAttributeType;
import codedriver.framework.matrix.constvalue.MatrixType;
import codedriver.framework.matrix.dao.mapper.MatrixExternalMapper;
import codedriver.framework.matrix.dao.mapper.MatrixMapper;
import codedriver.framework.matrix.dao.mapper.MatrixViewMapper;
import codedriver.framework.matrix.dto.*;
import codedriver.framework.matrix.exception.*;
import codedriver.framework.matrix.view.MatrixViewSqlBuilder;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.IValid;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.transaction.core.EscapeTransactionJob;
import codedriver.framework.util.UuidUtil;
import codedriver.module.tenant.auth.label.MATRIX_MODIFY;
import codedriver.module.tenant.service.matrix.MatrixService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @program: codedriver
 * @description:
 * @create: 2020-03-26 19:02
 **/
@Service
@Transactional
@AuthAction(action = MATRIX_MODIFY.class)
@OperationType(type = OperationTypeEnum.CREATE)
public class MatrixSaveApi extends PrivateApiComponentBase {

    @Resource
    private MatrixMapper matrixMapper;

    @Resource
    private MatrixExternalMapper externalMapper;

    @Resource
    private MatrixViewMapper viewMapper;

    @Resource
    private MatrixService matrixService;

    @Resource
    private FileMapper fileMapper;

    @Resource
    private SchemaMapper schemaMapper;

    @Override
    public String getToken() {
        return "matrix/save";
    }

    @Override
    public String getName() {
        return "数据源矩阵保存";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "name", type = ApiParamType.STRING, desc = "矩阵名称", isRequired = true, xss = true),
            @Param(name = "label", type = ApiParamType.REGEX, rule = "^[A-Za-z]+$", desc = "矩阵唯一标识", isRequired = true, xss = true),
            @Param(name = "type", type = ApiParamType.STRING, desc = "矩阵类型", isRequired = true),
            @Param(name = "uuid", type = ApiParamType.STRING, desc = "矩阵uuid"),
            @Param(name = "integrationUuid", type = ApiParamType.STRING, desc = "集成设置uuid"),
            @Param(name = "fileId", type = ApiParamType.LONG, desc = "视图配置文件id")
    })
    @Output({
            @Param(name = "matrix", explode = MatrixVo.class, desc = "矩阵数据源")
    })
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject returnObj = new JSONObject();
        MatrixVo matrixVo = JSON.toJavaObject(jsonObj, MatrixVo.class);
        matrixVo.setLcu(UserContext.get().getUserUuid(true));
        boolean isUuidBlank = StringUtils.isBlank(matrixVo.getUuid());
        if (isUuidBlank) {
            matrixVo.setUuid(UuidUtil.randomUuid());
        }
        if (matrixMapper.checkMatrixNameIsRepeat(matrixVo) > 0) {
            throw new MatrixNameRepeatException(matrixVo.getName());
        }
        if (matrixMapper.checkMatrixLabelIsRepeat(matrixVo) > 0) {
            throw new MatrixLabelRepeatException(matrixVo.getLabel());
        }
        if (!isUuidBlank) {
            matrixMapper.updateMatrixNameAndLcu(matrixVo);
        } else {
            matrixVo.setFcu(UserContext.get().getUserUuid(true));
            matrixMapper.insertMatrix(matrixVo);
        }
        if(MatrixType.EXTERNAL.getValue().equals(matrixVo.getType())){
            String integrationUuid = jsonObj.getString("integrationUuid");
            if(StringUtils.isNotBlank(integrationUuid)){
                matrixService.validateMatrixExternalData(integrationUuid);
                MatrixExternalVo externalVo = new MatrixExternalVo(matrixVo.getUuid(),integrationUuid);
                if (externalMapper.getMatrixExternalIsExists(externalVo.getMatrixUuid()) == 0) {
                    externalMapper.insertMatrixExternal(externalVo);
                } else {
                    externalMapper.updateMatrixExternal(externalVo);
                }
            }else{
//                throw new MatrixExternalIntegrationUuidEmptyException();
                throw new ParamNotExistsException("integrationUuid");
            }
        } else if (MatrixType.VIEW.getValue().equals(matrixVo.getType())) {
            Long fileId = jsonObj.getLong("fileId");
            if (fileId == null) {
                throw new ParamNotExistsException("fileId");
            }
            FileVo fileVo = fileMapper.getFileById(fileId);
            if (fileVo == null) {
                throw new FileNotFoundException(fileId);
            }
//            String xml = IOUtils.toString(FileUtil.getData(fileVo.getPath()), StandardCharsets.UTF_8);
            String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><ci><attrs><attr name=\"uuid\" label=\"用户uuid\"/><attr name=\"user_id\" label=\"用户id\"/><attr name=\"user_name\" label=\"用户名\"/><attr name=\"teamName\" label=\"分组\"/><attr name=\"vipLevel\" label=\"是否VIP\"/><attr name=\"phone\" label=\"电话\"/><attr name=\"email\" label=\"邮件\"/></attrs><sql>SELECT `u`.`uuid` AS uuid, `u`.`id` AS id, `u`.`user_name` AS name, `u`.`user_id` as user_id, `u`.`user_name` as user_name, u.email as email, u.phone as phone, if(u.vip_level=0,'否','是') as vipLevel, group_concat( `t`.`name`) AS teamName FROM `user` `u` LEFT JOIN `user_team` `ut` ON `u`.`uuid` = `ut`.`user_uuid` LEFT JOIN `team` `t` ON `t`.`uuid` = `ut`.`team_uuid` GROUP BY u.uuid </sql></ci>";
            if (StringUtils.isBlank(xml)) {
                throw new MatrixViewSettingFileNotFoundException();
            }
            List<MatrixAttributeVo> matrixAttributeVoList = buildView(matrixVo.getUuid(), matrixVo.getName(), xml);
            MatrixViewVo matrixViewVo = new MatrixViewVo();
            matrixViewVo.setMatrixUuid(matrixVo.getUuid());
            matrixViewVo.setFileId(fileId);
            JSONObject config = new JSONObject();
            config.put("attributeList", matrixAttributeVoList);
            matrixViewVo.setConfig(config.toJSONString());
            viewMapper.insertMatrixView(matrixViewVo);
        }
        returnObj.put("matrix", matrixVo);
        return returnObj;
    }

    private List<MatrixAttributeVo> buildView(String matrixUuid, String matrixName, String xml) {

        List<MatrixAttributeVo> processMatrixAttributeList = new ArrayList<>();
        MatrixViewSqlBuilder viewBuilder = new MatrixViewSqlBuilder(xml);
//        viewBuilder.setCiId(ciVo.getId());
        viewBuilder.setViewName("matrix_" + matrixUuid);
        if (viewBuilder.valid()) {
            //测试一下语句是否能正常执行
            try {
                schemaMapper.testCiViewSql(viewBuilder.getTestSql());
            } catch (Exception ex) {
                throw new MatrixViewSqlIrregularException(ex);
            }
            List<AttrVo> attrList = viewBuilder.getAttrList();
            if (CollectionUtils.isNotEmpty(attrList)) {
//                Map<String, Long> attrIdMap = new HashMap<>();
                int sort = 0;
                for (AttrVo attrVo : attrList) {
//                    attrVo.setCiId(ciVo.getId());
//                    attrMapper.insertAttr(attrVo);
//                    attrIdMap.put(attrVo.getName(), attrVo.getId());
                    MatrixAttributeVo processMatrixAttributeVo = new MatrixAttributeVo();
                    processMatrixAttributeVo.setMatrixUuid(matrixUuid);
                    processMatrixAttributeVo.setUuid(attrVo.getName());
                    processMatrixAttributeVo.setName(attrVo.getLabel());
                    processMatrixAttributeVo.setType(MatrixAttributeType.INPUT.getValue());
                    processMatrixAttributeVo.setIsDeletable(0);
                    processMatrixAttributeVo.setSort(sort++);
                    processMatrixAttributeVo.setIsRequired(0);
                    processMatrixAttributeList.add(processMatrixAttributeVo);
                }
//                viewBuilder.setAttrIdMap(attrIdMap);
                EscapeTransactionJob.State s = new EscapeTransactionJob(() -> {
                    System.out.println(TenantContext.get().getDataDbName());
                    if (schemaMapper.checkSchemaIsExists(TenantContext.get().getDataDbName()) > 0) {
                        //创建配置项表
                        String viewSql = viewBuilder.getCreateViewSql();
                        System.out.println(viewSql);
                        schemaMapper.insertView(viewBuilder.getCreateViewSql());
                    } else {
                        throw new DataBaseNotFoundException();
                    }
                }).execute();
                if (!s.isSucceed()) {
                    throw new CreateMatrixViewSchemaException(matrixName, true);
                }
            }
        }
        return processMatrixAttributeList;
    }

    public IValid name() {
        return value -> {
            MatrixVo matrixVo = JSON.toJavaObject(value, MatrixVo.class);
            if (StringUtils.isBlank(matrixVo.getUuid())) {
                matrixVo.setUuid(UuidUtil.randomUuid());
            }
            if (matrixMapper.checkMatrixNameIsRepeat(matrixVo) > 0) {
                return new FieldValidResultVo(new MatrixNameRepeatException(matrixVo.getName()));
            }
            return new FieldValidResultVo();
        };
    }

    public IValid label() {
        return value -> {
            MatrixVo matrixVo = JSON.toJavaObject(value, MatrixVo.class);
            if (StringUtils.isBlank(matrixVo.getUuid())) {
                matrixVo.setUuid(UuidUtil.randomUuid());
            }
            if (matrixMapper.checkMatrixLabelIsRepeat(matrixVo) > 0) {
                return new FieldValidResultVo(new MatrixLabelRepeatException(matrixVo.getLabel()));
            }
            return new FieldValidResultVo();
        };
    }
}

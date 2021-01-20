package codedriver.module.tenant.api.systemnotice;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.constvalue.GroupSearch;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.systemnotice.dao.mapper.SystemNoticeMapper;
import codedriver.framework.systemnotice.dto.SystemNoticeRecipientVo;
import codedriver.framework.systemnotice.dto.SystemNoticeVo;
import codedriver.framework.systemnotice.exception.SystemNoticeHasBeenIssuedException;
import codedriver.module.tenant.auth.label.SYSTEM_NOTICE_MODIFY;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @Title: SystemNoticeSaveApi
 * @Package: codedriver.module.tenant.api.systemnotice
 * @Description: 系统公告保存接口
 * @Author: laiwt
 * @Date: 2021/1/13 18:01
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 **/

@AuthAction(action = SYSTEM_NOTICE_MODIFY.class)
@Service
@OperationType(type = OperationTypeEnum.CREATE)
@Transactional
public class SystemNoticeSaveApi extends PrivateApiComponentBase {

    private final static int BATCH_DELETE_MAX_COUNT = 1000;

    @Autowired
    private SystemNoticeMapper systemNoticeMapper;

    @Override
    public String getToken() {
        return "systemnotice/save";
    }

    @Override
    public String getName() {
        return "保存系统公告";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "id", type = ApiParamType.LONG, desc = "公告ID"),
            @Param(name = "title", type = ApiParamType.STRING, isRequired = true, desc = "标题"),
            @Param(name = "content", type = ApiParamType.STRING, isRequired = true, desc = "内容"),
            @Param(name = "recipientList", type = ApiParamType.JSONARRAY, isRequired = true, desc = "通知对象列表,可多选，格式[\"user#userUuid\",\"team#teamUuid\",\"role#roleUuid\"]"),
    })
    @Output({})
    @Description(desc = "保存系统公告")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        SystemNoticeVo vo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<SystemNoticeVo>() {});

        SystemNoticeVo oldVo = systemNoticeMapper.getSystemNoticeBaseInfoById(vo.getId());

        vo.setLcu(UserContext.get().getUserUuid());
        if (oldVo == null) {
            vo.setFcu(UserContext.get().getUserUuid());
            systemNoticeMapper.insertSystemNotice(vo);
        } else {
            if (SystemNoticeVo.Status.ISSUED.getValue().equals(oldVo.getStatus())) {
                throw new SystemNoticeHasBeenIssuedException(oldVo.getTitle());
            }
            systemNoticeMapper.updateSystemNoticeBaseInfo(vo);
            systemNoticeMapper.deleteRecipientByNoticeId(vo.getId());
        }

        /**保存通知对象*/
        List<SystemNoticeRecipientVo> recipientVoList = null;
        JSONArray recipientList = jsonObj.getJSONArray("recipientList");
        if (CollectionUtils.isNotEmpty(recipientList)) {
            recipientVoList = new ArrayList<>();
            for (Object o : recipientList) {
                String[] split = o.toString().split("#");
                if (GroupSearch.getGroupSearch(split[0]) != null) {
                    SystemNoticeRecipientVo recipientVo = new SystemNoticeRecipientVo();
                    recipientVo.setSystemNoticeId(vo.getId());
                    recipientVo.setType(split[0]);
                    recipientVo.setUuid(split[1]);
                    recipientVoList.add(recipientVo);
                }
            }
        }
        if (CollectionUtils.isNotEmpty(recipientVoList)) {
            systemNoticeMapper.batchInsertSystemNoticeRecipient(recipientVoList);
        }

        return null;
    }
}

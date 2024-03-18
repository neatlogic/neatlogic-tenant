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

package neatlogic.module.tenant.api.systemnotice;

import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.SYSTEM_NOTICE_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.constvalue.GroupSearch;
import neatlogic.framework.dto.FieldValidResultVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.IValid;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.systemnotice.dao.mapper.SystemNoticeMapper;
import neatlogic.framework.systemnotice.dto.SystemNoticeRecipientVo;
import neatlogic.framework.systemnotice.dto.SystemNoticeVo;
import neatlogic.framework.systemnotice.exception.SystemNoticeHasBeenIssuedException;
import neatlogic.framework.systemnotice.exception.SystemNoticeRepeatException;
import neatlogic.framework.util.RegexUtils;
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

@AuthAction(action = SYSTEM_NOTICE_MODIFY.class)
@Service
@OperationType(type = OperationTypeEnum.CREATE)
@Transactional
public class SystemNoticeSaveApi extends PrivateApiComponentBase {

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
            @Param(name = "title", type = ApiParamType.REGEX, rule = RegexUtils.NAME, maxLength = 50,  isRequired = true, desc = "标题"),
            @Param(name = "content", type = ApiParamType.STRING, isRequired = true, desc = "内容"),
            @Param(name = "recipientList", type = ApiParamType.JSONARRAY, isRequired = true, desc = "通知对象列表,可多选，格式[\"user#userUuid\",\"team#teamUuid\",\"role#roleUuid\"]"),
    })
    @Output({})
    @Description(desc = "保存系统公告")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        SystemNoticeVo vo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<SystemNoticeVo>() {});

        if(systemNoticeMapper.checkSystemNoticeNameRepeat(vo) > 0){
            throw new SystemNoticeRepeatException(vo.getTitle());
        }

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

    public IValid title(){
        return value -> {
            SystemNoticeVo noticeVo = JSON.toJavaObject(value, SystemNoticeVo.class);
            if(systemNoticeMapper.checkSystemNoticeNameRepeat(noticeVo) > 0){
                return new FieldValidResultVo(new SystemNoticeRepeatException(noticeVo.getTitle()));
            }
            return new FieldValidResultVo();
        };
    }
}

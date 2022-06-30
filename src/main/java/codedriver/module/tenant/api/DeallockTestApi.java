/*
 * Copyright(c) 2021 TechSureCo.,Ltd.AllRightsReserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.tenant.dao.mapper.DeallockTestMapper;
import codedriver.module.tenant.dto.DeallockTestVo;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@Transactional
public class DeallockTestApi extends PrivateApiComponentBase {

    private final static Logger logger = LoggerFactory.getLogger(DeallockTestApi.class);
    @Resource
    private DeallockTestMapper deallockTestMapper;

    @Override
    public String getToken() {
        return "deallock/test";
    }

    @Override
    public String getName() {
        return "死锁测试";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "id1", type = ApiParamType.LONG, isRequired = true, desc = "Id1"),
            @Param(name = "id2", type = ApiParamType.LONG, isRequired = true, desc = "Id2"),
            @Param(name = "label", type = ApiParamType.STRING, isRequired = true, desc = "标识"),
            @Param(name = "action", type = ApiParamType.STRING, isRequired = true, desc = "标识")
    })
    @Description(desc = "死锁测试")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        String label = paramObj.getString("label");
        String action = paramObj.getString("action");
        Long id1 = paramObj.getLong("id1");
        Long id2 = paramObj.getLong("id2");
        if ("select".equals(action)) {
            DeallockTestVo deallockTest1 = deallockTestMapper.getLockById(id1);
            logger.error(label + "=" + JSONObject.toJSONString(deallockTest1));
            Thread.sleep(5000);
            DeallockTestVo deallockTest2 = deallockTestMapper.getLockById(id2);
            logger.error(label + "=" + JSONObject.toJSONString(deallockTest2));
        } else if ("insert".equals(action)) {
            DeallockTestVo deallockTest1 = new DeallockTestVo(id1);
            deallockTestMapper.insert(deallockTest1);
            logger.error(label + "=" + JSONObject.toJSONString(deallockTest1));
            Thread.sleep(5000);
            DeallockTestVo deallockTest2 = new DeallockTestVo(id2);
            deallockTestMapper.insert(deallockTest2);
            logger.error(label + "=" + JSONObject.toJSONString(deallockTest2));
        } else if ("update".equals(action)) {
            DeallockTestVo deallockTest1 = new DeallockTestVo(id1);
            deallockTestMapper.update(deallockTest1);
            logger.error(label + "=" + JSONObject.toJSONString(deallockTest1));
            Thread.sleep(5000);
            DeallockTestVo deallockTest2 = new DeallockTestVo(id2);
            deallockTestMapper.update(deallockTest2);
            logger.error(label + "=" + JSONObject.toJSONString(deallockTest2));
        } else if ("delete".equals(action)) {
            deallockTestMapper.delete(id1);
            logger.error(label + "=" + JSONObject.toJSONString(id1));
            Thread.sleep(5000);
            deallockTestMapper.delete(id2);
            logger.error(label + "=" + JSONObject.toJSONString(id2));
        }
        return null;
    }
}

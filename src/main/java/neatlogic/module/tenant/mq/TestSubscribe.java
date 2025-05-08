/*
 * Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package neatlogic.module.tenant.mq;

import neatlogic.framework.common.config.Config;
import neatlogic.framework.mq.core.SubscribeHandlerBase;
import neatlogic.framework.mq.dto.SubscribeVo;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.TextMessage;

@Component
public class TestSubscribe extends SubscribeHandlerBase {

    @Override
    protected void myOnMessage(SubscribeVo subscribeVo, Object message) {
        System.out.println("############消息队列测试处理器开始接收##############");
        System.out.println("serverId:" + Config.SCHEDULE_SERVER_ID);
        if (message instanceof TextMessage) {
            TextMessage textMessage = (TextMessage) message;
            try {
                System.out.println(textMessage.getText());
            } catch (JMSException e) {
                throw new RuntimeException(e);
            }
        } else {
            System.out.println(message);
        }
        System.out.println("############消息队列测试处理器结束接收##############");
    }

    @Override
    public String getName() {
        return "TEST_SUBSCRIBE";
    }

    @Override
    public String getLabel() {
        return "测试处理器";
    }
}

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

/*
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package neatlogic.module.tenant.service.featureusage;

import neatlogic.framework.asynchronization.thread.NeatLogicThread;
import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.dao.mapper.FeatureUsageAuditMapper;
import neatlogic.framework.dto.featureusageaudit.FeatureUsageAuditVo;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.DelayQueue;

@Service
public class FeatureUsageServiceImpl implements FeatureUsageService {
    private static final Logger logger = LoggerFactory.getLogger(FeatureUsageServiceImpl.class);
    private static final int INSERT_BATCH_SIZE = 500;
    /**
     * 统计延迟对象，默认初始化一个失效的延迟对象
     **/
    private static volatile DelayedItem delayedItem = new DelayedItem(true);
    /**
     * 延迟队列
     **/
    private static final DelayQueue<DelayedItem> delayQueue = new DelayQueue<>();

    @Resource
    private FeatureUsageAuditMapper featureUsageAuditMapper;

    @PostConstruct
    public void init() {
        Thread t = new Thread(new NeatLogicThread("FEATURE-USAGE-MANAGER") {

            @Override
            protected void execute() {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        DelayedItem take = delayQueue.take();
                        /* 从延迟队列取出延迟对象后，将延迟对象设置为失效，通知其他线程不要再往该对象写数据了 **/
                        take.setExpired(true);
                        while (take.getWritingDataThreadNum() > 0) {
                            /* 如果还有线程正在往当前延迟对象中写数据 **/
                            synchronized (take.getLock()) {
                                /* 等待所有正在往当前延迟对象中写数据的线程完成后，唤醒当前线程 **/
                                take.getLock().wait();
                            }
                        }
                        /** 业务代码开始**/
                        for (Map.Entry<String, ConcurrentMap<FeatureUsageAuditVo, Object>> tenantAccessTokenEntry : take.getTenantMap().entrySet()) {
                            TenantContext.init(tenantAccessTokenEntry.getKey());
                            // 按租户批量写入功能使用审计，分片避免单条 SQL 过大。
//                            List<FeatureUsageAuditVo> featureUsageAuditVoList = new ArrayList<>(tenantAccessTokenEntry.getValue().keySet());
//                            for (int fromIndex = 0; fromIndex < featureUsageAuditVoList.size(); fromIndex += INSERT_BATCH_SIZE) {
//                                int toIndex = Math.min(fromIndex + INSERT_BATCH_SIZE, featureUsageAuditVoList.size());
//                                featureUsageAuditMapper.insertFeatureUsageAuditList(featureUsageAuditVoList.subList(fromIndex, toIndex));
//                            }
                            List<FeatureUsageAuditVo> featureUsageAuditVoList = new ArrayList<>();
                            for (Map.Entry<FeatureUsageAuditVo, Object> entry : tenantAccessTokenEntry.getValue().entrySet()) {
                                featureUsageAuditVoList.add(entry.getKey());
                                if (featureUsageAuditVoList.size() >= 100) {
                                    featureUsageAuditMapper.insertFeatureUsageAuditList(featureUsageAuditVoList);
                                    featureUsageAuditVoList.clear();
                                }
                            }
                            if (CollectionUtils.isNotEmpty(featureUsageAuditVoList)) {
                                featureUsageAuditMapper.insertFeatureUsageAuditList(featureUsageAuditVoList);
                                featureUsageAuditVoList.clear();
                            }
                        }
                        /** 业务代码结束**/
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
        });
        t.setDaemon(true);
        t.start();
    }

    @Override
    public void addFeatureUsageAuditVo(FeatureUsageAuditVo featureUsageAuditVo) {
        try {
            /** 判断延迟对象是否失效 **/
            if (!delayedItem.addFeatureUsageAuditVo(featureUsageAuditVo)) {
                /** 初始化延迟对象时，必须加锁，否则会出现多个线程相互覆盖情况 **/
                synchronized (this) {
                    if (delayedItem.isExpired()) {
                        delayedItem = new DelayedItem();
                        delayQueue.add(delayedItem);
                    }
                }
                delayedItem.addFeatureUsageAuditVo(featureUsageAuditVo);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}

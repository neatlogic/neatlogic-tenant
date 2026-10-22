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
package neatlogic.module.tenant.service.selfcheck;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.thread.NeatLogicThread;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.asynchronization.threadpool.CachedThreadPool;
import neatlogic.framework.common.config.Config;
import neatlogic.framework.exception.core.ApiRuntimeException;
import neatlogic.framework.restful.core.IApiComponent;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentFactory;
import neatlogic.framework.restful.dto.ApiVo;
import neatlogic.framework.selfcheck.annotation.SelfCheckApi;
import neatlogic.framework.selfcheck.constvalue.SelfCheckRisk;
import neatlogic.framework.selfcheck.core.ISelfCheckCleanupApi;
import neatlogic.framework.selfcheck.core.SelfCheckContextHolder;
import neatlogic.framework.selfcheck.dto.SelfCheckContextVo;
import neatlogic.framework.selfcheck.dto.SelfCheckScriptContextVo;
import neatlogic.framework.selfcheck.dto.SelfCheckScriptResultVo;
import neatlogic.framework.selfcheck.service.SelfCheckScriptService;
import neatlogic.framework.transaction.core.AfterTransactionJob;
import neatlogic.module.tenant.dao.mapper.SelfCheckMapper;
import neatlogic.module.tenant.dto.selfcheck.SelfCheckRunItemVo;
import neatlogic.module.tenant.dto.selfcheck.SelfCheckRunVo;
import neatlogic.module.tenant.dto.selfcheck.SelfCheckScenarioItemVo;
import neatlogic.module.tenant.dto.selfcheck.SelfCheckScenarioVo;
import neatlogic.module.tenant.selfcheck.constvalue.SelfCheckCleanupStatus;
import neatlogic.module.tenant.selfcheck.constvalue.SelfCheckRunItemStatus;
import neatlogic.module.tenant.selfcheck.constvalue.SelfCheckRunStatus;
import neatlogic.module.tenant.selfcheck.constvalue.SelfCheckScenarioStatus;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class SelfCheckServiceImpl implements SelfCheckService {
    private static final Logger logger = LoggerFactory.getLogger(SelfCheckServiceImpl.class);
    private static final int DEFAULT_SCRIPT_TIMEOUT = 3;
    private static final int MAX_SCRIPT_TIMEOUT = 10;
    private static final int DEFAULT_API_TIMEOUT = 30;
    private static final int MAX_API_TIMEOUT = 120;
    private static final int MAX_SNAPSHOT_BYTES = 1024 * 1024;
    private static final Set<String> DEFAULT_SENSITIVE_KEY_SET = new HashSet<>(Arrays.asList(
            "password", "passwd", "pwd", "token", "accessToken", "refreshToken", "secret", "authorization", "cookie"
    ));

    @Resource
    private SelfCheckMapper selfCheckMapper;
    @Resource
    private SelfCheckScriptService selfCheckScriptService;
    /** 按模块分组查询当前租户已激活且通过代码准入的 API。 */
    @Override
    public JSONObject searchTarget(JSONObject paramObj) {
        String keyword = StringUtils.trimToEmpty(paramObj.getString("keyword"));
        Set<String> defaultTokenSet = new LinkedHashSet<>();
        JSONArray defaultValue = paramObj.getJSONArray("defaultValue");
        if (defaultValue != null) {
            for (Object value : defaultValue) {
                if (value != null) {
                    defaultTokenSet.add(String.valueOf(value));
                }
            }
        }

        List<ApiTarget> matchedTargetList = new ArrayList<>();
        Map<String, ApiTarget> defaultTargetMap = new LinkedHashMap<>();
        Map<String, Integer> moduleOrderMap = new LinkedHashMap<>();
        int moduleOrder = 0;
        List<ApiVo> activeApiList;
        try {
            activeApiList = PrivateApiComponentFactory.getTenantActiveApiList();
        } catch (CloneNotSupportedException ex) {
            throw new ApiRuntimeException(ex);
        }
        for (ApiVo apiVo : activeApiList) {
            String moduleGroup = StringUtils.defaultIfBlank(apiVo.getModuleGroup(), apiVo.getModuleId());
            if (!moduleOrderMap.containsKey(moduleGroup)) {
                moduleOrderMap.put(moduleGroup, moduleOrder++);
            }
            IApiComponent component = PrivateApiComponentFactory.getComponentMap().get(apiVo.getHandler());
            if (component == null) {
                continue;
            }
            SelfCheckApi selfCheckApi = AopUtils.getTargetClass(component).getAnnotation(SelfCheckApi.class);
            if (selfCheckApi == null) {
                continue;
            }
            if (selfCheckApi.risk() == SelfCheckRisk.SELF_CLEANING && !(component instanceof ISelfCheckCleanupApi)) {
                continue;
            }
            ApiTarget target = new ApiTarget(apiVo, component, selfCheckApi);
            if (matches(keyword, apiVo.getToken(), apiVo.getName(), apiVo.getModuleId(),
                    apiVo.getModuleGroup(), apiVo.getModuleGroupName())) {
                matchedTargetList.add(target);
            }
            if (defaultTokenSet.contains(apiVo.getToken())) {
                defaultTargetMap.put(apiVo.getToken(), target);
            }
        }
        matchedTargetList.sort(Comparator
                .comparing((ApiTarget target) -> moduleOrderMap.getOrDefault(
                        StringUtils.defaultIfBlank(target.apiVo.getModuleGroup(), target.apiVo.getModuleId()), Integer.MAX_VALUE))
                .thenComparing(target -> StringUtils.defaultString(target.apiVo.getName()), String.CASE_INSENSITIVE_ORDER)
                .thenComparing(target -> target.apiVo.getToken(), String.CASE_INSENSITIVE_ORDER));

        int currentPage = normalizePageValue(paramObj.getInteger("currentPage"), 1, Integer.MAX_VALUE);
        int pageSize = normalizePageValue(paramObj.getInteger("pageSize"), 50, 100);
        int rowNum = matchedTargetList.size();
        int start = Math.min((currentPage - 1) * pageSize, rowNum);
        int end = Math.min(start + pageSize, rowNum);
        List<ApiTarget> pageTargetList = new ArrayList<>(matchedTargetList.subList(start, end));
        Set<String> pageTokenSet = new HashSet<>();
        for (ApiTarget target : pageTargetList) {
            pageTokenSet.add(target.apiVo.getToken());
        }
        for (String token : defaultTokenSet) {
            ApiTarget target = defaultTargetMap.get(token);
            if (target != null && pageTokenSet.add(token)) {
                pageTargetList.add(target);
            }
        }
        return buildGroupedTargetResult(pageTargetList, rowNum, currentPage, pageSize);
    }

    /** 保存场景，并使用后端脚本引擎固化校验结果。 */
    @Override
    @Transactional
    public SelfCheckScenarioVo saveScenario(SelfCheckScenarioVo scenarioVo) {
        if (StringUtils.isBlank(scenarioVo.getStatus())) {
            scenarioVo.setStatus(SelfCheckScenarioStatus.DRAFT.name());
        }
        validateScenarioStatus(scenarioVo.getStatus());
        validateAndFillItemList(scenarioVo);
        boolean enabled = Objects.equals(scenarioVo.getStatus(), SelfCheckScenarioStatus.ENABLED.name());
        if (enabled && hasInvalidActiveItem(scenarioVo.getItemList())) {
            throw new ApiRuntimeException("启用场景前必须修正所有已启用检查项的脚本");
        }
        String userUuid = UserContext.get().getUserUuid();
        SelfCheckScenarioVo oldScenario = selfCheckMapper.getScenarioById(scenarioVo.getId());
        if (oldScenario == null) {
            scenarioVo.setVersion(1);
            scenarioVo.setFcu(userUuid);
            scenarioVo.setLcu(userUuid);
            selfCheckMapper.insertScenario(scenarioVo);
        } else {
            scenarioVo.setLcu(userUuid);
            if (scenarioVo.getVersion() == null) {
                scenarioVo.setVersion(oldScenario.getVersion());
            }
            if (selfCheckMapper.updateScenario(scenarioVo) == 0) {
                throw new ApiRuntimeException("自检场景已被其他用户修改，请刷新后重试");
            }
            scenarioVo.setVersion(scenarioVo.getVersion() + 1);
            selfCheckMapper.deleteScenarioItemByScenarioId(scenarioVo.getId());
        }
        int sort = 1;
        if (CollectionUtils.isNotEmpty(scenarioVo.getItemList())) {
            for (SelfCheckScenarioItemVo itemVo : scenarioVo.getItemList()) {
                itemVo.setScenarioId(scenarioVo.getId());
                if (itemVo.getSort() == null) {
                    itemVo.setSort(sort);
                }
                selfCheckMapper.insertScenarioItem(itemVo);
                sort++;
            }
        }
        return getScenario(scenarioVo.getId());
    }

    /** 获取场景及其有序检查项。 */
    @Override
    public SelfCheckScenarioVo getScenario(Long id) {
        SelfCheckScenarioVo scenarioVo = selfCheckMapper.getScenarioById(id);
        if (scenarioVo == null) {
            throw new ApiRuntimeException("自检场景不存在");
        }
        List<SelfCheckScenarioItemVo> itemList = selfCheckMapper.getScenarioItemListByScenarioId(id);
        for (SelfCheckScenarioItemVo itemVo : itemList) {
            fillApiMetadata(itemVo);
        }
        scenarioVo.setItemList(itemList);
        return scenarioVo;
    }

    /** 分页查询场景。 */
    @Override
    public JSONObject searchScenario(SelfCheckScenarioVo searchVo) {
        int rowNum = selfCheckMapper.searchScenarioCount(searchVo);
        searchVo.setRowNum(rowNum);
        JSONObject result = new JSONObject();
        if (rowNum == 0) {
            result.put("tbodyList", Collections.emptyList());
        } else {
            result.put("tbodyList", selfCheckMapper.searchScenario(searchVo));
        }
        result.put("rowNum", rowNum);
        result.put("currentPage", searchVo.getCurrentPage());
        result.put("pageSize", searchVo.getPageSize());
        result.put("pageCount", searchVo.getPageCount());
        return result;
    }

    /** 删除不在执行中的场景。 */
    @Override
    @Transactional
    public void deleteScenario(Long id) {
        if (selfCheckMapper.countRunningRunByScenarioId(id) > 0) {
            throw new ApiRuntimeException("自检场景正在执行，不能删除");
        }
        selfCheckMapper.deleteScenarioItemByScenarioId(id);
        selfCheckMapper.deleteScenarioById(id);
    }

    /** 通过乐观锁更新场景状态，启用前重新校验脚本。 */
    @Override
    public void updateScenarioStatus(Long id, String status, Integer version) {
        validateScenarioStatus(status);
        SelfCheckScenarioVo scenarioVo = getScenario(id);
        if (Objects.equals(status, SelfCheckScenarioStatus.ENABLED.name())) {
            validateActiveScripts(scenarioVo.getItemList());
            if (hasInvalidActiveItem(scenarioVo.getItemList())) {
                throw new ApiRuntimeException("启用场景前必须修正所有已启用检查项的脚本");
            }
        }
        if (selfCheckMapper.updateScenarioStatus(id, status, version, UserContext.get().getUserUuid()) == 0) {
            throw new ApiRuntimeException("自检场景已被其他用户修改，请刷新后重试");
        }
    }

    /** 仅编译脚本并返回诊断。 */
    @Override
    public SelfCheckScriptResultVo validateScript(String script) {
        return selfCheckScriptService.validate(script);
    }

    /** 按正式链路调试单个未保存检查项，并保留审计记录。 */
    @Override
    public SelfCheckRunItemVo testItem(JSONObject paramObj) {
        JSONObject itemBaseObj = new JSONObject(paramObj);
        itemBaseObj.remove("requestParam");
        SelfCheckScenarioItemVo itemVo = itemBaseObj.toJavaObject(SelfCheckScenarioItemVo.class);
        itemVo.setRequestParamObj(paramObj.getJSONObject("requestParam"));
        fillAndValidateItem(itemVo);
        SelfCheckRunVo runVo = new SelfCheckRunVo();
        runVo.setId(null);
        runVo.setScenarioId(null);
        runVo.setTriggerType("DEBUG");
        runVo.setStatus(SelfCheckRunStatus.RUNNING.name());
        runVo.setServerId(Config.SCHEDULE_SERVER_ID);
        runVo.setExecutorUuid(UserContext.get().getUserUuid());
        runVo.setFcu(runVo.getExecutorUuid());
        runVo.setStartTime(new Date());
        runVo.setScenarioName("单项调试");
        runVo.setItemCount(1);
        runVo.setSuccessCount(0);
        runVo.setFailedCount(0);
        selfCheckMapper.insertRun(runVo);
        SelfCheckRunItemVo result = executeItem(runVo, itemVo);
        runVo.setEndTime(new Date());
        runVo.setDurationMs(runVo.getEndTime().getTime() - runVo.getStartTime().getTime());
        if (Objects.equals(result.getStatus(), SelfCheckRunItemStatus.SUCCEED.name())) {
            runVo.setStatus(SelfCheckRunStatus.SUCCEED.name());
            runVo.setSuccessCount(1);
            runVo.setFailedCount(0);
        } else {
            runVo.setStatus(SelfCheckRunStatus.FAILED.name());
            runVo.setSuccessCount(0);
            runVo.setFailedCount(1);
        }
        selfCheckMapper.updateRun(runVo);
        return result;
    }

    /** 创建异步场景执行并通过事务后任务启动。 */
    @Override
    @Transactional
    public SelfCheckRunVo createRun(Long scenarioId) {
        SelfCheckScenarioVo scenarioVo = selfCheckMapper.getScenarioLockById(scenarioId);
        if (scenarioVo == null) {
            throw new ApiRuntimeException("自检场景不存在");
        }
        scenarioVo.setItemList(selfCheckMapper.getScenarioItemListByScenarioId(scenarioId));
        if (!Objects.equals(scenarioVo.getStatus(), SelfCheckScenarioStatus.ENABLED.name())) {
            throw new ApiRuntimeException("只有已启用的自检场景才能正式执行");
        }
        if (hasInvalidActiveItem(scenarioVo.getItemList())) {
            throw new ApiRuntimeException("场景中存在脚本无效的检查项");
        }
        SelfCheckRunVo runVo = new SelfCheckRunVo();
        runVo.setScenarioId(scenarioId);
        runVo.setScenarioName(scenarioVo.getName());
        runVo.setScenarioVersion(scenarioVo.getVersion());
        runVo.setTriggerType("MANUAL");
        runVo.setStatus(SelfCheckRunStatus.PENDING.name());
        runVo.setRunningKey("SCENARIO:" + scenarioId);
        runVo.setServerId(Config.SCHEDULE_SERVER_ID);
        runVo.setExecutorUuid(UserContext.get().getUserUuid());
        runVo.setFcu(runVo.getExecutorUuid());
        runVo.setItemCount(countActiveItem(scenarioVo.getItemList()));
        runVo.setSuccessCount(0);
        runVo.setFailedCount(0);
        try {
            selfCheckMapper.insertRun(runVo);
            NeatLogicThread thread = new NeatLogicThread("SELF-CHECK-RUN-" + runVo.getId(), true) {
                @Override
                protected void execute() {
                    executeScenario(runVo, scenarioVo);
                }
            };
            AfterTransactionJob<NeatLogicThread> afterTransactionJob = new AfterTransactionJob<>("SELF-CHECK-RUN-MANAGER");
            afterTransactionJob.execute(thread);
        } catch (DuplicateKeyException ex) {
            throw new ApiRuntimeException("该自检场景正在执行，请勿重复提交");
        }
        return runVo;
    }

    /** 分页查询执行历史。 */
    @Override
    public JSONObject searchRun(SelfCheckRunVo searchVo) {
        int rowNum = selfCheckMapper.searchRunCount(searchVo);
        searchVo.setRowNum(rowNum);
        JSONObject result = new JSONObject();
        if (rowNum == 0) {
            result.put("tbodyList", Collections.emptyList());
        } else {
            result.put("tbodyList", selfCheckMapper.searchRun(searchVo));
        }
        result.put("rowNum", rowNum);
        result.put("currentPage", searchVo.getCurrentPage());
        result.put("pageSize", searchVo.getPageSize());
        result.put("pageCount", searchVo.getPageCount());
        return result;
    }

    /** 获取执行记录及检查项完整阶段证据。 */
    @Override
    public JSONObject getRun(Long runId) {
        SelfCheckRunVo runVo = selfCheckMapper.getRunById(runId);
        if (runVo == null) {
            throw new ApiRuntimeException("自检执行记录不存在");
        }
        JSONObject result = (JSONObject) JSON.toJSON(runVo);
        List<JSONObject> itemList = new ArrayList<>();
        for (SelfCheckRunItemVo itemVo : selfCheckMapper.getRunItemListByRunId(runId)) {
            itemList.add(itemVo.toDetailJson());
        }
        result.put("itemList", itemList);
        return result;
    }

    /** 按配置顺序执行场景，单项失败不阻断后续检查项。 */
    private void executeScenario(SelfCheckRunVo runVo, SelfCheckScenarioVo scenarioVo) {
        Date startTime = new Date();
        int successCount = 0;
        int failedCount = 0;
        try {
            runVo.setStatus(SelfCheckRunStatus.RUNNING.name());
            runVo.setStartTime(startTime);
            selfCheckMapper.updateRun(runVo);
            if (CollectionUtils.isNotEmpty(scenarioVo.getItemList())) {
                for (SelfCheckScenarioItemVo itemVo : scenarioVo.getItemList()) {
                    if (!Objects.equals(itemVo.getIsActive(), 1)) {
                        continue;
                    }
                    try {
                        SelfCheckRunItemVo itemResult = executeItem(runVo, itemVo);
                        if (Objects.equals(itemResult.getStatus(), SelfCheckRunItemStatus.SUCCEED.name())) {
                            successCount++;
                        } else {
                            failedCount++;
                        }
                    } catch (Exception ex) {
                        failedCount++;
                        recordUnexpectedItemFailure(runVo, itemVo, ex);
                    }
                }
            }
            if (failedCount == 0) {
                runVo.setStatus(SelfCheckRunStatus.SUCCEED.name());
            } else {
                runVo.setStatus(SelfCheckRunStatus.FAILED.name());
            }
        } catch (Exception ex) {
            runVo.setStatus(SelfCheckRunStatus.FAILED.name());
            runVo.setErrorMessage(limitText(ex.getMessage(), 2000));
            failedCount++;
        } finally {
            Date endTime = new Date();
            runVo.setEndTime(endTime);
            runVo.setDurationMs(endTime.getTime() - startTime.getTime());
            runVo.setSuccessCount(successCount);
            runVo.setFailedCount(failedCount);
            runVo.setRunningKey(null);
            selfCheckMapper.updateRun(runVo);
        }
    }

    /** 执行脚本编译、目标调用、断言和强制清理的完整检查项链路。 */
    private SelfCheckRunItemVo executeItem(SelfCheckRunVo runVo, SelfCheckScenarioItemVo itemVo) {
        SelfCheckRunItemVo resultVo = createRunItem(runVo, itemVo);
        JSONArray phaseList = new JSONArray();
        selfCheckMapper.insertRunItem(resultVo);
        Date startTime = new Date();
        resultVo.setStartTime(startTime);
        resultVo.setStatus(SelfCheckRunItemStatus.RUNNING.name());
        TargetExecution targetExecution = null;
        SelfCheckApi selfCheckApi = null;
        Date targetStartTime = null;
        Throwable targetException = null;
        try {
            SelfCheckScriptResultVo validation = selfCheckScriptService.validate(itemVo.getVerifyScript());
            phaseList.add(buildScriptPhase("SCRIPT_COMPILE", validation));
            if (!Boolean.TRUE.equals(validation.getValid())) {
                copyScriptResult(resultVo, validation);
                resultVo.setStatus(SelfCheckRunItemStatus.SCRIPT_SYNTAX_ERROR.name());
                return finishRunItem(resultVo, phaseList, startTime);
            }

            selfCheckApi = resolveApiTarget(itemVo.getApiToken()).annotation;
            targetStartTime = new Date();
            try {
                targetExecution = executeTargetWithTimeout(runVo, resultVo, itemVo);
                phaseList.add(buildSuccessPhase("TARGET_EXECUTE", targetStartTime, targetExecution.endTime));
                Snapshot responseSnapshot = buildSnapshot(targetExecution.response, getSensitiveResultPathList(selfCheckApi));
                resultVo.setResponseSnapshot(responseSnapshot.value);
                if (responseSnapshot.truncated) {
                    addTruncatedField(resultVo, "responseSnapshot");
                }
            } catch (TimeoutException ex) {
                targetException = ex;
                logger.error("功能自检目标执行超时，traceId={}", resultVo.getTraceId(), ex);
                phaseList.add(buildErrorPhase("TARGET_EXECUTE", targetStartTime, new Date(), "TimeoutException", "目标执行超时", resultVo.getTraceId()));
                resultVo.setStatus(SelfCheckRunItemStatus.TARGET_TIMEOUT.name());
                return finishRunItem(resultVo, phaseList, startTime);
            } catch (Exception ex) {
                targetException = ex;
                logger.error("功能自检目标执行失败，traceId={}", resultVo.getTraceId(), ex);
                phaseList.add(buildErrorPhase("TARGET_EXECUTE", targetStartTime, new Date(), ex.getClass().getSimpleName(), ex.getMessage(), resultVo.getTraceId()));
                resultVo.setStatus(SelfCheckRunItemStatus.TARGET_ERROR.name());
                return finishRunItem(resultVo, phaseList, startTime);
            }

            SelfCheckScriptContextVo contextVo = buildScriptContext(runVo, resultVo, itemVo, targetExecution);
            SelfCheckScriptResultVo scriptResult = selfCheckScriptService.execute(
                    itemVo.getVerifyScript(), contextVo, normalizeTimeout(itemVo.getScriptTimeout(), DEFAULT_SCRIPT_TIMEOUT, MAX_SCRIPT_TIMEOUT));
            phaseList.add(buildScriptPhase("SCRIPT_EXECUTE", scriptResult));
            copyScriptResult(resultVo, scriptResult);
            resultVo.setStatus(scriptResult.getStatus());
            return finishRunItem(resultVo, phaseList, startTime);
        } catch (Exception ex) {
            targetException = ex;
            logger.error("功能自检检查项执行失败，traceId={}", resultVo.getTraceId(), ex);
            phaseList.add(buildErrorPhase("ORCHESTRATE", startTime, new Date(), ex.getClass().getSimpleName(), ex.getMessage(), resultVo.getTraceId()));
            resultVo.setStatus(SelfCheckRunItemStatus.TARGET_ERROR.name());
            return finishRunItem(resultVo, phaseList, startTime);
        } finally {
            if (selfCheckApi != null && selfCheckApi.risk() == SelfCheckRisk.SELF_CLEANING) {
                Date cleanupStartTime = new Date();
                resultVo.setCleanupStatus(SelfCheckCleanupStatus.PENDING.name());
                try {
                    cleanupTarget(runVo, resultVo, itemVo, targetExecution, targetStartTime, targetException);
                    resultVo.setCleanupStatus(SelfCheckCleanupStatus.SUCCEED.name());
                    phaseList.add(buildSuccessPhase("CLEANUP", cleanupStartTime, new Date()));
                } catch (Exception ex) {
                    logger.error("功能自检清理失败，traceId={}", resultVo.getTraceId(), ex);
                    resultVo.setCleanupStatus(SelfCheckCleanupStatus.FAILED.name());
                    resultVo.setStatus(SelfCheckRunItemStatus.CLEANUP_FAILED.name());
                    phaseList.add(buildErrorPhase("CLEANUP", cleanupStartTime, new Date(), ex.getClass().getSimpleName(), ex.getMessage(), resultVo.getTraceId()));
                }
                finishRunItem(resultVo, phaseList, startTime);
            }
        }
    }

    /** 固化检查项当前阶段结果与耗时。 */
    private SelfCheckRunItemVo finishRunItem(SelfCheckRunItemVo resultVo, JSONArray phaseList, Date startTime) {
        Date endTime = new Date();
        resultVo.setPhaseList(phaseList.toJSONString());
        resultVo.setEndTime(endTime);
        resultVo.setDurationMs(endTime.getTime() - startTime.getTime());
        selfCheckMapper.updateRunItem(resultVo);
        return resultVo;
    }

    /** 在独立线程中执行目标，并在超时时等待目标实际退出后再返回。 */
    private TargetExecution executeTargetWithTimeout(SelfCheckRunVo runVo, SelfCheckRunItemVo runItemVo,
                                                     SelfCheckScenarioItemVo itemVo) throws Exception {
        FutureTask<TargetExecution> task = new FutureTask<>(() -> invokeTarget(runVo, runItemVo, itemVo));
        CountDownLatch workerFinishedLatch = new CountDownLatch(1);
        CachedThreadPool.execute(new NeatLogicThread("SELF-CHECK-TARGET-" + runItemVo.getId()) {
            @Override
            protected void execute() {
                try {
                    task.run();
                } finally {
                    workerFinishedLatch.countDown();
                }
            }
        });
        int timeout = normalizeTimeout(itemVo.getApiTimeout(), DEFAULT_API_TIMEOUT, MAX_API_TIMEOUT);
        try {
            return task.get(timeout, TimeUnit.SECONDS);
        } catch (java.util.concurrent.TimeoutException ex) {
            task.cancel(true);
            // 必须等待目标线程真正退出，避免外层清理与仍在写入数据的目标 API 并发执行。
            awaitWorkerFinished(workerFinishedLatch);
            throw new TimeoutException("目标执行超过 " + timeout + " 秒");
        }
    }

    /**
     * 不可中断地等待目标线程退出；等待期间收到中断时，在目标退出后恢复中断标记。
     */
    private void awaitWorkerFinished(CountDownLatch workerFinishedLatch) {
        boolean interrupted = false;
        while (workerFinishedLatch.getCount() > 0) {
            try {
                workerFinishedLatch.await();
            } catch (InterruptedException ex) {
                interrupted = true;
            }
        }
        if (interrupted) {
            Thread.currentThread().interrupt();
        }
    }

    /** 调用已通过显式准入的内部对象型 API。 */
    private TargetExecution invokeTarget(SelfCheckRunVo runVo, SelfCheckRunItemVo runItemVo,
                                         SelfCheckScenarioItemVo itemVo) throws Exception {
        Date startTime = new Date();
        SelfCheckContextVo contextVo = buildExecutionContext(runVo, runItemVo, itemVo, startTime);
        SelfCheckContextHolder.set(contextVo);
        try {
            ApiTarget apiTarget = resolveApiTarget(itemVo.getApiToken());
            Object response = apiTarget.component.doService(apiTarget.apiVo, itemVo.getRequestParamObj(), null);
            Date endTime = new Date();
            contextVo.setResponse(response);
            contextVo.setTargetEndTime(endTime);
            return new TargetExecution(response, startTime, endTime, apiTarget.annotation);
        } catch (Exception ex) {
            contextVo.setException(ex);
            contextVo.setTargetEndTime(new Date());
            throw ex;
        } finally {
            SelfCheckContextHolder.clear();
        }
    }

    /** 执行写入型 API 的幂等清理契约。 */
    private void cleanupTarget(SelfCheckRunVo runVo, SelfCheckRunItemVo runItemVo,
                               SelfCheckScenarioItemVo itemVo, TargetExecution targetExecution,
                               Date targetStartTime, Throwable targetException) {
        ApiTarget apiTarget = resolveApiTarget(itemVo.getApiToken());
        if (!(apiTarget.component instanceof ISelfCheckCleanupApi)) {
            throw new ApiRuntimeException("写入型自检 API 未实现清理契约");
        }
        Date startTime = targetStartTime;
        if (targetExecution != null) {
            startTime = targetExecution.startTime;
        }
        if (startTime == null) {
            startTime = new Date();
        }
        SelfCheckContextVo contextVo = buildExecutionContext(runVo, runItemVo, itemVo, startTime);
        if (targetExecution != null) {
            contextVo.setResponse(targetExecution.response);
            contextVo.setTargetEndTime(targetExecution.endTime);
        } else {
            contextVo.setTargetEndTime(new Date());
        }
        contextVo.setException(targetException);
        SelfCheckContextHolder.set(contextVo);
        try {
            ((ISelfCheckCleanupApi) apiTarget.component).cleanup(contextVo);
        } finally {
            SelfCheckContextHolder.clear();
        }
    }

    /** 解析 API 并再次校验显式准入与清理契约。 */
    private ApiTarget resolveApiTarget(String token) {
        try {
            ApiVo apiVo = null;
            for (ApiVo activeApiVo : PrivateApiComponentFactory.getTenantActiveApiList()) {
                if (Objects.equals(activeApiVo.getToken(), token)) {
                    apiVo = activeApiVo;
                    break;
                }
            }
            if (apiVo == null) {
                throw new ApiRuntimeException("自检 API 不存在、未加载或所属模块未激活：{0}", token);
            }
            IApiComponent component = PrivateApiComponentFactory.getComponentMap().get(apiVo.getHandler());
            if (component == null) {
                throw new ApiRuntimeException("自检 API 不是对象型接口：{0}", token);
            }
            SelfCheckApi annotation = AopUtils.getTargetClass(component).getAnnotation(SelfCheckApi.class);
            if (annotation == null) {
                throw new ApiRuntimeException("接口未通过自检准入：{0}", token);
            }
            if (annotation.risk() == SelfCheckRisk.SELF_CLEANING && !(component instanceof ISelfCheckCleanupApi)) {
                throw new ApiRuntimeException("写入型自检 API 未实现清理契约：{0}", token);
            }
            return new ApiTarget(apiVo, component, annotation);
        } catch (CloneNotSupportedException ex) {
            throw new ApiRuntimeException(ex);
        }
    }

    /** 构造只包含 JSON 数据的受限脚本上下文。 */
    private SelfCheckScriptContextVo buildScriptContext(SelfCheckRunVo runVo, SelfCheckRunItemVo runItemVo,
                                                        SelfCheckScenarioItemVo itemVo, TargetExecution targetExecution) {
        SelfCheckScriptContextVo contextVo = new SelfCheckScriptContextVo();
        contextVo.setRequest(itemVo.getRequestParamObj());
        contextVo.setResponse(JSON.toJSON(targetExecution.response));
        ApiTarget apiTarget = resolveApiTarget(itemVo.getApiToken());
        JSONObject target = new JSONObject();
        target.put("token", apiTarget.apiVo.getToken());
        target.put("name", apiTarget.apiVo.getName());
        target.put("moduleId", apiTarget.apiVo.getModuleId());
        target.put("risk", apiTarget.annotation.risk().name());
        contextVo.setTarget(target);
        JSONObject run = new JSONObject();
        run.put("runId", runVo.getId());
        run.put("runItemId", runItemVo.getId());
        run.put("scenarioId", runVo.getScenarioId());
        contextVo.setRun(run);
        JSONObject timing = new JSONObject();
        timing.put("startTime", targetExecution.startTime);
        timing.put("endTime", targetExecution.endTime);
        timing.put("durationMs", targetExecution.endTime.getTime() - targetExecution.startTime.getTime());
        contextVo.setTiming(timing);
        List<String> sensitiveValueList = new ArrayList<>();
        collectSensitiveValues(itemVo.getRequestParamObj(), sensitiveValueList);
        collectSensitiveValues(targetExecution.response, sensitiveValueList);
        if (targetExecution.selfCheckApi != null) {
            collectPathValues(itemVo.getRequestParamObj(), targetExecution.selfCheckApi.sensitiveParamPathList(), sensitiveValueList);
            collectPathValues(targetExecution.response, targetExecution.selfCheckApi.sensitiveResultPathList(), sensitiveValueList);
        }
        contextVo.setSensitiveValueList(sensitiveValueList);
        return contextVo;
    }

    /** 构造目标 API 与清理契约共享的执行上下文。 */
    private SelfCheckContextVo buildExecutionContext(SelfCheckRunVo runVo, SelfCheckRunItemVo runItemVo,
                                                     SelfCheckScenarioItemVo itemVo, Date startTime) {
        SelfCheckContextVo contextVo = new SelfCheckContextVo();
        contextVo.setRunId(runVo.getId());
        contextVo.setRunItemId(runItemVo.getId());
        contextVo.setScenarioId(runVo.getScenarioId());
        if (Objects.equals(runVo.getTriggerType(), "DEBUG")) {
            contextVo.setScenarioItemId(null);
        } else {
            contextVo.setScenarioItemId(itemVo.getId());
        }
        contextVo.setRequest(itemVo.getRequestParamObj());
        contextVo.setTargetStartTime(startTime);
        return contextVo;
    }

    /** 从检查项配置创建不可变审计快照。 */
    private SelfCheckRunItemVo createRunItem(SelfCheckRunVo runVo, SelfCheckScenarioItemVo itemVo) {
        SelfCheckRunItemVo resultVo = new SelfCheckRunItemVo();
        resultVo.setRunId(runVo.getId());
        if (!Objects.equals(runVo.getTriggerType(), "DEBUG")) {
            resultVo.setScenarioItemId(itemVo.getId());
        }
        resultVo.setItemName(itemVo.getName());
        resultVo.setSort(itemVo.getSort());
        ApiTarget apiTarget = resolveApiTarget(itemVo.getApiToken());
        resultVo.setApiToken(apiTarget.apiVo.getToken());
        resultVo.setApiName(apiTarget.apiVo.getName());
        resultVo.setApiRisk(apiTarget.annotation.risk().name());
        resultVo.setApiTimeout(itemVo.getApiTimeout());
        resultVo.setScriptTimeout(itemVo.getScriptTimeout());
        String[] sensitivePathList = apiTarget.annotation.sensitiveParamPathList();
        Snapshot requestSnapshot = buildSnapshot(itemVo.getRequestParamObj(), sensitivePathList);
        resultVo.setRequestSnapshot(requestSnapshot.value);
        resultVo.setVerifyScript(itemVo.getVerifyScript());
        resultVo.setStatus(SelfCheckRunItemStatus.PENDING.name());
        resultVo.setCleanupStatus(SelfCheckCleanupStatus.NOT_REQUIRED.name());
        JSONArray truncatedFieldList = new JSONArray();
        if (requestSnapshot.truncated) {
            truncatedFieldList.add("requestSnapshot");
        }
        resultVo.setTruncatedFieldList(truncatedFieldList.toJSONString());
        resultVo.setTraceId(UUID.randomUUID().toString());
        return resultVo;
    }

    /** 将脚本结果、控制台与诊断序列化到执行记录。 */
    private void copyScriptResult(SelfCheckRunItemVo resultVo, SelfCheckScriptResultVo scriptResult) {
        // 自检脚本只允许返回 Boolean，历史快照直接保存本次布尔判定值。
        resultVo.setScriptResult(JSON.toJSONString(scriptResult.getSuccess()));
        resultVo.setDiagnosticList(JSON.toJSONString(scriptResult.getDiagnosticList()));
        resultVo.setConsoleLogList(JSON.toJSONString(scriptResult.getConsoleList()));
        if (Boolean.TRUE.equals(scriptResult.getLogTruncated())) {
            addTruncatedField(resultVo, "consoleLogList");
        }
    }

    /** 将脚本服务结果转换为统一阶段结果。 */
    private JSONObject buildScriptPhase(String phase, SelfCheckScriptResultVo resultVo) {
        JSONObject result = new JSONObject();
        result.put("phase", phase);
        result.put("status", resultVo.getStatus());
        result.put("durationMs", resultVo.getDurationMs());
        result.put("message", resultVo.getMessage());
        result.put("diagnosticList", resultVo.getDiagnosticList());
        Date endTime = new Date();
        Date startTime = endTime;
        if (resultVo.getDurationMs() != null) {
            startTime = new Date(endTime.getTime() - resultVo.getDurationMs());
        }
        result.put("startTime", startTime);
        result.put("endTime", endTime);
        if (CollectionUtils.isNotEmpty(resultVo.getDiagnosticList())) {
            JSONObject diagnostic = (JSONObject) JSON.toJSON(resultVo.getDiagnosticList().get(0));
            result.putAll(diagnostic);
            result.put("phase", phase);
            result.put("status", resultVo.getStatus());
        }
        return result;
    }

    /** 创建成功阶段记录。 */
    private JSONObject buildSuccessPhase(String phase, Date startTime, Date endTime) {
        JSONObject result = new JSONObject();
        result.put("phase", phase);
        result.put("status", "SUCCEED");
        result.put("startTime", startTime);
        result.put("endTime", endTime);
        result.put("durationMs", endTime.getTime() - startTime.getTime());
        return result;
    }

    /** 创建隐藏 Java 堆栈、包含追踪标识的失败阶段记录。 */
    private JSONObject buildErrorPhase(String phase, Date startTime, Date endTime, String errorType,
                                       String message, String traceId) {
        JSONObject result = new JSONObject();
        result.put("phase", phase);
        result.put("status", "FAILED");
        result.put("startTime", startTime);
        result.put("endTime", endTime);
        result.put("durationMs", endTime.getTime() - startTime.getTime());
        result.put("errorType", errorType);
        result.put("message", limitText(message, 2000));
        result.put("traceId", traceId);
        return result;
    }

    /** 校验场景检查项并填充默认值。 */
    private void validateAndFillItemList(SelfCheckScenarioVo scenarioVo) {
        if (CollectionUtils.isEmpty(scenarioVo.getItemList())) {
            return;
        }
        for (SelfCheckScenarioItemVo itemVo : scenarioVo.getItemList()) {
            fillAndValidateItem(itemVo);
        }
    }

    /** 启用场景前使用当前脚本引擎重新校验全部启用项。 */
    private void validateActiveScripts(List<SelfCheckScenarioItemVo> itemList) {
        if (CollectionUtils.isEmpty(itemList)) {
            return;
        }
        for (SelfCheckScenarioItemVo itemVo : itemList) {
            if (!Objects.equals(itemVo.getIsActive(), 1)) {
                continue;
            }
            SelfCheckScriptResultVo validation = selfCheckScriptService.validate(itemVo.getVerifyScript());
            if (Boolean.TRUE.equals(validation.getValid())) {
                itemVo.setIsScriptValid(1);
            } else {
                itemVo.setIsScriptValid(0);
                itemVo.setScriptDiagnosticList(JSON.toJSONString(validation.getDiagnosticList()));
            }
        }
    }

    /** 将编排层未预期异常转换为可查询的检查项失败记录。 */
    private void recordUnexpectedItemFailure(SelfCheckRunVo runVo, SelfCheckScenarioItemVo itemVo, Exception ex) {
        logger.error("功能自检检查项编排失败，runId={}，itemId={}", runVo.getId(), itemVo.getId(), ex);
        SelfCheckRunItemVo failureVo = new SelfCheckRunItemVo();
        failureVo.setRunId(runVo.getId());
        if (!Objects.equals(runVo.getTriggerType(), "DEBUG")) {
            failureVo.setScenarioItemId(itemVo.getId());
        }
        failureVo.setItemName(itemVo.getName());
        failureVo.setSort(itemVo.getSort());
        ApiTarget apiTarget = resolveApiTarget(itemVo.getApiToken());
        failureVo.setApiToken(apiTarget.apiVo.getToken());
        failureVo.setApiName(apiTarget.apiVo.getName());
        failureVo.setApiRisk(apiTarget.annotation.risk().name());
        failureVo.setApiTimeout(itemVo.getApiTimeout());
        failureVo.setScriptTimeout(itemVo.getScriptTimeout());
        failureVo.setVerifyScript(itemVo.getVerifyScript());
        Snapshot requestSnapshot = buildSnapshot(itemVo.getRequestParamObj(), apiTarget.annotation.sensitiveParamPathList());
        failureVo.setRequestSnapshot(requestSnapshot.value);
        JSONArray truncatedFieldList = new JSONArray();
        if (requestSnapshot.truncated) {
            truncatedFieldList.add("requestSnapshot");
        }
        failureVo.setTruncatedFieldList(truncatedFieldList.toJSONString());
        failureVo.setStatus(SelfCheckRunItemStatus.TARGET_ERROR.name());
        failureVo.setCleanupStatus(SelfCheckCleanupStatus.NOT_REQUIRED.name());
        failureVo.setTraceId(UUID.randomUUID().toString());
        Date now = new Date();
        failureVo.setStartTime(now);
        failureVo.setEndTime(now);
        failureVo.setDurationMs(0L);
        JSONArray phaseList = new JSONArray();
        phaseList.add(buildErrorPhase("ORCHESTRATE", now, now, ex.getClass().getSimpleName(), ex.getMessage(), failureVo.getTraceId()));
        failureVo.setPhaseList(phaseList.toJSONString());
        selfCheckMapper.insertRunItem(failureVo);
    }

    /** 校验目标、超时和脚本配置并填充脚本诊断。 */
    private void fillAndValidateItem(SelfCheckScenarioItemVo itemVo) {
        if (itemVo.getIsActive() == null) {
            itemVo.setIsActive(1);
        }
        if (StringUtils.isBlank(itemVo.getName()) || StringUtils.isBlank(itemVo.getApiToken())
                || StringUtils.isBlank(itemVo.getVerifyScript())) {
            throw new ApiRuntimeException("检查项名称、API标识和验真脚本不能为空");
        }
        resolveApiTarget(itemVo.getApiToken());
        itemVo.setScriptTimeout(normalizeTimeout(itemVo.getScriptTimeout(), DEFAULT_SCRIPT_TIMEOUT, MAX_SCRIPT_TIMEOUT));
        itemVo.setApiTimeout(normalizeTimeout(itemVo.getApiTimeout(), DEFAULT_API_TIMEOUT, MAX_API_TIMEOUT));
        SelfCheckScriptResultVo validation = selfCheckScriptService.validate(itemVo.getVerifyScript());
        if (Boolean.TRUE.equals(validation.getValid())) {
            itemVo.setIsScriptValid(1);
        } else {
            itemVo.setIsScriptValid(0);
        }
        itemVo.setScriptDiagnosticList(JSON.toJSONString(validation.getDiagnosticList()));
    }

    /** 判断启用检查项中是否存在无效脚本。 */
    private boolean hasInvalidActiveItem(List<SelfCheckScenarioItemVo> itemList) {
        if (CollectionUtils.isEmpty(itemList)) {
            return true;
        }
        boolean hasActiveItem = false;
        for (SelfCheckScenarioItemVo itemVo : itemList) {
            if (Objects.equals(itemVo.getIsActive(), 1)) {
                hasActiveItem = true;
                if (!Objects.equals(itemVo.getIsScriptValid(), 1)) {
                    return true;
                }
            }
        }
        return !hasActiveItem;
    }

    /** 校验场景状态值。 */
    private void validateScenarioStatus(String status) {
        try {
            SelfCheckScenarioStatus.valueOf(status);
        } catch (Exception ex) {
            throw new ApiRuntimeException("不支持的自检场景状态：{0}", status);
        }
    }

    /** 将超时配置约束在平台允许范围内。 */
    private int normalizeTimeout(Integer timeout, int defaultTimeout, int maxTimeout) {
        if (timeout == null || timeout <= 0) {
            return defaultTimeout;
        }
        return Math.min(timeout, maxTimeout);
    }

    /** 获取准入注解声明的响应敏感路径。 */
    private String[] getSensitiveResultPathList(SelfCheckApi selfCheckApi) {
        if (selfCheckApi == null) {
            return new String[0];
        }
        return selfCheckApi.sensitiveResultPathList();
    }

    /** 判断任一候选文本是否包含查询关键字。 */
    private boolean matches(String keyword, String... values) {
        if (StringUtils.isBlank(keyword)) {
            return true;
        }
        String normalizedKeyword = keyword.toLowerCase(Locale.ROOT);
        for (String value : values) {
            if (value != null && value.toLowerCase(Locale.ROOT).contains(normalizedKeyword)) {
                return true;
            }
        }
        return false;
    }

    /** 将分页后的 API 元数据转换为前端分组选择器需要的结构。 */
    private JSONObject buildGroupedTargetResult(List<ApiTarget> targetList, int rowNum, int currentPage, int pageSize) {
        Map<String, JSONObject> groupMap = new LinkedHashMap<>();
        for (ApiTarget target : targetList) {
            ApiVo apiVo = target.apiVo;
            String moduleGroup = StringUtils.defaultIfBlank(apiVo.getModuleGroup(), apiVo.getModuleId());
            String moduleGroupName = StringUtils.defaultIfBlank(apiVo.getModuleGroupName(), moduleGroup);
            JSONObject group = groupMap.get(moduleGroup);
            if (group == null) {
                group = new JSONObject();
                group.put("moduleGroup", moduleGroup);
                group.put("moduleGroupName", moduleGroupName);
                group.put("apiList", new JSONArray());
                groupMap.put(moduleGroup, group);
            }
            JSONObject api = new JSONObject();
            api.put("token", apiVo.getToken());
            api.put("name", apiVo.getName());
            api.put("description", apiVo.getDescription());
            api.put("moduleId", apiVo.getModuleId());
            api.put("moduleGroup", moduleGroup);
            api.put("moduleGroupName", moduleGroupName);
            api.put("risk", target.annotation.risk().name());
            api.put("defaultScript", target.annotation.defaultScript());
            JSONObject help = target.component.help();
            JSONArray inputSchema = help.getJSONArray("input");
            if (inputSchema == null) {
                inputSchema = new JSONArray();
            }
            JSONArray outputSchema = help.getJSONArray("output");
            if (outputSchema == null) {
                outputSchema = new JSONArray();
            }
            api.put("inputSchema", inputSchema);
            api.put("outputSchema", outputSchema);
            group.getJSONArray("apiList").add(api);
        }
        JSONObject result = new JSONObject();
        result.put("tbodyList", new ArrayList<>(groupMap.values()));
        result.put("rowNum", rowNum);
        result.put("currentPage", currentPage);
        result.put("pageSize", pageSize);
        if (rowNum == 0) {
            result.put("pageCount", 0);
        } else {
            result.put("pageCount", (rowNum + pageSize - 1) / pageSize);
        }
        return result;
    }

    /** 规范分页字段，限制用户传入的页大小。 */
    private int normalizePageValue(Integer value, int defaultValue, int maxValue) {
        if (value == null || value <= 0) {
            return defaultValue;
        }
        return Math.min(value, maxValue);
    }

    /** 记录具体被截断的执行证据字段，避免使用含义模糊的总开关。 */
    private void addTruncatedField(SelfCheckRunItemVo runItemVo, String field) {
        JSONArray fieldList = new JSONArray();
        if (StringUtils.isNotBlank(runItemVo.getTruncatedFieldList())) {
            fieldList = JSON.parseArray(runItemVo.getTruncatedFieldList());
        }
        if (!fieldList.contains(field)) {
            fieldList.add(field);
        }
        runItemVo.setTruncatedFieldList(fieldList.toJSONString());
    }

    /** 统计场景中本次实际参与执行的检查项。 */
    private int countActiveItem(List<SelfCheckScenarioItemVo> itemList) {
        int count = 0;
        if (CollectionUtils.isNotEmpty(itemList)) {
            for (SelfCheckScenarioItemVo itemVo : itemList) {
                if (Objects.equals(itemVo.getIsActive(), 1)) {
                    count++;
                }
            }
        }
        return count;
    }

    /** 补充不持久化的 API 展示信息，API 下线时仍允许查看原 token。 */
    private void fillApiMetadata(SelfCheckScenarioItemVo itemVo) {
        try {
            ApiTarget target = resolveApiTarget(itemVo.getApiToken());
            itemVo.setApiName(target.apiVo.getName());
            itemVo.setApiRisk(target.annotation.risk().name());
        } catch (Exception ex) {
            itemVo.setApiName(itemVo.getApiToken());
        }
    }

    /** 对请求或响应深拷贝、脱敏并按字节上限截断。 */
    private Snapshot buildSnapshot(Object value, String[] sensitivePathList) {
        Object jsonValue = JSON.toJSON(value);
        redactSensitiveKeys(jsonValue);
        if (sensitivePathList != null) {
            for (String path : sensitivePathList) {
                redactPath(jsonValue, path);
            }
        }
        String text = JSON.toJSONString(jsonValue);
        if (text.getBytes(StandardCharsets.UTF_8).length <= MAX_SNAPSHOT_BYTES) {
            return new Snapshot(text, false);
        }
        String truncatedText = limitText(text, MAX_SNAPSHOT_BYTES / 2);
        JSONObject wrapper = new JSONObject();
        wrapper.put("truncated", true);
        wrapper.put("content", truncatedText);
        return new Snapshot(wrapper.toJSONString(), true);
    }

    /** 递归遮蔽常见敏感字段。 */
    private void redactSensitiveKeys(Object value) {
        if (value instanceof JSONObject) {
            JSONObject object = (JSONObject) value;
            for (String key : new ArrayList<>(object.keySet())) {
                boolean sensitive = DEFAULT_SENSITIVE_KEY_SET.stream().anyMatch(item -> item.equalsIgnoreCase(key));
                if (sensitive) {
                    object.put(key, "******");
                } else {
                    redactSensitiveKeys(object.get(key));
                }
            }
        } else if (value instanceof JSONArray) {
            for (Object item : (JSONArray) value) {
                redactSensitiveKeys(item);
            }
        }
    }

    /** 收集常见敏感字段原值供脚本日志二次脱敏。 */
    private void collectSensitiveValues(Object value, List<String> sensitiveValueList) {
        Object jsonValue = JSON.toJSON(value);
        if (jsonValue instanceof JSONObject) {
            JSONObject object = (JSONObject) jsonValue;
            for (String key : object.keySet()) {
                Object child = object.get(key);
                boolean sensitive = DEFAULT_SENSITIVE_KEY_SET.stream().anyMatch(item -> item.equalsIgnoreCase(key));
                if (sensitive && child != null) {
                    sensitiveValueList.add(String.valueOf(child));
                } else {
                    collectSensitiveValues(child, sensitiveValueList);
                }
            }
        } else if (jsonValue instanceof JSONArray) {
            for (Object item : (JSONArray) jsonValue) {
                collectSensitiveValues(item, sensitiveValueList);
            }
        }
    }

    /** 按准入注解路径收集敏感值。 */
    private void collectPathValues(Object value, String[] pathList, List<String> sensitiveValueList) {
        Object jsonValue = JSON.toJSON(value);
        if (!(jsonValue instanceof JSONObject) || pathList == null) {
            return;
        }
        for (String path : pathList) {
            Object pathValue = getPathValue((JSONObject) jsonValue, path);
            if (pathValue != null) {
                sensitiveValueList.add(String.valueOf(pathValue));
            }
        }
    }

    /** 读取点号分隔 JSON 路径的值。 */
    private Object getPathValue(JSONObject value, String path) {
        if (StringUtils.isBlank(path)) {
            return null;
        }
        String normalized = path;
        if (normalized.startsWith("$.")) {
            normalized = normalized.substring(2);
        }
        Object current = value;
        String[] parts = normalized.split("\\.");
        for (String part : parts) {
            if (!(current instanceof JSONObject)) {
                return null;
            }
            current = ((JSONObject) current).get(part);
        }
        return current;
    }

    /** 遮蔽点号分隔 JSON 路径对应的值。 */
    private void redactPath(Object value, String path) {
        if (!(value instanceof JSONObject) || StringUtils.isBlank(path)) {
            return;
        }
        String normalized = path;
        if (normalized.startsWith("$.")) {
            normalized = normalized.substring(2);
        }
        String[] parts = normalized.split("\\.");
        JSONObject current = (JSONObject) value;
        for (int i = 0; i < parts.length - 1; i++) {
            Object child = current.get(parts[i]);
            if (!(child instanceof JSONObject)) {
                return;
            }
            current = (JSONObject) child;
        }
        if (parts.length > 0 && current.containsKey(parts[parts.length - 1])) {
            current.put(parts[parts.length - 1], "******");
        }
    }

    /** 限制持久化错误文本长度。 */
    private String limitText(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength) + "...[已截断]";
    }

    private static class ApiTarget {
        private final ApiVo apiVo;
        private final IApiComponent component;
        private final SelfCheckApi annotation;

        private ApiTarget(ApiVo apiVo, IApiComponent component, SelfCheckApi annotation) {
            this.apiVo = apiVo;
            this.component = component;
            this.annotation = annotation;
        }
    }

    private static class TargetExecution {
        private final Object response;
        private final Date startTime;
        private final Date endTime;
        private final SelfCheckApi selfCheckApi;

        private TargetExecution(Object response, Date startTime, Date endTime, SelfCheckApi selfCheckApi) {
            this.response = response;
            this.startTime = startTime;
            this.endTime = endTime;
            this.selfCheckApi = selfCheckApi;
        }
    }

    private static class Snapshot {
        private final String value;
        private final boolean truncated;

        private Snapshot(String value, boolean truncated) {
            this.value = value;
            this.truncated = truncated;
        }
    }
}

/*
 * Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package neatlogic.module.tenant.api.file;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.constvalue.CacheControlType;
import neatlogic.framework.crossover.CrossoverServiceFactory;
import neatlogic.framework.crossover.IFileCrossoverService;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.publicapi.PublicBinaryStreamApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class DownloadFilePublicApi extends PublicBinaryStreamApiComponentBase {

    @Override
    public String getName() {
        return "附件下载接口(供第三方使用)";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @CacheControl(cacheControlType = CacheControlType.MAXAGE, maxAge = 30000)
    @Input({@Param(name = "id", type = ApiParamType.LONG, desc = "附件id", isRequired = true)})
    @Description(desc = "附件下载接口(供第三方使用)")
    @Override
    public Object myDoService(JSONObject paramObj, HttpServletRequest request, HttpServletResponse response) throws Exception {
        IFileCrossoverService fileCrossoverService = CrossoverServiceFactory.getApi(IFileCrossoverService.class);
        fileCrossoverService.downloadFile(paramObj, request, response);
        return null;
    }

    @Override
    public String getToken() {
        return "/public/file/download";
    }
}

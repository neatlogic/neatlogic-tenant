/*
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package neatlogic.module.tenant.api.documentonline;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.DOCUMENTONLINE_CONFIG_MODIFY;
import neatlogic.framework.documentonline.util.DocumentOnlineManager;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.springframework.stereotype.Service;

@Service
@AuthAction(action = DOCUMENTONLINE_CONFIG_MODIFY.class)
@OperationType(type = OperationTypeEnum.UPDATE)
public class LoadDocumentOnlineOutsideWarApi extends PrivateApiComponentBase {

    @Override
    public String getName() {
        return "nmtad.loaddocumentonlineoutsidewarapi.getname";
    }

    @Input({})
    @Output({})
    @Description(desc = "nmtad.loaddocumentonlineoutsidewarapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        return DocumentOnlineManager.LoadDocumentsOutsideWar();
    }

    @Override
    public String getToken() {
        return "documentonline/outsidewar/load";
    }
}

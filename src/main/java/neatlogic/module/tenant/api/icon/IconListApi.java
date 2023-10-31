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

package neatlogic.module.tenant.api.icon;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import java.util.*;

@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class IconListApi extends PrivateApiComponentBase {

    @Override
    public String getToken() {
        return "icon/list";
    }

    @Override
    public String getName() {
        return "图标列表接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "type", type = ApiParamType.JSONARRAY, desc = "图标分类。action:操作类,caution:警示类,chart:表单类,common:通用类,file:文件类型,logo:LOGO类,orientation:排版方向类,status:状态类,terminology:专用类", isRequired = true)})
    @Output({@Param(type = ApiParamType.STRING, desc = "图标列表")})
    @Description(desc = "图标列表接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Map<String, String[]> iconMap = new HashMap<>();
        iconMap.put("action",
                new String[]{"ts-setting", "ts-cog-s", "ts-cogs", "ts-edit", "ts-pencil-s", "ts-edittext", "ts-trash", "ts-trash-s", "ts-save", "ts-option-horizontal", "ts-option-vertical", "ts-search", "ts-search-minus", "ts-search-plus", "ts-minus", "ts-minus-o", "ts-plus", "ts-plus-o", "ts-remove", "ts-remove-o", "ts-pause", "ts-stop-o", "ts-stop", "ts-play-s", "ts-play", "ts-continue-o", "ts-continue", "ts-redo", "ts-refresh", "ts-rotate-right", "ts-rotate-left", "ts-reset-firststep",
                        "ts-import", "ts-download", "ts-export", "ts-upload", "ts-filedownload", "ts-exchange", "ts-toggle-o", "ts-toggle", "ts-sort", "ts-filter", "ts-lock", "ts-unlock", "ts-eye", "ts-eye-close", "ts-send", "ts-share-alt", "ts-star-s", "ts-star", "ts-unstar", "ts-forward", "ts-backward", "ts-expand", "ts-compress", "ts-normalsize", "ts-fullscreen", "ts-fullpage", "ts-arrows", "ts-zoom", "ts-intersect", "ts-subtract", "ts-union", "ts-acknowledge", "ts-handlersort", "ts-resolve",
                        "ts-remove-s", "ts-link", "ts-unlink", "ts-adduser", "ts-adduser-s", "ts-heart-s", "ts-heart"});
        iconMap.put("caution", new String[]{"ts-notice", "ts-alarmsetting", "ts-alert-off", "ts-alert", "ts-alert-o", "ts-alert-s", "ts-alarmservice", "ts-bell", "ts-bell-off", "ts-bell-s", "ts-info-s", "ts-info", "ts-timer", "ts-interval", "ts-warning", "ts-malfunctionlist", "ts-m-problem", "ts-problemlist", "ts-m-urgency"});
        iconMap.put("chart", new String[]{"ts-areachart", "ts-barchart", "ts-linechart", "ts-number", "ts-numberchart", "ts-piechart", "ts-plotchart", "ts-tablechart"});
        iconMap.put("common",
                new String[]{"ts-palette", "ts-chain", "ts-chat", "ts-m-request", "ts-hand", "ts-manually", "ts-script", "ts-vip", "ts-dev-security", "ts-m-dashboard-job", "ts-m-schedulejob", "ts-recycle", "ts-bars", "ts-component", "ts-icon", "ts-layout", "ts-tablelist", "ts-m-view", "ts-typelist", "ts-templatesetting", "ts-viewlist", "ts-m-system", "ts-m-template", "ts-file", "ts-dynamic-text", "ts-m-works", "ts-page", "ts-pages", "ts-dev-iplist", "ts-m-playbook", "ts-catalogue",
                        "ts-m-project", "ts-text", "ts-textmodule", "ts-flowsetting", "ts-listsetting", "ts-parameter", "ts-tag", "ts-tags", "ts-changelist", "ts-solution", "ts-email", "ts-json", "ts-risklist", "ts-cone", "ts-defect-code", "ts-viewmodule", "ts-agent", "ts-batch-ops", "ts-branch", "ts-exagent", "ts-module", "ts-nodata", "ts-pluginlist", "ts-list", "ts-formlist", "ts-relation", "ts-permission", "ts-radar", "ts-shunt", "ts-spinner", "ts-m-batchdeploy", "ts-m-stack",
                        "ts-m-signature", "ts-dev-assets", "ts-bad", "ts-excellent", "ts-good", "ts-ordinary", "ts-hurry"});
        iconMap.put("file", new String[]{"ts-mm-css", "ts-mm-bat", "ts-mm-cls", "ts-mm-cnf", "ts-mm-bmp", "ts-mm-cmd", "ts-mm-gzip", "ts-mm-doc", "ts-mm-exe", "ts-mm-gif", "ts-mm-mov", "ts-mm-html", "ts-mm-mp4", "ts-mm-java", "ts-mm-jpg", "ts-mm-png", "ts-mm-pdf", "ts-mm-py", "ts-mm-pptx", "ts-mm-ppt", "ts-mm-js", "ts-mm-rar", "ts-mm-rtf", "ts-mm-docx", "ts-mm-rpm", "ts-mm-unknown", "ts-mm-txt", "ts-mm-jpeg", "ts-mm-sql", "ts-mm-sh", "ts-mm-zip", "ts-mm-tar", "ts-mm-vbs", "ts-mm-xls",
                "ts-mm-xlsm", "ts-mm-xml", "ts-mm-xlsx", "ts-mm-svg", "ts-folder", "ts-folder-open", "ts-folder-add"});
        iconMap.put("logo", new String[]{"ts-centos", "ts-cisco", "ts-dev-nutanix", "ts-openstack", "ts-dev-apache", "ts-dev-aix", "ts-dev-alicloud", "ts-dev-apple", "ts-dev-aws", "ts-dev-dell", "ts-dev-docker", "ts-dev-db2", "ts-dev-freebsd", "ts-dev-firewall", "ts-dev-hadoop", "ts-dev-ibm", "ts-dev-huawei", "ts-dev-java", "ts-dev-hp", "ts-dev-jboss", "ts-dev-juniper", "ts-dev-lighttpd", "ts-dev-kafka", "ts-dev-inspur", "ts-dev-mysql", "ts-dev-linux", "ts-dev-mesos", "ts-dev-nginx",
                "ts-dev-redis", "ts-dev-postgresql", "ts-dev-redhat", "ts-dev-spark", "ts-dev-tomcat", "ts-dev-vsphere", "ts-dev-windows", "ts-dev-resin", "ts-dev-zookeeper", "ts-tencentcloud"});
        iconMap.put("orientation", new String[]{"ts-angle-double-down", "ts-angle-double-up", "ts-angle-double-left", "ts-angle-double-right", "ts-angle-right", "ts-angle-left", "ts-angle-down", "ts-angle-up", "ts-caret-left", "ts-caret-down", "ts-caret-up", "ts-caret-right", "ts-long-arrow-down", "ts-long-arrow-up", "ts-long-arrow-left", "ts-long-arrow-right", "ts-align-horizontal", "ts-align-vertical", "ts-dedent", "ts-indent", "ts-object-group", "ts-block", "ts-full", "ts-one-half",
                "ts-one-quarter", "ts-one-third", "ts-three-quarter", "ts-two-third", "ts-innercurve", "ts-singlepoly", "ts-straightline", "ts-doublepoly", "ts-menuhide", "ts-menushow", "ts-vertical-middle", "ts-vertical-bottom", "ts-horizontal-center", "ts-vertical-top", "ts-horizontal-justify", "ts-horizontal-right", "ts-horizontal-left"});
        iconMap.put("status", new String[]{"ts-complete", "ts-check", "ts-finish", "ts-forbid", "ts-pending", "ts-dev-bin", "ts-circle", "ts-round", "ts-minus-square-s", "ts-check-square-o", "ts-minus-square", "ts-check-square", "ts-round-s", "ts-square-o", "ts-circle-fill"});
        iconMap.put("terminology",
                new String[]{"ts-calendar", "ts-console", "ts-debug", "ts-dictionary", "ts-envelope", "ts-location", "ts-phone", "ts-picture", "ts-version", "ts-pulse", "ts-report", "ts-sitemap", "ts-workflow", "ts-user", "ts-user-s", "ts-team", "ts-team-s", "ts-dev-business", "ts-cloud", "ts-dev-userinfo", "ts-m-auth", "ts-m-browser", "ts-m-certificate", "ts-m-callcenter", "ts-m-dashboard", "ts-m-installation", "ts-m-cmdb", "ts-m-ip", "ts-m-home", "ts-m-metrics", "ts-m-myjob", "ts-m-monitor",
                        "ts-m-plugin", "ts-m-octopus", "ts-m-apm", "ts-cite", "ts-integration", "ts-eoa", "ts-internet", "ts-code", "ts-sop", "ts-relationship", "ts-cubes", "ts-cube", "ts-database", "ts-network-adapter", "ts-dev-lun", "ts-dev-virtualmachine", "ts-dev-appcube", "ts-cluster", "ts-dev-container", "ts-dev-drawer", "ts-dev-ha", "ts-dev-f5", "ts-dev-hardware", "ts-dev-loadbalancing", "ts-dev-logicaldisk", "ts-dev-middleware", "ts-dev-network-devices", "ts-dev-nic", "ts-dev-netarea",
                        "ts-dev-port", "ts-dev-os", "ts-dev-router", "ts-dev-storm", "ts-dev-server", "ts-m-server", "ts-dev-switch", "ts-dev-storage", "ts-dev-2cube", "ts-environ-common", "ts-environ-dev", "ts-environ-sit", "ts-environ-uat", "ts-environ-prd", "ts-dev-vs", "ts-dev-app", "ts-dev-cfg", "ts-dev-controllerinfo", "ts-dev-datacenter", "ts-dev-diskinfo", "ts-dev-diskgroup", "ts-dev-domainname-intranet", "ts-dev-drawer-expansion", "ts-dev-domainname", "ts-dev-drawer-room",
                        "ts-dev-exchange", "ts-dev-ftp", "ts-dev-h3c", "ts-dev-hba", "ts-dev-iis", "ts-dev-hyperflex", "ts-dev-line", "ts-dev-ldap", "ts-dev-rack", "ts-dev-oracle", "ts-dev-pool", "ts-dev-raid", "ts-dev-saas", "ts-dev-root", "ts-dev-subsystem", "ts-dev-vpn", "ts-dev-weblogic", "ts-m-ci", "ts-m-deployment", "ts-m-misc", "ts-dev-zone"});
        //除cmdb以外的图标
        iconMap.put("nocmdb", new String[]{"tsfont-neatlogic", "tsfont-action",
                "tsfont-addchange",
                "tsfont-addimg-s",
                "tsfont-addimg",
                "tsfont-addtag",
                "tsfont-addteam",
                "tsfont-adduser",
                "tsfont-adjust",
                "tsfont-agent",
                "tsfont-anquangeliwangzha",
                "tsfont-anquanguanlipingtai",
                "tsfont-anquanwangguan",
                "tsfont-anzhuangbao",
                "tsfont-anzhuangshuxing",
                "tsfont-arrow-corner-left",
                "tsfont-arrow-corner-right",
                "tsfont-arrow-down",
                "tsfont-arrow-left",
                "tsfont-arrow-right",
                "tsfont-arrow-up",
                "tsfont-attachment",
                "tsfont-attribute",
                "tsfont-auto",
                "tsfont-bad",
                "tsfont-badge",
                "tsfont-baobiao",
                "tsfont-baosong",
                "tsfont-bar",
                "tsfont-barlist",
                "tsfont-batch-download",
                "tsfont-batch-success",
                "tsfont-batch-upload",
                "tsfont-bell-o",
                "tsfont-bell-off",
                "tsfont-bell-on",
                "tsfont-bell-z",
                "tsfont-bell",
                "tsfont-biaoti",
                "tsfont-bind",
                "tsfont-block",
                "tsfont-blocklist",
                "tsfont-blocks",
                "tsfont-bofenfuyongshebei",
                "tsfont-bold",
                "tsfont-book",
                "tsfont-border-all",
                "tsfont-border-clear",
                "tsfont-broom",
                "tsfont-bushushuxing",
                "tsfont-calendar",
                "tsfont-callcenter",
                "tsfont-canvas",
                "tsfont-celve",
                "tsfont-center",
                "tsfont-change",
                "tsfont-changing",
                "tsfont-chart-area",
                "tsfont-chart-bar",
                "tsfont-chart-bullet",
                "tsfont-chart-funnel",
                "tsfont-chart-gauge",
                "tsfont-chart-groupedcolumn",
                "tsfont-chart-heatmap",
                "tsfont-chart-line",
                "tsfont-chart-lines",
                "tsfont-chart-liquid",
                "tsfont-chart-number",
                "tsfont-chart-pie",
                "tsfont-chart-polyline",
                "tsfont-chart-progress",
                "tsfont-chart-radar",
                "tsfont-chart-rose",
                "tsfont-chart-sankey",
                "tsfont-chart-scatter",
                "tsfont-chart-scatterbubble",
                "tsfont-chart-stackedcolumn",
                "tsfont-chart-table",
                "tsfont-chart-wordcloud",
                "tsfont-chart-wordcloud",
                "tsfont-check-o",
                "tsfont-check-s",
                "tsfont-check-some",
                "tsfont-check-square",
                "tsfont-check",
                "tsfont-checklist",
                "tsfont-close-o",
                "tsfont-close-s",
                "tsfont-close",
                "tsfont-cloud",
                "tsfont-code",
                "tsfont-collapse",
                "tsfont-common",
                "tsfont-compare",
                "tsfont-config",
                "tsfont-connector",
                "tsfont-cooperate-o",
                "tsfont-cooperate-s",
                "tsfont-copy",
                "tsfont-danger-o",
                "tsfont-danger-s",
                "tsfont-day",
                "tsfont-dot",
                "tsfont-down",
                "tsfont-download",
                "tsfont-drafts",
                "tsfont-drag",
                "tsfont-drop-down-s",
                "tsfont-drop-down",
                "tsfont-drop-left",
                "tsfont-drop-right",
                "tsfont-drop-up",
                "tsfont-duixiangcunchu",
                "tsfont-edit-s",
                "tsfont-edit",
                "tsfont-excellent",
                "tsfont-expand",
                "tsfont-export-s",
                "tsfont-export",
                "tsfont-eye-off",
                "tsfont-eye",
                "tsfont-fangbingduwangguan",
                "tsfont-file-multi",
                "tsfont-file-single",
                "tsfont-filter-o",
                "tsfont-filter",
                "tsfont-first",
                "tsfont-flow-children",
                "tsfont-flow-siblings",
                "tsfont-flow",
                "tsfont-fold",
                "tsfont-folder-o",
                "tsfont-folder-s",
                "tsfont-folder-share",
                "tsfont-font-size",
                "tsfont-forbid",
                "tsfont-formcascadelist",
                "tsfont-formdynamiclist",
                "tsfont-forminput",
                "tsfont-formlink",
                "tsfont-formlist",
                "tsfont-formselect",
                "tsfont-formselectcascader",
                "tsfont-formstaticlist",
                "tsfont-formtextarea",
                "tsfont-formtime",
                "tsfont-fullscreen",
                "tsfont-fuwuqi",
                "tsfont-good",
                "tsfont-hand",
                "tsfont-heart-s",
                "tsfont-history",
                "tsfont-home",
                "tsfont-horizontal-center",
                "tsfont-horizontal-justify",
                "tsfont-horizontal-left",
                "tsfont-horizontal-right",
                "tsfont-image-s",
                "tsfont-image",
                "tsfont-img-center",
                "tsfont-img-left",
                "tsfont-img-right",
                "tsfont-import-s",
                "tsfont-import",
                "tsfont-indent",
                "tsfont-info-o",
                "tsfont-info-s",
                "tsfont-info",
                "tsfont-inlink",
                "tsfont-integration",
                "tsfont-ipliebiao",
                "tsfont-italic",
                "tsfont-ITfuwu",
                "tsfont-jiamishebei",
                "tsfont-jicheng",
                "tsfont-jifang",
                "tsfont-json",
                "tsfont-kangjujuefuwushebei",
                "tsfont-label-s",
                "tsfont-label",
                "tsfont-last",
                "tsfont-layer",
                "tsfont-left",
                "tsfont-lightning",
                "tsfont-list",
                "tsfont-liuliangfenxishebei",
                "tsfont-location",
                "tsfont-lock-s",
                "tsfont-lock",
                "tsfont-loudongsaomiaoshebei",
                "tsfont-mail-o",
                "tsfont-mail-read-o",
                "tsfont-mail-s",
                "tsfont-mail-unread-o",
                "tsfont-manager",
                "tsfont-mark-all",
                "tsfont-message-o",
                "tsfont-message",
                "tsfont-minus-o",
                "tsfont-minus-s",
                "tsfont-minus-square",
                "tsfont-minus",
                "tsfont-module",
                "tsfont-modules",
                "tsfont-monitor",
                "tsfont-night",
                "tsfont-node",
                "tsfont-node1",
                "tsfont-non-auth",
                "tsfont-off-fullscreen",
                "tsfont-option-horizontal",
                "tsfont-option-vertical",
                "tsfont-orderlist",
                "tsfont-ordinary",
                "tsfont-outdent",
                "tsfont-outline-s",
                "tsfont-palette",
                "tsfont-pause-o",
                "tsfont-pause-s",
                "tsfont-pause",
                "tsfont-peizhiguanli",
                "tsfont-peizhihechashebei",
                "tsfont-permission",
                "tsfont-phone",
                "tsfont-pie",
                "tsfont-pingbijigui",
                "tsfont-play-o",
                "tsfont-play-s",
                "tsfont-play",
                "tsfont-plugin",
                "tsfont-plus-o",
                "tsfont-plus-s",
                "tsfont-plus-square",
                "tsfont-plus",
                "tsfont-pod",
                "tsfont-proxy",
                "tsfont-putongjigui",
                "tsfont-question-o",
                "tsfont-question-s",
                "tsfont-question",
                "tsfont-refresh",
                "tsfont-reminder",
                "tsfont-reply-all",
                "tsfont-reply",
                "tsfont-restart",
                "tsfont-restful",
                "tsfont-revover",
                "tsfont-right",
                "tsfont-riqishijian",
                "tsfont-rizhishoujiyufenxixitong",
                "tsfont-role-s",
                "tsfont-role",
                "tsfont-rotate-right",
                "tsfont-run",
                "tsfont-ruqinjianceyufangyushebei",
                "tsfont-save",
                "tsfont-scene",
                "tsfont-script",
                "tsfont-search-minus",
                "tsfont-search-plus",
                "tsfont-search",
                "tsfont-security",
                "tsfont-send",
                "tsfont-setting",
                "tsfont-shangwanghangweiguanlishebei",
                "tsfont-share",
                "tsfont-shitu",
                "tsfont-shujukutuomin",
                "tsfont-shunt",
                "tsfont-shuziqianmingshebei",
                "tsfont-sla",
                "tsfont-sop",
                "tsfont-sort-all",
                "tsfont-sort-left",
                "tsfont-sort-right",
                "tsfont-spacing",
                "tsfont-square",
                "tsfont-stage",
                "tsfont-star-border",
                "tsfont-star-half",
                "tsfont-star",
                "tsfont-stars",
                "tsfont-tabforword",
                "tsfont-takeover",
                "tsfont-task-cancel",
                "tsfont-task-ok",
                "tsfont-task",
                "tsfont-taskperson-s",
                "tsfont-taskperson",
                "tsfont-team-s",
                "tsfont-team",
                "tsfont-techsure",
                "tsfont-test",
                "tsfont-text-delete",
                "tsfont-textarea",
                "tsfont-theme",
                "tsfont-tickets",
                "tsfont-time",
                "tsfont-timer",
                "tsfont-title",
                "tsfont-tool",
                "tsfont-topo",
                "tsfont-transmit",
                "tsfont-trash-o",
                "tsfont-trash-s",
                "tsfont-tree",
                "tsfont-type",
                "tsfont-unbind",
                "tsfont-undo",
                "tsfont-unfold",
                "tsfont-unlock",
                "tsfont-unsend",
                "tsfont-up",
                "tsfont-upload",
                "tsfont-urgency",
                "tsfont-user-s",
                "tsfont-user-setting",
                "tsfont-user",
                "tsfont-verify",
                "tsfont-vertical-bottom",
                "tsfont-vertical-middle",
                "tsfont-vertical-top",
                "tsfont-vote-o",
                "tsfont-wangluoshujufangxielouxitong",
                "tsfont-warning-o",
                "tsfont-warning-s",
                "tsfont-Webyingyongfanghuxitong",
                "tsfont-wechat",
                "tsfont-xitongpeizhi",
                "tsfont-xunijizhuanyongshebeivpn",
                "tsfont-yitihuajigui",
                "tsfont-youjiananquanguolvxitong",
                "tsfont-yunweishenjishebei",
                "tsfont-zhishiku",
                "tsfont-zhunrukongzhishebei",
                "tsfont-zichanshuruzujian",
                "tsfont-zidonghua",
                "tsfont-zirenwu",
                "tsfont-font-bg",
                "tsfont-font-color",
                "tsfont-pin-angle-o",
                "tsfont-pin-angle-s",
                "tsfont-pin-o",
                "tsfont-pin-s",
                "tsfont-private-data-source",
                "tsfont-trigger",
                "tsfont-webhook",
                "tsfont-width",
                "tsfont-neatlogic",
                "tsfont-gitlab",
                "tsfont-svn",
                "tsfont-merge",
                "tsfont-table-column",
                "tsfont-table-row",
                "tsfont-check-square-o",
                "tsfont-double-arrow-left",
                "tsfont-double-arrow-right",
                "tsfont-double-arrow-up",
                "tsfont-heart-o",
                "tsfont-folder-open",
                "tsfont-solid-circle",
                "tsfont-alert",
                "tsfont-batch-ops",
                "tsfont-listsetting",
                "tsfont-m-apm",
                "tsfont-m-batchdeploy",
                "tsfont-m-dashboard-job",
                "tsfont-m-dashboard",
                "tsfont-m-deployment",
                "tsfont-m-ip",
                "tsfont-m-octopus",
                "tsfont-m-request",
                "tsfont-m-signature",
                "tsfont-m-stack",
                "tsfont-spinner",
                "tsfont-tags",
                "tsfont-border-bottom",
                "tsfont-border-horizontal",
                "tsfont-border-left",
                "tsfont-border-right",
                "tsfont-border-top",
                "tsfont-border-vertical"
        });
        iconMap.put("cmdb", new String[]{
                "tsfont-ci-o",
                "tsfont-ci",
                "tsfont-apple",
                "tsfont-certificate",
                "tsfont-centos",
                "tsfont-dell",
                "tsfont-apache",
                "tsfont-freebsd",
                "tsfont-debug",
                "tsfont-docker",
                "tsfont-firewall",
                "tsfont-secdev",
                "tsfont-accessendpoint",
                "tsfont-hp",
                "tsfont-jboss",
                "tsfont-php",
                "tsfont-python",
                "tsfont-lighttpd",
                "tsfont-kafka",
                "tsfont-hadoop",
                "tsfont-mesos",
                "tsfont-linux",
                "tsfont-lun",
                "tsfont-location-o",
                "tsfont-nginx",
                "tsfont-netarea",
                "tsfont-openstack",
                "tsfont-report",
                "tsfont-storm",
                "tsfont-redhat",
                "tsfont-resin",
                "tsfont-redis",
                "tsfont-jetty",
                "tsfont-iis",
                "tsfont-memcached",
                "tsfont-activemq",
                "tsfont-rabbitmq",
                "tsfont-spark",
                "tsfont-ibm",
                "tsfont-websphere",
                "tsfont-weblogic",
                "tsfont-keepalive",
                "tsfont-tuxedo",
                "tsfont-tencentcloud",
                "tsfont-virtualmachine",
                "tsfont-tomcat",
                "tsfont-version",
                "tsfont-wenjian",
                "tsfont-tianjiawenjian",
                "tsfont-inspur",
                "tsfont-huawei",
                "tsfont-aix",
                "tsfont-alicloud",
                "tsfont-aws",
                "tsfont-cisco",
                "tsfont-java",
                "tsfont-db",
                "tsfont-desktop",
                "tsfont-juniper",
                "tsfont-nutanix",
                "tsfont-pulse",
                "tsfont-dns",
                "tsfont-vm",
                "tsfont-vmware-cluster",
                "tsfont-windows",
                "tsfont-cluster-mode",
                "tsfont-cluster-software",
                "tsfont-ip-list",
                "tsfont-db-cluster",
                "tsfont-oracle",
                "tsfont-oracle-rac",
                "tsfont-mssqlserver",
                "tsfont-sybase",
                "tsfont-mongodb",
                "tsfont-postgresql",
                "tsfont-informix",
                "tsfont-informix-db",
                "tsfont-mysql",
                "tsfont-mysql-db",
                "tsfont-db2",
                "tsfont-dbins",
                "tsfont-elasticsearch",
                "tsfont-net-area",
                "tsfont-userinfo",
                "tsfont-os-cluster",
                "tsfont-danger-level",
                "tsfont-app",
                "tsfont-cluster",
                "tsfont-internet",
                "tsfont-db-ins",
                "tsfont-datacenter",
                "tsfont-component",
                "tsfont-ip-object",
                "tsfont-ins",
                "tsfont-os",
                "tsfont-vcenter",
                "tsfont-vmware-cluster",
                "tsfont-storages",
                "tsfont-softwareservice",
                "tsfont-group",
                "tsfont-adapter",
                "tsfont-dictionary",
                "tsfont-devices",
                "tsfont-auth",
                "tsfont-browser",
                "tsfont-eoa",
                "tsfont-loadblance-vs",
                "tsfont-hardware",
                "tsfont-port",
                "tsfont-f5",
                "tsfont-a10",
                "tsfont-k8s",
                "tsfont-k8s_pod",
                "tsfont-fcswitch",
                "tsfont-fcdev",
                "tsfont-console",
                "tsfont-zookeeper",
                "tsfont-application",
                "tsfont-storagerpa",
                "tsfont-storage",
                "tsfont-virtualstorage",
                "tsfont-host",
                "tsfont-switch",
                "tsfont-router",
                "tsfont-netdev",
                "tsfont-novmtool",
                "tsfont-application_module",
                "tsfont-empty",
                "tsfont-k8s_node",
                "tsfont-unknown"
        });


        List<String> iconList = new ArrayList<>();
        JSONArray typeList = jsonObj.getJSONArray("type");
        if (!typeList.isEmpty()) {
            for (int i = 0; i < typeList.size(); i++) {
                if (iconMap.containsKey(typeList.getString(i))) {
                    iconList.addAll(Arrays.asList(iconMap.get(typeList.getString(i))));
                }
            }
        }
        return iconList;
    }
}

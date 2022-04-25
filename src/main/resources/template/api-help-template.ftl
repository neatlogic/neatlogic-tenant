<!DOCTYPE html>
<html>
<head lang="en">
    <meta charset="UTF-8"/>
    <style>
        .innerTable {
            width: 100%;
            text-align: center;
            border-collapse: collapse;
        }

        .innerTable th, .innerTable td {
            border: 1px solid #000;
            font-weight: normal;
        }

        .lineTd {
            text-align: right;
            word-break: break-all;
            word-wrap: break-word;
        }
    </style>
</head>

<body>
<p>${DATA.description}</p>
<#--正文-->

<#--输入参数-->
<#if DATA.input?? && DATA.input?size gt 0>
    <p>输入参数</p>
    <table class="innerTable">
        <thead>
        <tr>
            <th>名称</th>
            <th>类型</th>
            <th>是否必填</th>
            <th>说明</th>
        </tr>
        </thead>
        <tbody>
        <#list DATA.input as value>
            <tr>
                <td>${value["name"]}</td>
                <td>${value["type"]}</td>
                <td>${value["isRequired"]}</td>
                <td>${value["description"]}</td>
            </tr>
        </#list>
        </tbody>
    </table>
</#if>

<#if DATA.output?? && DATA.output?size gt 0>
    <p>输出参数</p>
    <table class="innerTable">
        <thead>
        <tr>
            <th>名称</th>
            <th>类型</th>
            <th>是否必填</th>
            <th>说明</th>
        </tr>
        </thead>
        <tbody>
        <#list DATA.output as value>
            <tr>
                <td>${value["name"]}</td>
                <td>${value["type"]}</td>
                <td>${value["isRequired"]}</td>
                <td>${value["description"]}</td>
            </tr>
        </#list>
        </tbody>
    </table>
</#if>


</body>
</html>

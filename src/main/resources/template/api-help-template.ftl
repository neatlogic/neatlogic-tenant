<!DOCTYPE html>
<html>
<head lang="en">
    <meta charset="UTF-8"/>
    <style>
        body {
            font-family: "SimSun";
        }
        p{
            font-weight: bold;
        }
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

<body style="font-family: SimSun;">
<#if DATA?? && DATA?size gt 0>
    <#list DATA as api>
        <p>接口token</p>
        <span>${api.token}</span>
        <p>接口名称</p>
        <span>${api.name}</span>
        <p>描述</p>
        <span>${api.description}</span>
    <#--输入参数-->
        <#if api.input?? && api.input?size gt 0>
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
                <#list api.input as value>
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
    <#--输出参数-->
        <#if api.output?? && api.output?size gt 0>
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
                <#list api.output as value>
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
        <p></p>
        <p></p>
        <p>----------------------------------------------------------------------------------------------------------------------------</p>
        <p></p>
        <p></p>
    </#list>
</#if>

</body>
</html>

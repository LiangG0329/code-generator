<head>
    <title>代码生成平台官网</title>
</head>
<body>
<h1>欢迎来到代码生成平台官网</h1>
<ul>
    <#-- 循环渲染导航条 -->
    <#list menuItems as item>
        <li><a href="${item.url}">${item.label}</a></li>
    </#list>
</ul>
<#-- 底部版权信息（注释部分，不会被输出）-->
<footer>
    ${currentYear} 代码生成平台. All rights reserved.
</footer>
</body>
</html>
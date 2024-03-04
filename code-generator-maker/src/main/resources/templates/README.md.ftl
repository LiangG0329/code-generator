# ${name}

> ${description}
>
> 作者：${author}
>
> 基于 [LiangGuo](https://github.com/LiangG0329) 的 [代码生成器项目](https://github.com/LiangG0329/code-generator) 制作，感谢您的使用！

可以通过命令行交互式输入的方式动态生成想要的项目代码

## 使用说明

进入生成的*项目根目录*,执行项目根目录下的生成的脚本文件 *generator*：

```
generator <命令> <选项参数>
```

示例命令：

```
generator generate<#if modelConfig?has_content && modelConfig.models?has_content><#list modelConfig.models as modelInfo><#if modelInfo.fieldName??> --${modelInfo.fieldName}<#if modelInfo.abbr??>|-${modelInfo.abbr}</#if></#if></#list></#if>  # 代码生成
generator config  # 输出动态参数的信息
generator list  # 输出子文件列表
```

## 参数说明
<#if modelConfig?has_content && modelConfig.models?has_content>
<#list modelConfig.models as modelInfo>
<#-- 命令选项 -->
<#if modelInfo.fieldName??>
${modelInfo?index + 1}) ${modelInfo.fieldName}

类型：${modelInfo.type}

描述：${modelInfo.description}

默认值：${modelInfo.defaultValue?c}

<#if modelInfo.abbr??>子命令缩写： -${modelInfo.abbr}</#if>
</#if>
<#-- 分组命令 -->
<#if modelInfo.groupKey??>

类型：${modelInfo.type}

描述：${modelInfo.description}

分组命令选项：<#list modelInfo.models as groupModel><#if groupModel.fieldName??> --${groupModel.fieldName}<#if groupModel.abbr??>|-${groupModel.abbr}</#if></#if></#list>

</#if>

</#list>
</#if>
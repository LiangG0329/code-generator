package ${basePackage}.model;

import lombok.Data;

<#-- 宏定义: 定义models属性 -->
<#macro generateModel indent modelInfo>

<#if modelInfo.description??>
${indent}/**
${indent} * ${modelInfo.description}
${indent} */
</#if>
${indent}private ${modelInfo.type} ${modelInfo.fieldName}<#if modelInfo.defaultValue??> = ${modelInfo.defaultValue?c}</#if>;
</#macro>

/**
 * 数据模型
 * @author ${author}
 * @create ${createTime}
 */
@Data
public class DataModel {
<#if modelConfig?has_content && modelConfig.models?has_content>
<#list modelConfig.models as modelInfo>
    <#-- 分组 -->
    <#if modelInfo.groupKey??>

    /**
     * group Key=${modelInfo.groupKey} Name=${modelInfo.groupName}
     */
    private ${modelInfo.type} ${modelInfo.groupKey} = new ${modelInfo.type}();

    /**
     * ${modelInfo.description}
     */
    @Data
    public static class ${modelInfo.type} {
        <#-- 遍历分组内的model -->
        <#list modelInfo.models as submodelInfo>
            <@generateModel indent="        " modelInfo=submodelInfo />
        </#list>
    }
    <#else>
    <#-- 无分组 -->
    <@generateModel indent="    " modelInfo=modelInfo />
    </#if>
</#list>
</#if>
}
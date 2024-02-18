package ${basePackage}.model;

import lombok.Data;

/**
 * 数据模型
 * @author Liang
 * @create 2024/2/13
 */
@Data
public class DataModel {
<#list modelConfig.models as modelInfo>

    <#if modelInfo.description??>
    /**
     * ${modelInfo.description}
     */
    private ${modelInfo.type} ${modelInfo.fieldName} <#if modelInfo.defaultValue??> = ${modelInfo.defaultValue?c}</#if>;
    </#if>

</#list>
}
package ${basePackage}.cli.command;

import cn.hutool.core.bean.BeanUtil;
import ${basePackage}.generator.FileGenerator;
import ${basePackage}.model.DataModel;
import lombok.Data;
import picocli.CommandLine;

import java.util.concurrent.Callable;

<#-- 宏定义: 生成选项 -->
<#macro generateOption indent modelInfo>

${indent}/** 命令选项: ${modelInfo.description} */
${indent}@CommandLine.Option(names = {<#if modelInfo.abbr??>"-${modelInfo.abbr}", </#if>"--${modelInfo.fieldName}"}, arity = "0..1", <#if modelInfo.description??>description = "${modelInfo.description}",</#if> interactive = true, echo = true)
${indent}private ${modelInfo.type} ${modelInfo.fieldName}<#if modelInfo.defaultValue??> = ${modelInfo.defaultValue?c}</#if>;
</#macro>

<#-- 宏定义: 生成命令调用 -->
<#macro generateCommand indent modelInfo>

${indent}System.out.println("输入${modelInfo.groupName}配置：");
${indent}CommandLine commandLine = new CommandLine(${modelInfo.type}Command.class);
${indent}commandLine.execute(${modelInfo.allArgsStr});
</#macro>

/**
 * generate 生成命令类和命令选项
 * @author ${author}
 * @create ${createTime}
 */
@Data
@CommandLine.Command(name = "generate", description = "代码生成", mixinStandardHelpOptions = true)
public class GenerateCommand implements Callable<Integer> {
<#list modelConfig.models as modelInfo>
    <#-- group model 分组命令 -->
    <#if modelInfo.groupKey??>

    /**
     * ${modelInfo.groupName}
     */
    static DataModel.${modelInfo.type} ${modelInfo.groupKey} = new DataModel.${modelInfo.type}();

    <#-- 根据分组生成命令类 -->
    /** 分组命令 ${modelInfo.groupKey} */
    @CommandLine.Command(name = "${modelInfo.groupKey}")
    @Data
    public static class ${modelInfo.type}Command implements Runnable {
    <#list modelInfo.models as subModel>
        <@generateOption indent="        " modelInfo=subModel />
    </#list>

        /** 将参数传递给外层对象 */
        @Override
        public void run() {
        <#list modelInfo.models as subModelInfo>
            ${modelInfo.groupKey}.set${subModelInfo.fieldName? cap_first}(${subModelInfo.fieldName});
        </#list>
        }
    }
    <#else>
    <@generateOption indent="    " modelInfo=modelInfo />
    </#if>
</#list>

    <#-- 生成调用方法 -->
    @Override
    public Integer call() throws Exception {
        <#list modelConfig.models as modelInfo>
        <#if modelInfo.groupKey??>
        <#if modelInfo.condition??>
        if (${modelInfo.condition}) {
            <@generateCommand indent="            " modelInfo=modelInfo />
        }
        <#else>
        <@generateCommand indent="      " modelInfo=modelInfo />
        </#if>
        </#if>
        </#list>
        <#-- 填充数据模型对象 -->
        DataModel dataModel = new DataModel();  // 数据模型
        BeanUtil.copyProperties(this, dataModel);
        <#list modelConfig.models as modelInfo>
        <#if modelInfo.groupKey??>
        dataModel.set${modelInfo.type}(${modelInfo.groupKey});
        </#if>
        </#list>
        System.out.println("配置信息: " + dataModel);
        // 执行代码生成方法
        FileGenerator.doGenerate(dataModel);
        return 0;
    }
}
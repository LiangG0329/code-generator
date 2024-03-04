package ${basePackage}.generator;

import freemarker.template.TemplateException;
import ${basePackage}.model.DataModel;

import java.io.File;
import java.io.IOException;

<#macro generateFile indent fileInfo>
${indent}inputPath = new File(inputRootPath, "${fileInfo.inputPath}").getAbsolutePath();
${indent}outputPath = new File(outputRootPath, "${fileInfo.outputPath}").getAbsolutePath();
<#if fileInfo.generateType == "static">
${indent}StaticFileGenerator.copyFilesByHutool(inputPath, outputPath);
<#else>
${indent}DynamicFileGenerator.doGenerate(inputPath, outputPath, model);
</#if>
</#macro>
/**
 * 文件生成
 * @author ${author}
 * @create 2024/2/13
 */
public class FileGenerator {
    /**
    * 生成静态文件和动态文件
    * @param model 数据模型
    * @throws TemplateException
    * @throws IOException
    */
    public static void doGenerate(DataModel model) throws TemplateException, IOException {
        String inputRootPath = "${fileConfig.inputRootPath}";
        String outputRootPath = "${fileConfig.outputRootPath}";

        String inputPath;
        String outputPath;
    <#-- 获取model变量 -->
    <#if modelConfig?has_content && modelConfig.models?has_content>
    <#list modelConfig.models as modelInfo>
        <#-- 分组model -->
        <#if modelInfo.groupKey??>
        <#list modelInfo.models as subModelInfo>
        ${subModelInfo.type} ${subModelInfo.fieldName} = model.get${modelInfo.groupKey? cap_first}().get${subModelInfo.fieldName? cap_first}();
        </#list>
        <#else >
        ${modelInfo.type} ${modelInfo.fieldName} = model.get${modelInfo.fieldName? cap_first}();
        </#if>
    </#list>
    </#if>
    <#if fileConfig?has_content && fileConfig.files?has_content>
    <#list fileConfig.files as fileInfo>
        <#if fileInfo.groupKey??>

         // 分组文件 groupKey = ${fileInfo.groupKey}
        <#if fileInfo.condition??>
        if (${fileInfo.condition}) {
            <#list fileInfo.files as fileInfo>
            <@generateFile indent="            " fileInfo=fileInfo/>
            </#list>
        }
        <#else>
        <#list fileInfo.files as fileInfo>
        <@generateFile fileInfo=fileInfo indent="        "/>
        </#list>
        </#if>
        <#else>

        <#if fileInfo.condition??>
        if (${fileInfo.condition}) {
            <@generateFile indent="            " fileInfo=fileInfo/>
        }
        <#else>
        <@generateFile indent="        " fileInfo=fileInfo/>
        </#if>
        </#if>
    </#list>
    </#if>
    }
}

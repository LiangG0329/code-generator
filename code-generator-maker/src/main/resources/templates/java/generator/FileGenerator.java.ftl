package ${basePackage}.generator;

import freemarker.template.TemplateException;

import java.io.File;
import java.io.IOException;

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
    public static void doGenerate(Object model) throws TemplateException, IOException {
        String inputRootPath = "${fileConfig.inputRootPath}";
        String outputRootPath = "${fileConfig.outputRootPath}";

        String inputPath;
        String outputPath;
    <#list fileConfig.files as fileInfo>

        inputPath = new File(inputRootPath, "${fileInfo.inputPath}").getAbsolutePath();
        outputPath = new File(outputRootPath, "${fileInfo.outputPath}").getAbsolutePath();
        <#if fileInfo.generateType == "static">
        // 静态文件生成
        StaticFileGenerator.copyFilesByHutool(inputPath, outputPath);
        <#else>
        // 动态文件生成
        DynamicFileGenerator.doGenerate(inputPath, outputPath, model);
        </#if>
    </#list>
    }
}

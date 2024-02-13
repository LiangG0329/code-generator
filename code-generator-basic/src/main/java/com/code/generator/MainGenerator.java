package com.code.generator;

import com.code.model.MainTemplateConfig;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.IOException;

/**
 * 动静结合，生成ACM(求和代码)
 * @author Liang
 * @create 2024/2/13
 */
public class MainGenerator {
    public static void main(String[] args) throws TemplateException, IOException {
        // 1.静态文件生成
        String rootPath = System.getProperty("user.dir");
        // 输入路径
        String inputPath = rootPath + File.separator + "code-generator-demo-projects" + File.separator + "acm-template";
        System.out.println("inputPath = " + inputPath);
        // 输出路径
        String outputPath = rootPath + File.separator + "code-generator-basic\\output";
        System.out.println("outputPath = " + outputPath);
        // 静态复制
        StaticGenerator.copyFilesByHutool(inputPath, outputPath);

        // 2.动态文件生成
        String projectPath = System.getProperty("user.dir") + File.separator + "code-generator-basic";
        String dynamicInputPath = projectPath + File.separator + "src/main/resources/templates/MainTemplate.java.ftl";
        String dynamicOutputPath = projectPath + File.separator + "output/MainTemplate.java";
        MainTemplateConfig mainTemplateConfig = new MainTemplateConfig();
        mainTemplateConfig.setAuthor("hello world:");
        mainTemplateConfig.setOutputText("out:");
        DynamicGenerator.doGenerator(dynamicInputPath, dynamicOutputPath, mainTemplateConfig);
    }
}

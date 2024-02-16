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
    public static void doGenerate(Object model) throws TemplateException, IOException {
        // 1.静态文件生成
        String rootPath = System.getProperty("user.dir");
        System.out.println("rootPath = " + rootPath);
        // 输入路径
        String inputPath = rootPath + File.separator + "code-generator-demo-projects" + File.separator + "acm-template";
        System.out.println("staticInputPath = " + inputPath);
        // 输出路径
        String outputPath = rootPath + File.separator + "code-generator-basic\\output";
        System.out.println("staticOutputPath = " + outputPath);
        // 静态复制
        StaticGenerator.copyFilesByHutool(inputPath, outputPath);

        // 2.动态文件生成
        String projectPath = System.getProperty("user.dir") + File.separator + "code-generator-basic";
        String dynamicInputPath = projectPath + File.separator + "src/main/resources/templates/MainTemplate.java.ftl";
        System.out.println("dynamicInputPath = " + dynamicInputPath);
        String dynamicOutputPath = projectPath + File.separator + "output/MainTemplate.java";
        System.out.println("dynamicOutputPath = " + dynamicOutputPath);
        DynamicGenerator.doGenerator(dynamicInputPath, dynamicOutputPath, model);
    }

    public static void main(String[] args) throws TemplateException, IOException {
        MainTemplateConfig mainTemplateConfig = new MainTemplateConfig();
        mainTemplateConfig.setAuthor("hello world:");
        mainTemplateConfig.setOutputText("out:");
        doGenerate(mainTemplateConfig);
    }
}

package com.code.maker.generator.file;

import com.code.maker.model.DataModel;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.IOException;

/**
 * 文件生成
 * @author Liang
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
        String inputRootPath = "E:\\pojects\\code-gengrator\\code-generator-demo-projects\\acm-template-pro";
        String outputRootPath = "E:\\pojects\\code-gengrator\\acm-template-pro";

        String inputPath;
        String outputPath;

        // 1.动态文件生成

        inputPath = new File(inputRootPath, "src/com/code/acm/MainTemplate.java.ftl").getAbsolutePath();
        outputPath = new File(outputRootPath, "src/com/code/acm/MainTemplate.java").getAbsolutePath();
        DynamicFileGenerator.doGenerate(inputPath, outputPath, model);

        // 2.静态文件生成

        inputPath = new File(inputRootPath, ".gitignore").getAbsolutePath();
        outputPath = new File(outputRootPath, ".gitignore").getAbsolutePath();
        StaticFileGenerator.copyFilesByHutool(inputPath, outputPath);

        inputPath = new File(inputRootPath, "README.md").getAbsolutePath();
        outputPath = new File(outputRootPath, "README.md").getAbsolutePath();
        StaticFileGenerator.copyFilesByHutool(inputPath, outputPath);
    }

    /** 测试 */
    public static void main(String[] args) throws TemplateException, IOException {
        DataModel mainTemplateConfig = new DataModel();
        mainTemplateConfig.setAuthor("hello world:");
        mainTemplateConfig.setLoop(false);
        mainTemplateConfig.setOutputText("out:");
        doGenerate(mainTemplateConfig);
    }
}

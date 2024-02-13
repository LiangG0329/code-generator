package com.code.generator;

import cn.hutool.core.io.FileUtil;
import com.code.model.MainTemplateConfig;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * @author
 * @create 2024/2/13
 */
public class DynamicGenerator {
    public static void main(String[] args) throws IOException, TemplateException {
        // 指定模板文件所在的路径
        String projectPath = System.getProperty("user.dir") + File.separator + "code-generator-basic";
        String inputPath = projectPath + File.separator + "src/main/resources/templates/MainTemplate.java.ftl";
        String outputPath = projectPath + File.separator + "output/MainTemplate.java";
        MainTemplateConfig mainTemplateConfig = new MainTemplateConfig();
        mainTemplateConfig.setAuthor("liang");
        mainTemplateConfig.setOutputText("out:");
        doGenerator(inputPath, outputPath, mainTemplateConfig);
    }

    /**
     * 生成文件
     * @param inputPath  模板文件输入路径
     * @param outputPath  生成文件输出路径
     * @param model  数据模型
     * @throws IOException
     * @throws TemplateException
     */
    public static void doGenerator(String inputPath, String outputPath, Object model) throws IOException, TemplateException {
        // 创建Configuration 对象，参数为 FreeMarker 版本号
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_32);

        // 指定模板文件所在的路径
        File templateDir = new File(inputPath).getParentFile();
        configuration.setDirectoryForTemplateLoading(templateDir);

        // 设置模板文件使用的字符集,数字格式
        configuration.setDefaultEncoding("utf-8");
        configuration.setNumberFormat("0.######");

        // 创建模板对象，加载指定模板
        String templateName = new File(inputPath).getName();
        Template template = configuration.getTemplate(templateName);

        // 数据模型(对象/HashMap)，填充模板
        // MainTemplateConfig mainTemplateConfig = (MainTemplateConfig) model;

        // 文件不存在则创建文件和父目录
        if (!FileUtil.exist(outputPath)) {
            FileUtil.touch(outputPath);
        }

        // 指定生成的文件路径和名称
        Writer out = new FileWriter(outputPath);

        // 调用template的process,处理并生成文件
        template.process(model, out);

        // 关闭输出
        out.close();
    }
}
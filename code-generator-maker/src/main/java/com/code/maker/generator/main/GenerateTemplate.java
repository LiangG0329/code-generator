package com.code.maker.generator.main;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;
import com.code.maker.generator.GitInit;
import com.code.maker.generator.file.DynamicFileGenerator;
import com.code.maker.meta.Meta;
import com.code.maker.meta.MetaManager;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.IOException;

/**
 * 核心生成器模板,抽象类,可根据需要重写方法,定制代码生成器
 *
 * @author Liang
 * @create 2024/2/20
 */
public abstract class GenerateTemplate {
    /**
     * 执行制作代码生成器流程
     *
     * @throws TemplateException
     * @throws IOException
     * @throws InterruptedException
     */
    public void doGenerate() throws TemplateException, IOException, InterruptedException {
        Meta meta = MetaManager.getMeta();
        System.out.println("meta = " + meta);

        // 输出根路径
        String projectPath = System.getProperty("user.dir");
        String outputPath = projectPath + File.separator + "generated" + File.separator + meta.getName();
        if (!FileUtil.exist(outputPath)) {
            FileUtil.mkdir(outputPath);
        }

        // 1.复制原始文件到生成代码包 (后续使用相对路径生成代码，提高可移植性)
        String sourceCopyPath = copySource(meta, outputPath);

        // 2.代码生成
        generateCode(meta, outputPath);

        // 3.构建 jar 包
        String jarPath = buildJar(meta, outputPath);

        // 4.封装脚本
        String Output = buildScript(jarPath, outputPath);

        // 5.生成精简版代码生成器(产物包)
        buildDist(outputPath, sourceCopyPath, jarPath, Output);

        // 6.git init代码托管, 根据需要可开启支持使用 Git 版本控制工具来托管
        //gitInit(projectPath, outputPath);
    }

    /**
     * 复制原始文件
     * @param meta 元信息对象
     * @param outputPath 生成器输出生成根路径
     * @return 复制生成文件的绝对路径
     */
    protected String copySource(Meta meta, String outputPath) {
        String sourceRootPath = meta.getFileConfig().getSourceRootPath();
        String sourceCopyPath = outputPath + File.separator + ".source";
        FileUtil.copy(sourceRootPath, sourceCopyPath, true);
        return sourceCopyPath;
    }

    /**
     * 代码生成
     *
     * @param meta 元信息对象
     * @param outputPath 输出生成根路径
     * @throws IOException
     * @throws TemplateException
     */
    protected void generateCode(Meta meta, String outputPath) throws IOException, TemplateException {
        // 读取 resources 目录
        ClassPathResource classPathResource = new ClassPathResource("");
        String inputResourcePath = classPathResource.getAbsolutePath();

        // Java 包基础路径
        String outputBasePackage = meta.getBasePackage();
        String outputBasePackagePath = StrUtil.join("/", StrUtil.split(outputBasePackage, "."));
        String outputBaseJavaPackagePath = outputPath + File.separator + "src/main/java/" + outputBasePackagePath;

        String inputFilePath;
        String outputFilePath;

        // model.DataModel
        inputFilePath = inputResourcePath + File.separator + "templates/java/model/DataModel.java.ftl";
        outputFilePath = outputBaseJavaPackagePath + "/model/DataModel.java";
        DynamicFileGenerator.doGenerate(inputFilePath , outputFilePath, meta);

        // cli.command.ConfigCommand
        inputFilePath = inputResourcePath + File.separator + "templates/java/cli/command/ConfigCommand.java.ftl";
        outputFilePath = outputBaseJavaPackagePath + "/cli/command/ConfigCommand.java";
        DynamicFileGenerator.doGenerate(inputFilePath , outputFilePath, meta);

        // cli.command.GenerateCommand
        inputFilePath = inputResourcePath + File.separator + "templates/java/cli/command/GenerateCommand.java.ftl";
        outputFilePath = outputBaseJavaPackagePath + "/cli/command/GenerateCommand.java";
        DynamicFileGenerator.doGenerate(inputFilePath , outputFilePath, meta);

        // cli.command.ListCommand
        inputFilePath = inputResourcePath + File.separator + "templates/java/cli/command/ListCommand.java.ftl";
        outputFilePath = outputBaseJavaPackagePath + "/cli/command/ListCommand.java";
        DynamicFileGenerator.doGenerate(inputFilePath , outputFilePath, meta);

        // cli.CommandExecutor
        inputFilePath = inputResourcePath + File.separator + "templates/java/cli/CommandExecutor.java.ftl";
        outputFilePath = outputBaseJavaPackagePath + "/cli/CommandExecutor.java";
        DynamicFileGenerator.doGenerate(inputFilePath , outputFilePath, meta);

        // Main
        inputFilePath = inputResourcePath + File.separator + "templates/java/Main.java.ftl";
        outputFilePath = outputBaseJavaPackagePath + "/Main.java";
        DynamicFileGenerator.doGenerate(inputFilePath , outputFilePath, meta);

        // generator.DynamicGenerator
        inputFilePath = inputResourcePath + File.separator + "templates/java/generator/DynamicFileGenerator.java.ftl";
        outputFilePath = outputBaseJavaPackagePath + "/generator/DynamicFileGenerator.java";
        DynamicFileGenerator.doGenerate(inputFilePath , outputFilePath, meta);

        // generator.FileGenerator
        inputFilePath = inputResourcePath + File.separator + "templates/java/generator/FileGenerator.java.ftl";
        outputFilePath = outputBaseJavaPackagePath + "/generator/FileGenerator.java";
        DynamicFileGenerator.doGenerate(inputFilePath , outputFilePath, meta);

        // generator.StaticGenerator
        inputFilePath = inputResourcePath + File.separator + "templates/java/generator/StaticFileGenerator.java.ftl";
        outputFilePath = outputBaseJavaPackagePath + "/generator/StaticFileGenerator.java";
        DynamicFileGenerator.doGenerate(inputFilePath , outputFilePath, meta);

        // pom.xml
        inputFilePath = inputResourcePath + File.separator + "templates/pom.xml.ftl";
        outputFilePath = outputPath + File.separator + "pom.xml";
        DynamicFileGenerator.doGenerate(inputFilePath , outputFilePath, meta);

        // README.md 项目介绍文档
        inputFilePath = inputResourcePath + File.separator + "templates/README.md.ftl";
        outputFilePath = outputPath + File.separator + "README.md";
        DynamicFileGenerator.doGenerate(inputFilePath , outputFilePath, meta);
    }

    /**
     * 构建 jar 包
     *
     * @param meta 元信息对象
     * @param outputPath 输出生成根路径
     * @return 返回 jar 包的相对路径
     * @throws IOException
     * @throws InterruptedException
     */
    protected String buildJar(Meta meta, String outputPath) throws IOException, InterruptedException {
        JarGenerator.doGenerate(outputPath);
        String jarName = String.format("%s-%s-jar-with-dependencies.jar", meta.getName(), meta.getVersion());
        String jarPath = "target/" + jarName;
        return jarPath;
    }

    /**
     * 封装脚本
     *
     * @param jarPath 构建的 jar 包相对路径
     * @param outputPath 输出输出路径
     * @return 生成的可执行脚本的路径
     */
    protected String buildScript(String jarPath,String outputPath) {
        String shellOutputPath = outputPath + File.separator + "generator";
        ScriptGenerator.doGenerate(shellOutputPath, jarPath);
        return shellOutputPath;
    }

    /**
     * 生成精简版代码生成器
     *
     * @param outputPath 输出生成根路径
     * @param sourceCopyPath 复制生成文件的绝对路径
     * @param jarPath jar包相对路径
     * @param shellOutputPath 封装脚本的绝对路径
     */
    protected String buildDist(String outputPath, String sourceCopyPath, String jarPath, String shellOutputPath) {
        String distOutputPath = outputPath + "-dist";
        // - 拷贝jar包
        String targetAbsolutePath = distOutputPath + File.separator + "target";
        FileUtil.mkdir(targetAbsolutePath);
        String jarAbsolutePath = outputPath + File.separator + jarPath;
        FileUtil.copy(jarAbsolutePath, targetAbsolutePath, true);
        // - 拷贝脚本文件
        FileUtil.copy(shellOutputPath, distOutputPath, true);
        FileUtil.copy(shellOutputPath + ".bat", distOutputPath, true);
        // - 拷贝源模板文件
        FileUtil.copy(sourceCopyPath, distOutputPath, true);

        return distOutputPath;
    }

    /**
     * 制作zip压缩包
     *
     * @param filePath 需制作zip压缩包的文件路径
     * @return zip压缩包路径
     */
    protected String buildZip(String filePath) {
        String zipPath = filePath +".zip";
        ZipUtil.zip(filePath, zipPath);
        return zipPath;
    }

    /**
     *  Git代码托管初始化
     *
     * @param projectPath 项目根路径
     * @param outputPath 输出生成根路径
     * @throws IOException
     * @throws InterruptedException
     */
    protected void gitInit(String projectPath, String outputPath) throws IOException, InterruptedException {
        // 完整版代码托管
        GitInit.doInit(outputPath);
        // 复制gitignore文件
        String gitIgnorePath = projectPath + File.separator + ".gitignore";
        FileUtil.copy(gitIgnorePath, outputPath, false);
        // 精简版代码托管
        String distOutputPath = outputPath + "-dist";
        GitInit.doInit(distOutputPath);
        FileUtil.copy(gitIgnorePath, distOutputPath, false);
    }
}

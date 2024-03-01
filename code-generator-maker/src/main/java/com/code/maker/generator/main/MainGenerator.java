package com.code.maker.generator.main;

import freemarker.template.TemplateException;

import java.io.IOException;

/**
 * 核心生成器,继承代码生成器模板,并根据需要重写方法,生成定制的代码生成器
 *
 * @author Liang
 * @create 2024/2/17
 */
public class MainGenerator extends GenerateTemplate{
    @Override
    protected String buildDist(String outputPath, String sourceCopyPath, String jarPath, String shellOutputPath) {
        System.out.println("未选择生成精简版生成器");
        return "";
    }

    /** 测试 */
    public static void main(String[] args) throws TemplateException, IOException, InterruptedException {
        MainGenerator mainGenerator = new MainGenerator();
        mainGenerator.doGenerate();
    }
}

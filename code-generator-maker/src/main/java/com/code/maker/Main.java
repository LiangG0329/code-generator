package com.code.maker;

//import com.code.maker.cli.CommandExecutor;

import com.code.maker.generator.MainGenerator;
import freemarker.template.TemplateException;

import java.io.IOException;

/**
 * 生成测试
 * @author Liang
 * @create 2024/2/13
 */
public class Main {
    public static void main(String[] args) throws TemplateException, IOException, InterruptedException {
        MainGenerator mainGenerator = new MainGenerator();
        mainGenerator.doGenerate();
    }
}
package com.code.cli.command;

import cn.hutool.core.io.FileUtil;
import picocli.CommandLine;

import java.io.File;
import java.util.List;

/**
 * list辅助命令,遍历生成文件夹,输出子文件列表
 * @author Liang
 * @create 2024/2/15
 */
@CommandLine.Command(name = "list", description = "查看文件列表", mixinStandardHelpOptions = true)
public class ListCommand implements Runnable{

    @Override
    public void run() {
        // 项目根路径
        String rootPath = System.getProperty("user.dir");
        // 输入路径
        File inputPath = new File(rootPath, "code-generator-demo-projects\\acm-template").getAbsoluteFile();
        System.out.println("inputPath = " + inputPath);
        // 使用Hutool工具包遍历文件夹
        List<File> files = FileUtil.loopFiles(inputPath);
        System.out.println("All files: ");
        for (File file: files) {
            System.out.println(file);
        }
    }
}

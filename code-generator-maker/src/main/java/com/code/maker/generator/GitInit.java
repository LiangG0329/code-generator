package com.code.maker.generator;

import cn.hutool.core.io.FileUtil;

import java.io.*;

/**
 * git 代码托管,执行git init
 *
 * @author Liang
 * @create 2024/2/19
 */
public class GitInit {
    public static void doInit(String projectDir) throws IOException, InterruptedException {
        String initCommand = "git init";

        ProcessBuilder processBuilder = new ProcessBuilder(initCommand.split(" "));
        processBuilder.directory(new File(projectDir));

        Process process = processBuilder.start();

        // 读取命令输出
        InputStream inputStream = process.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while((line = reader.readLine()) != null) {
            // 打印输出
            System.out.println(line);
        }

        // 等待命令执行完成
        int exitCode = process.waitFor();
        System.out.println("git init 命令执行结束，退出码：" + exitCode);
    }

    /** 测试 */
    public static void main(String[] args) throws IOException, InterruptedException {
        String projectDir = "E:\\projects\\code-generator\\code-generator-maker\\generated\\acm-template-pro-generator-dist";
        doInit(projectDir);
        // 复制gitignore文件
        String projectPath = System.getProperty("user.dir");
        String gitIgnorePath = projectPath + File.separator + ".gitignore";
        System.out.println("gitIgnorePath = " + gitIgnorePath);
        FileUtil.copy(gitIgnorePath, projectDir, false);
    }
}

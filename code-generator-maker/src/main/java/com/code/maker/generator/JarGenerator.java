package com.code.maker.generator;

import java.io.*;

/**
 *  jar 包生成工具
 * @author Liang
 * @create 2024/2/18
 */
public class JarGenerator {
    /**
     * 构建 jar 包
     * @param projectDir 项目目录
     * @throws IOException
     */
    public static void doGenerate(String projectDir) throws IOException, InterruptedException {
        // 清理之前的构建并打包
        // 注意不同操作系统，执行的命令不同
        String winMavenCommand = "mvn.cmd clean package -DskipTests=true";
        String otherMavenCommand = "mvn clean package -DskipTests=true";

        String mvnCommand = winMavenCommand;

        ProcessBuilder processBuilder = new ProcessBuilder(mvnCommand.split(" "));
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
        System.out.println("命令执行结束，退出码：" + exitCode);
    }

    /** 测试 */
    public static void main(String[] args) throws IOException, InterruptedException {
        doGenerate("E:\\projects\\code-generator\\code-generator-basic");
    }
}

package com.code.maker.generator.main;

import java.io.*;
import java.util.Map;

/**
 *  jar 包生成工具
 * @author Liang
 * @create 2024/2/18
 */
public class JarGenerator {
    /**
     * 构建 jar 包
     * @param projectDir 项目目录
     * @throws RuntimeException
     */
    public static void doGenerate(String projectDir) throws RuntimeException {
        // 清理之前的构建并打包
        // 注意不同操作系统，执行的命令不同
        String mvnCommand;
        String osName = System.getProperty("os.name");
        if (osName.startsWith("Windows")) {
            mvnCommand = "mvn.cmd clean package -DskipTests=true";
        } else {
            mvnCommand = "mvn clean package -DskipTests=true";
        }

        ProcessBuilder processBuilder = new ProcessBuilder(mvnCommand.split(" "));
        processBuilder.directory(new File(projectDir));
        Map<String, String> environment = processBuilder.environment();
        System.out.println(environment);

        // 执行命令
        try {
            Process process = processBuilder.start();

            // 创建一个新线程来读取标准输出
            new Thread(() -> {
                try (BufferedReader stderr = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = stderr.readLine()) != null) {
                        System.out.println(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            // 创建一个新线程来读取错误输出
            new Thread(() -> {
                try (BufferedReader stdout = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                    String line;
                    while ((line = stdout.readLine()) != null) {
                        System.out.println(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            // 等待命令执行完成
            int exitCode = process.waitFor();
            System.out.println("maven package 命令执行结束，退出码：" + exitCode);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("jar包构建失败");
        }

    }

    /** 测试 */
    public static void main(String[] args) throws IOException, InterruptedException {
        doGenerate("E:\\projects\\code-generator\\code-generator-basic");
    }
}

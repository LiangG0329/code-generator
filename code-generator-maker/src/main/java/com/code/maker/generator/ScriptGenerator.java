package com.code.maker.generator;

import cn.hutool.core.io.FileUtil;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

/**
 * 脚本封装工具
 * @author Liang
 * @create 2024/2/18
 */
public class ScriptGenerator {
    public static void doGenerate(String outputPath, String jarPath) {
        // linux 系统
        // #!/bin/bash
        // java -jar target/code-generator-basic-1.0-SNAPSHOT-jar-with-dependencies.jar "$@"
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("#!/bin/bash").append("\n");
        stringBuilder.append(String.format("java -jar %s \"$@\"", jarPath)).append("\n");
        // 生成脚本文件
        FileUtil.writeBytes(stringBuilder.toString().getBytes(StandardCharsets.UTF_8), outputPath);
        // 添加可执行权限
        try {
            Set<PosixFilePermission> permissions = PosixFilePermissions.fromString("rwxrwxrwx");
            Files.setPosixFilePermissions(Paths.get(outputPath), permissions);
        } catch (Exception e) {
//            e.printStackTrace();
        }

        // window系统
        // @echo off
        // java -jar target/code-generator-basic-1.0-SNAPSHOT-jar-with-dependencies.jar %*
        stringBuilder = new StringBuilder();
        stringBuilder.append("@echo off").append("\n");
        stringBuilder.append(String.format("java -jar %s %%*", jarPath)).append("\n");
        FileUtil.writeBytes(stringBuilder.toString().getBytes(StandardCharsets.UTF_8), outputPath + ".bat");
    }

    /** 测试 */
    public static void main(String[] args) throws IOException {
        String outputPath = System.getProperty("user.dir") + File.separator + "generator";
        doGenerate(outputPath, "");
    }
}

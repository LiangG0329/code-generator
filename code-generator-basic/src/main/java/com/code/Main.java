package com.code;

import com.code.cli.CommandExecutor;

/**
 * 生成测试
 * @author Liang
 * @create 2024/2/13
 */
public class Main {
    public static void main(String[] args) {
//        args = new String[]{"generator", "-l", "-a", "-o"};
//        args = new String[]{"config"};
//        args = new String[]{"list"};

        // 将代码生成器打成 jar 包，支持用户执行并使用命令行工具动态输入参数
        CommandExecutor commandExecutor = new CommandExecutor();
        commandExecutor.doExecute(args);
    }
}
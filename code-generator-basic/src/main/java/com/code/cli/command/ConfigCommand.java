package com.code.cli.command;

import cn.hutool.core.util.ReflectUtil;
import com.code.model.MainTemplateConfig;
import picocli.CommandLine;

import java.lang.reflect.Field;

/**
 * config 子命令,输出允许用户传入的动态参数的信息
 * @author Liang
 * @create 2024/2/15
 */
@CommandLine.Command(name = "config", description = "查看参数信息", mixinStandardHelpOptions = true)
public class ConfigCommand implements Runnable{
    @Override
    public void run() {
        // config命令逻辑
        System.out.println("查看参数信息:");

        // 通过 Java 的反射机制，在程序运行时动态打印出对象属性的信息

        // 方法1: JDK原生反射语法
//        Class<?> myClass = MainTemplateConfig.class;
        // 获取类的所有字段信息
//        Field[] fields = myClass.getDeclaredFields();

        // 方法2: Hutool的反射工具类
        Field[] fields = ReflectUtil.getFields(MainTemplateConfig.class);

        // 遍历打印每个字段信息
        for (Field field : fields) {
            System.out.println("字段名称: " + field.getName());
            System.out.println("权限修饰符: " + java.lang.reflect.Modifier.toString(field.getModifiers()));
            System.out.println("字段类型: " + field.getType());
            System.out.println("---------");
        }
    }
}

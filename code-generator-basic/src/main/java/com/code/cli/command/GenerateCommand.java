package com.code.cli.command;

import cn.hutool.core.bean.BeanUtil;
import com.code.generator.MainGenerator;
import com.code.model.MainTemplateConfig;
import lombok.Data;
import picocli.CommandLine;

import java.util.concurrent.Callable;

/**
 * generate辅助命令,生成ACM求和模版代码
 * @author Liang
 * @create 2024/2/15
 */
@Data
@CommandLine.Command(name = "generate", description = "代码生成", mixinStandardHelpOptions = true)
public class GenerateCommand implements Callable<Integer> {
    /** 子命令1: 是否开启循环 */
    @CommandLine.Option(names = {"-l", "--loop"}, arity = "0..1", description = "是否循环,输入true/false", interactive = true, echo = true)
    private boolean loop;

    /** 子命令2: 作者注释 */
    @CommandLine.Option(names = {"-a", "--author"}, arity = "0..1", description = "作者名称", interactive = true, echo = true)
    private String author = "admin";

    /** 子命令3: 输出文本提示 */
    @CommandLine.Option(names = {"-o", "--outputText"}, arity = "0..1", description = "输出文本", interactive = true, echo = true)
    private String outputText = "sum = ";

    @Override
    public Integer call() throws Exception {
        MainTemplateConfig mainTemplateConfig = new MainTemplateConfig();  // 数据模型
        BeanUtil.copyProperties(this, mainTemplateConfig);
        System.out.println("配置信息: " + mainTemplateConfig);
        // 执行代码生成方法
        MainGenerator.doGenerate(mainTemplateConfig);
        return 0;
    }
}

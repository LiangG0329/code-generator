package ${basePackage}.cli;

import ${basePackage}.cli.command.GenerateCommand;
import ${basePackage}.cli.command.ListCommand;
import ${basePackage}.cli.command.ConfigCommand;
import picocli.CommandLine;
import picocli.CommandLine.Command;

/**
 * @author Liang
 * @create 2024/2/15
 */
@Command(name = "${name}", mixinStandardHelpOptions = true)
public class CommandExecutor implements Runnable{
    private final CommandLine commandLine;

    // 静态代码块，类加载时初始化，只初始化一次
    {
        commandLine = new CommandLine(this)
                .addSubcommand(new GenerateCommand())
                .addSubcommand(new ConfigCommand())
                .addSubcommand(new ListCommand());
    }

    @Override
    public void run() {
        // 不输入命令时，给出友好提示
        System.out.println("请输入命令，或输入 --help 查看帮助手册");
    }

    /**
     * 执行命令
     * @param args 命令参数
     * @return 执行结果退出码
     */
    public Integer doExecute(String[] args) {
        return commandLine.execute(args);
    }
}

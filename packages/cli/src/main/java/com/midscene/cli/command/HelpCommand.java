package com.midscene.cli.command;

import com.midscene.cli.MidSceneCli;

/**
 * 显示命令帮助信息命令
 */
public class HelpCommand implements Command {
    private final MidSceneCli cli;
    
    public HelpCommand(MidSceneCli cli) {
        this.cli = cli;
    }
    
    @Override
    public int execute(String[] args) {
        if (args.length > 0) {
            // 显示特定命令的帮助
            String commandName = args[0];
            System.out.println(cli.getCommandHelp(commandName));
        } else {
            // 显示通用帮助
            cli.printHelp();
        }
        return 0;
    }
    
    @Override
    public String getHelp() {
        StringBuilder help = new StringBuilder();
        help.append("命令: help - 显示命令帮助信息\n\n");
        help.append("使用方法: midscene help [命令名]\n\n");
        help.append("参数:\n");
        help.append("  [命令名]    可选，指定要查看帮助的命令\n\n");
        help.append("示例:\n");
        help.append("  midscene help              - 显示通用帮助信息\n");
        help.append("  midscene help run          - 显示run命令的详细帮助\n");
        help.append("  midscene help record       - 显示record命令的详细帮助\n");
        return help.toString();
    }
    
    @Override
    public String getName() {
        return "help";
    }
}
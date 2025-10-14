package com.midscene.cli.command;

/**
 * 命令接口
 * 所有CLI命令都需要实现此接口
 */
public interface Command {
    /**
     * 执行命令
     * @param args 命令参数
     * @return 退出码，0表示成功，非0表示失败
     */
    int execute(String[] args);
    
    /**
     * 获取命令的帮助信息
     * @return 命令的帮助文本
     */
    String getHelp();
    
    /**
     * 获取命令名称
     * @return 命令名称
     */
    String getName();
}
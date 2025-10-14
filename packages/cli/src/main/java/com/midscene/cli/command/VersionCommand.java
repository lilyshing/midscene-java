package com.midscene.cli.command;

/**
 * 显示版本信息命令
 */
public class VersionCommand implements Command {
    private final String version;
    
    public VersionCommand(String version) {
        this.version = version;
    }
    
    @Override
    public int execute(String[] args) {
        System.out.println("midscene version " + version);
        return 0;
    }
    
    @Override
    public String getHelp() {
        StringBuilder help = new StringBuilder();
        help.append("命令: version - 显示版本信息\n\n");
        help.append("使用方法: midscene version\n\n");
        help.append("选项:\n");
        help.append("  无\n\n");
        help.append("示例:\n");
        help.append("  midscene version       - 显示当前安装的版本\n");
        return help.toString();
    }
    
    @Override
    public String getName() {
        return "version";
    }
}